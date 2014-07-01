package link.angelmaker.manager;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.AngelMakerConfig;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.SequencedNode;

/**
 * A AMManager that facilitates retransmitting. This manager has a memory of
 * Bytes, which it can retransmit on request. This manager is optimized to send
 * a FlaggingNode containing an ErrorDetectionNode containing a SequencedNode.
 * Pipelining is used, it keeps sending the next data till it hears otherwise.
 * 
 * Memory: Contains the (2^AngelMakerConfig.MESSAGE_BIT_COUNT)-1 Bytes last
 * sent. The memory is initialized to null, which indicates there is nothing
 * more in memory. Every byte is hold in place till
 * (2^AngelMakerConfig.MESSAGE_BIT_COUNT)-1 new bytes are loaded in after it.
 * loadNew indicates the most recent loaded in Byte. After overwritten, the byte
 * is lost and can no longer be retransmitted. This effect is also caused by
 * sequence numbers wrapping around.
 * 
 * 
 * Sending:
 * First of the index (byte sequence number) of the next byte to send
 * gets set. This is (lastSent byte+1)%memory.length, if the other side
 * expressed everything is fine. If the other side requested a retransmission
 * (by telling the last byte it receives correct), this side starts sending
 * bytes from the next sequence number, effectively retransmitting.
 * 
 * Up to AngelMakerConfig.PACKET_BYTE_COUNT get pulled out of the queue,
 * starting at the next byte to send. When it next byte to send index equals the
 * loadNew it indicates that instead of sending the old byte on that index, a
 * new byte from the queueOut (queue to send) should be loaded in memory and
 * send.
 * 
 * Eventually this array is given to the node in use (typically FlaggingNode,
 * the converted data is extracted and that handed to the BitExchanger.
 * 
 * This Node should be able to receive between (and including)
 * 0-AngelMakerConfig.PACKET_BYTE_COUNT amount of bytes. If it receives 0 bytes,
 * it acts as a filler.
 * 
 * Every 'node' (converted data) send contains a sequence number of the next
 * byte and a message. This message can either be MESSAGE_FINE
 * (2^AngelMakerConfig.MESSAGE_BIT_COUNT), or the last byte received correctly.
 * This is how it asks for retransmissions. Fillers also contain these messages
 * and sequence numbers.
 * 
 * 
 * Receiving:
 * The received bits from the BitExchanger get 'streamed' into a
 * Node, till it indicates it is full. When it is full it gets checked if it is
 * correct (ErrorDetectionNode facilitates this). If it is correct the sequence
 * number and message get read. The message receives gets handed to the send
 * thread, which uses this to decide if it needs to retransmit.
 * 
 * If the received sequence number corresponds with the sequence number expected
 * the data gets read. This data can again be
 * 0-AngelMakerConfig.PACKET_BYTE_COUNT amount of bytes. This data is put in the
 * queueIn (received bytes), for AngelMaker to read.
 * 
 * The expected sequence number is heightened with the amount of bytes received.
 * (This means that a filler, although having a sequence number, has no effect
 * on the expected sequence number).
 * 
 * 
 * 
 * @author I3anaan
 * 
 */
public class MemoryRetransmittingManager extends Thread implements AMManager, AMManager.Server {
	private BitExchanger exchanger;
	private ArrayBlockingQueue<Byte> queueIn;
	private ArrayBlockingQueue<Byte> queueOut;

	public static final int MESSAGE_FINE = (int) Math.pow(2,
			AngelMakerConfig.MESSAGE_BIT_COUNT) - 1;
	/**
	 * Premade BitSet2[] so that messages do not have to be reconstructed
	 * everytime, now a index in this array gives a usable BitSet2.
	 */
	public BitSet2[] possibleMessages;

	private Node.Resetable receivingNode = new FlaggingNode(null);
	private Node.Resetable sendingNode = new FlaggingNode(null);

	private volatile int messageReceived;
	private volatile int messageToSend;

	
	private Byte[] memory;
	/**
	 * Pre-created array that contains the Bytes from which to build the new Node to send from.
	 */
	private Byte[] nodeBuildingBytes;
	/**
	 * Contains the current non-consumed converted bits received from the BitExchanger.
	 * The receiving Node uses this to fill itself.
	 */
	private BitSet2 spilledBitsIn;

	/**
	 * Receiver Thread.
	 */
	private Receiver receiver;
	private int lastReceivedCorrect = 254;
	private int lastSent = 254;
	/**
	 * The index of the oldest byte in memory.
	 * When sending this index, a new byte should be loaded in.
	 */
	private int loadNew = 0;
	/**
	 * Pre-created empty array for optimization.
	 */
	private static final byte[] emptyArray = new byte[] {};

	public MemoryRetransmittingManager() {
		this.queueIn = new ArrayBlockingQueue<Byte>(2048);
		this.queueOut = new ArrayBlockingQueue<Byte>(2048);
		this.memory = new Byte[MESSAGE_FINE]; // bitsUsed - amount of special
												// messages.
		nodeBuildingBytes = new Byte[AngelMakerConfig.PACKET_BYTE_COUNT];

		possibleMessages = new BitSet2[MESSAGE_FINE + 1];
		possibleMessages[MESSAGE_FINE] = intMessageToBitSet(MESSAGE_FINE);
		for (int i = 0; i < memory.length; i++) {
			memory[i] = null;
			possibleMessages[i] = intMessageToBitSet(i);
		}
		spilledBitsIn = new BitSet2();
		messageReceived = MESSAGE_FINE;
		messageToSend = MESSAGE_FINE;
	}

	@Override
	public void setExchanger(BitExchanger exchanger) {
		this.exchanger = exchanger;
	}

	@Override
	public void enable() {
		receiver = new Receiver();
		receiver.start();
	}

	/**
	 * Put the given bytes in the queue to be send.
	 * Blocking if queue gets full.
	 */
	@Override
	public void sendBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			try {
				queueOut.put(bytes[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
				AngelMaker.logger
						.warning("Dropped Byte to send while trying to put it in MemoryRetransmittingManager.queueOut.");
			}
		}
	}

	/**
	 * Read bytes from the received queue.
	 * Every byte is only returned once, similair to a queue.
	 * Does not block, returns an empty array instead.
	 */
	@Override
	public byte[] readBytes() {
		if (queueIn.isEmpty()) {
			return emptyArray;
		} else {
			ArrayList<Byte> arr = new ArrayList<Byte>();
			queueIn.drainTo(arr);
			return Bytes.toArray(arr);
		}
	}

	@Override
	public Node getCurrentSendingNode() {
		return sendingNode;
	}

	@Override
	public Node getCurrentReceivingNode() {
		AngelMaker.logger.info("Requested Current Receving Node");
		return receivingNode;
	}

	@Override
	/**
	 * Called by the BitExchanger when it needs new bits to send.
	 */
	public BitSet2 getNextBits() {
		if (messageReceived != MESSAGE_FINE) {
			lastSent = messageReceived; // Other side requested retransmitting
										// after this index
			messageReceived = MESSAGE_FINE; // Moved sending index down, now
											// continue assuming fine.
		}

		int indexToSend = (lastSent + 1) % (memory.length);
		// Reset array.
		for (int i = 0; i < AngelMakerConfig.PACKET_BYTE_COUNT; i++) {
			nodeBuildingBytes[i] = null;
		}
		for (int i = 0; i < AngelMakerConfig.PACKET_BYTE_COUNT; i++) {
			if ((indexToSend + i) % (memory.length) == loadNew) {
				// If added all the requested retransmissions.
				Byte newByte = queueOut.poll();
				if (newByte != null) {
					// If extra bytes to send, add to memory and prepare for
					// sending.
					memory[(indexToSend + i) % (memory.length)] = newByte;
					nodeBuildingBytes[i] = newByte;
					loadNew = (indexToSend + i + 1) % (memory.length);
				} else {
					i = AngelMakerConfig.PACKET_BYTE_COUNT;
					// Stop for loop, nothing else to send
				}
			} else {
				// Retransmissions first.
				nodeBuildingBytes[i] = memory[(indexToSend + i)
						% (memory.length)];
			}
			lastSent = (indexToSend + i) % memory.length;
		}

		// Build Node from created array

		sendingNode.reset();
		if (nodeBuildingBytes[0] != null) {
			sendingNode.giveOriginal(new BitSet2(nodeBuildingBytes));
		}

		if (sendingNode.getChildNodes()[0].getChildNodes()[0] instanceof SequencedNode) {
			SequencedNode seqNode = ((SequencedNode) sendingNode
					.getChildNodes()[0].getChildNodes()[0]);
			seqNode.setMessage(possibleMessages[messageToSend]);
			seqNode.setSeq(possibleMessages[(indexToSend) % memory.length]);
		} else {
			throw new IncompatibleModulesException();
		}
		messageToSend = MESSAGE_FINE; // Do not send same message multiple
										// times.
		return sendingNode.getConverted(); // Extract from Node.
	}

	/**
	 * Converts the given int to a unsigned BitSet2 representing that int.
	 * Uses up AngelMakerConfig.MESSAGE_BIT_COUNT bits.
	 */
	public static BitSet2 intMessageToBitSet(int message) {
		BitSet2 bs = new BitSet2(Ints.toByteArray(message));
		return bs.get(bs.length() - AngelMakerConfig.MESSAGE_BIT_COUNT,
				bs.length());
	}

	/**
	 * Thread that fills Nodes with received data.
	 * The data from these nodes gets extracted and put in the received queue (queueIn).
	 * The message received gets handed to the sender (MemoryRetransmittingManager main class).
	 * @author I3anaan
	 *
	 */
	private class Receiver extends Thread {

		@Override
		public void run() {
			while (true) {
				refillReceivingNode(); //wait till the next node (receivingNode) is fully received.
				
				Node errorDetection = receivingNode.getChildNodes()[0];
				if (errorDetection.isCorrect()) {
					//Packet has no errors.
					Node packetNode = errorDetection.getChildNodes()[0];
					if (packetNode instanceof SequencedNode) {
						SequencedNode seqNode = ((SequencedNode) packetNode);
						if (seqNode.getSeq().getUnsignedValue() == (lastReceivedCorrect + 1)
								% memory.length) {
							// Fully correct, expected sequence number.

							byte[] dataBytes = seqNode.getOriginal()
									.toByteArray();
							//Extract original data from received node.
							for (byte b : dataBytes) {
								try {
									queueIn.put(b);
									//Put it in the received queue.
								} catch (InterruptedException e) {
									// Should not happen, but if does, just drop
									// byte.
									e.printStackTrace();
									AngelMaker.logger
											.warning("Dropped received byte while trying to put it in MemoryRetransmittingManager.queueIn");
								}
							}
							//Update the expected sequence number
							lastReceivedCorrect = (lastReceivedCorrect + seqNode
									.getOriginal().length() / 8)
									% memory.length;
							int currentMessageReceived = seqNode.getMessage()
									.getUnsignedValue();
							if (currentMessageReceived != MESSAGE_FINE) {
								//Only if the message received is not fine set it to the most recent received message.
								messageReceived = currentMessageReceived;
							}
							messageToSend = MESSAGE_FINE;
						} else {
							// Only sequence number is wrong, packet is correct.
							//Extract message.
							int currentMessageReceived = seqNode.getMessage()
									.getUnsignedValue();
							if (currentMessageReceived != MESSAGE_FINE) {
								messageReceived = currentMessageReceived;
							}
							messageToSend = lastReceivedCorrect;
						}
					} else {
						throw new IncompatibleModulesException();
					}
				} else {
					// Packet has errors.
					messageToSend = lastReceivedCorrect;
				}
			}
		}

		/**
		 * Gives the receivinNode the received converted bits till it is full.
		 */
		private void refillReceivingNode() {
			receivingNode.reset();
			while (!receivingNode.isFull()) {
				spilledBitsIn = receivingNode.giveConverted(BitSet2
						.concatenate(spilledBitsIn, exchanger.readBits()));
			}
		}
	}


	@Override
	public String toString() {
		String result = "MemoryRetransmittingManager";
		result = result + "\n\tLastSent: " + lastSent + "\tNewestSend: "
				+ loadNew + "\tLastReceived: " + lastReceivedCorrect;
		return result;
	}
}

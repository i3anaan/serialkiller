package link;

import java.util.Arrays;

import util.BitSet2;
import common.Layer;
import phys.PhysicalLayer;
import util.Bytes;

/**
 * Do not use debouncer.
 * 
 * @author I3anaan
 * 
 */
public class DelayCorrectedFDXLinkLayerSectionSegment {

	boolean connectionSync;
	byte lastSentSyncPing = 0;
	byte previousByteSent = 0;
	byte previousByteReceived = 0;
	Layer down;

	private String connectionRole = "unkown"; // Debug

	FlaggedFrame lastReceivedFrame = new FlaggedFrame();
	FlaggedFrame frameToSendNext = new FlaggedFrame();

	public static final String SENDER = "sender";
	public static final String RECEIVER = "receiver";
	public static final byte NO_BYTE = -1;

	protected boolean readFrame;
	protected boolean setFrameToSend;

	private boolean allowedToSend;

	public DelayCorrectedFDXLinkLayerSectionSegment(PhysicalLayer down) {
		this.down = down;
	}

	public void exchangeFrame() {
		if (readFrame && setFrameToSend) {
			readFrame = false;
			setFrameToSend = false;
			BitSet2 incomingData = new BitSet2();
			BitSet2 outgoingData = frameToSendNext.getBitSet();
			log("Frame to send: " + frameToSendNext + "   outgoing bits: "
					+ outgoingData.toString());
			int bitsReceived = 0;
			int bitsSent = 0;
			boolean retry = false;

			try {
				while (bitsReceived < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9
						|| bitsSent < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9) {

					if (!connectionSync) {
						log(connectionRole + "  Setting up sync..");
						waitForSync();
						log(connectionRole + "  sync done");
					}

					byte byteSent = NO_BYTE;
					if (bitsReceived != 0 || connectionRole == SENDER) {
						byteSent = sendBit(outgoingData, bitsSent);

						log("Sent byte ["+bitsSent+"]: " + previousByteSent);
					}

					try {
						byte receivedByte = NO_BYTE;
						if (bitsReceived < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9) {
							receivedByte = readBit();
						}
						// Succsefully exchanged a bit.
						
						if (byteSent != NO_BYTE) {
							previousByteSent = byteSent;
							bitsSent++;
						}
						
						if (receivedByte != NO_BYTE) {
							incomingData.set(bitsReceived,
									extractBitFromInput(receivedByte) == 1);
							previousByteReceived = receivedByte;
							log("Read byte ["+bitsReceived+"]: " + previousByteReceived);
						}
						bitsReceived++;

						retry = false;
						
					} catch (TimeOutException e) {
						retry = true;
					}
				}
			} catch (InvalidByteTransitionException e) {
				// TODO restart exchangeframe?
				e.printStackTrace();
			}
			lastReceivedFrame = new FlaggedFrame(incomingData);
			log("Build received frame:  " + lastReceivedFrame.getPayload()
					+ "   from bits: " + incomingData);
		} else {
			log("Not ready to exchange frames yet.");
		}
	}

	private byte sendBit(BitSet2 outputData, int index) {
		byte byteToSend = adaptBitToPrevious(outputData.get(index));
		down.sendByte(byteToSend);
		return byteToSend;
	}

	private byte readBit() throws TimeOutException {
		byte input = down.readByte();
		long waitTime = 5000000000l + System.nanoTime();
		boolean timeout = false;
		while (!(input != previousByteReceived && input == down.readByte()
				&& input == down.readByte() && input == down.readByte())) {
			input = down.readByte();
			if (System.nanoTime() > waitTime) {
				timeout = true;
				log("Timeout on wait for ack loop!");
				throw new TimeOutException();
			}
		}
		return input;
	}

	private void waitForSync() {
		log("On line before sync: " + down.readByte());
		boolean lastToSend = false;

		log("Sending 3");
		down.sendByte((byte) 3);

		while (down.readByte() != 3) {
			// Wait on 11.
		}
		log("Read 3.");
		long waitTime = (long) (Math.random() * 1000000000) + System.nanoTime();
		while (System.nanoTime() < waitTime && !lastToSend) {
			byte input = down.readByte();
			if (input == 0 && checkStable(input, 20)){ 
					// First to see, last to send.
				connectionRole = SENDER;
				log("Assumed role: " + connectionRole);
				lastToSend = true;
				down.sendByte((byte) 1);
				// send ack.
				// Assume ok.
				previousByteSent = 1;
				previousByteReceived = 0;
			}
		}
		if (!lastToSend) {
			log("Wait over, sending now");
			// First to send, last to receive ack.
			connectionRole = RECEIVER;
			down.sendByte((byte) 0);
			long waitTill = System.nanoTime() + 1000000000l;
			byte input = down.readByte();
			while (!(input==1 && checkStable(input,2))){ 
					input = down.readByte();
				if (System.nanoTime() > waitTill) {
					log("Waited too long for other side, expecting desync");
					waitForSync();
					return;
				}
				// Wait on 1 (ack from first to see)
			}
			// assume ok.
			previousByteSent = 0;
			previousByteReceived = 1;
		}
		connectionSync = true;
		/*
		 * if (checkLineInUse()) { // Other end detected, got sync; return; }
		 * log("No other end detected...");
		 * 
		 * while (!connectionSync) { //log("Waiting on other end...");
		 * down.sendByte((byte) 1); try { Thread.sleep(20); } catch
		 * (InterruptedException e) { e.printStackTrace(); }
		 * down.sendByte((byte) 0); try { Thread.sleep(20); } catch
		 * (InterruptedException e) { e.printStackTrace(); } checkForResponse();
		 * }
		 */
	}
	
	
	public boolean checkStable(byte input, int amount){
		//TODO dit onderzoeken of hierdoor sync op errorLPT faalt.
		for(int i=0;i<amount;i++){
			if(input!=down.readByte()){
				return false;
			}
		}
		return true;
	}

	private byte adaptBitToPrevious(byte nextData) {
		if ((nextData & 1) == (previousByteSent & 1)) {
			// Same databit, different clockbit
			if ((previousByteSent & 2) == 2) { // Invert clockbit
				return (byte) (0 | (previousByteSent & 1));
			} else {
				return (byte) (2 | (previousByteSent & 1));
			}
		} else {
			// Different databit, same clockbit
			return (byte) ((nextData & 1) | previousByteSent & 2);
		}
	}

	private byte adaptBitToPrevious(boolean nextData) {
		if (nextData) {
			return adaptBitToPrevious((byte) 1);
		} else {
			return adaptBitToPrevious((byte) 0);
		}
	}

	private byte extractBitFromInput(byte input)
			throws InvalidByteTransitionException {
		if ((input & 1) == (previousByteReceived & 1)
				&& (input & 2) != (previousByteReceived & 2)) {
			// Both LSB are same, but it is still diferent > read LSB.
			return (byte) (input & 1);
		} else if ((input & 2) == (previousByteReceived & 2)) {
			// Both MSB are same, but it is still different >
			return (byte) (input & 1);
		} else {
			throw new InvalidByteTransitionException();
		}
	}

	public void sendFrame(FlaggedFrame data) {
		frameToSendNext = data;
		// log("Units to send next: "+Arrays.toString(data.payload.units));
		setFrameToSend = true;
	}

	public Frame readFrame() {
		readFrame = true;
		// log("readFrame():  "+Arrays.toString(lastReceivedFrame.getPayload().units));

		return lastReceivedFrame.getPayload().getClone();
	}

	public synchronized void log(String msg) {
		System.out.println(System.nanoTime() + "\t"
				+ Thread.currentThread().getId() + "\t" + msg);
		System.out.flush();
		System.err.flush();
	}

}

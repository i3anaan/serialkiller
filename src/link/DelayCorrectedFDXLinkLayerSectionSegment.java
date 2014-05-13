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

	int totalBytesSend = 0;
	int totalBytesReceived = 0;

	private String connectionRole = "unkown"; // Debug

	FlaggedFrame lastReceivedFrame = new FlaggedFrame();
	FlaggedFrame frameToSendNext = new FlaggedFrame();

	protected boolean readFrame;
	protected boolean setFrameToSend;

	public DelayCorrectedFDXLinkLayerSectionSegment(PhysicalLayer down) {
		this.down = down;
	}

	public void exchangeFrame() {
		// log("Exchaning frames!");
		if (readFrame && setFrameToSend) {
			readFrame = false;
			setFrameToSend = false;
			BitSet2 incomingData = new BitSet2();
			BitSet2 outgoingData = frameToSendNext.getBitSet();
			log("Frame to send: " + frameToSendNext + "   outgoing bits: "
					+ outgoingData.toString());
			int bitsReceived = 0;
			int bitsSent = 0;
			//01100101
			// connectionSync = false;
			try {
				while (bitsReceived < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9
						|| bitsSent < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9) {

					if (!connectionSync) {
						log(connectionRole + "  Setting up sync..");
						waitForSync();
						log(connectionRole + "  sync done");
						if(connectionRole.equals("First to receive"));{
							//incomingData.set(0,true);
							//incomingData.set(1,true);
							//bitsReceived = 1;
							//totalBytesReceived = 1;
							//log("Fixing offset");
							//TODO ugly offset fix;
							//offset seems random
						}
						log("PreviousByteReceived: "+previousByteReceived+", "+down.readByte());
						log("PreviousByteSent: "+previousByteSent);
					}

					byte byteToSend = adaptBitToPrevious(outgoingData
							.get(bitsSent));
					log("Previous byte sent: " + previousByteSent
					 + " Sending now: " + byteToSend);
					down.sendByte(byteToSend);
					

					byte input = down.readByte();
					long waitTime = 5000000000l + System.nanoTime();
					boolean timeout = false;
					// Only stop the loop if the input differs from the
					// previousByteReceived and the input has stabilised
					// (multiple read check).
					// OR if a timeout occurs.
					log("Going into wait on ack loop, current input: "+input+" previousByteReceived: "+previousByteReceived);
					while (!timeout
							&& !(input != previousByteReceived
									&& input == down.readByte()
									&& input == down.readByte()
									&& input == down.readByte())) {
						input = down.readByte();
						if (System.nanoTime() > waitTime) {
							timeout = true;
							log("Timeout on wait for ack loop!");
						}
						// log("Waiting for ack...");
					}
					int stability = 0;
					for(int i=0;i<100;i++){
						if(input!=down.readByte()){
							stability++;
						}
					}
					log("Signal stability: "+stability);
					
					
					
					if (!timeout) {
						log("No timeout!");
						log("["+bitsSent+"] Output: "+byteToSend+"  Previous Byte Send: "+previousByteSent);
						log("["+bitsReceived+"] Input:  "+input+"  Previous input: "+previousByteReceived);
						//No timeout occured, assume both sending and reading went oke.
						//Update some variables, and extract the received bit.
						
						bitsSent++;
						totalBytesSend++;
						previousByteSent = byteToSend;
						
						previousByteReceived = input;
						incomingData.set(bitsReceived,
								extractBitFromInput(input) == 1);

						bitsReceived++;
						totalBytesReceived++;
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

	private void waitForSync() {
		log("On line before sync: " + down.readByte());
		// down.sendByte((byte) 2);
		boolean lastToSend = false;

		log("Sending 3");
		down.sendByte((byte) 3);

		while (down.readByte() != 3) {
			// Wait on 11.
		}
		log("Read 3.");
		long waitTime = (long) (Math.random() * 1000000000) + System.nanoTime();
		// log("CurrentTime:  " + System.nanoTime() + "  Wait till:  " +
		// waitTime);
		while (System.nanoTime() < waitTime) {
			// log("Randomly delaying sending, while reading.");
			if (down.readByte() == 0) {
				// First to see, last to send.
				connectionRole = "First to send";
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
			connectionRole = "First to receive";
			down.sendByte((byte) 0);
			long waitTill = System.nanoTime() + 1000000000l;
			while (down.readByte() != 1) {
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

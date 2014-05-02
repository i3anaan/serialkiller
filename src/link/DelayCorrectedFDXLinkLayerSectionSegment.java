package link;

import phys.PhysicalLayer;
import util.Bytes;

/**
 * Do not use debouncer. Use CleanStart 0.
 * 
 * @author I3anaan
 * 
 */
public class DelayCorrectedFDXLinkLayerSectionSegment extends LinkLayer {

	boolean connectionSync;
	byte lastSentSyncPing = 0;
	byte previousByteSent = 0;
	byte previousByteReceived = 0;

	Frame lastReceivedFrame = new Frame((byte) 0);
	Frame frameToSendNext = new Frame((byte) 0);

	protected boolean readFrame;
	protected boolean setFrameToSend;

	public DelayCorrectedFDXLinkLayerSectionSegment(PhysicalLayer down) {
		this.down = down;
	}

	public void exchangeFrame() {
		if (readFrame && setFrameToSend) {
			readFrame = false;
			setFrameToSend = false;
			Frame incomingData = new Frame();
			try {
				while (!incomingData.isComplete()) {
					if (!connectionSync) {
						// waitForSync();
					}

					byte byteToSend = adaptBitToPrevious(frameToSendNext
							.nextBit());
					frameToSendNext.removeBit();
					// System.out.println("Byte to send: "+
					// Bytes.format(byteToSend));
					down.sendByte(byteToSend);
					previousByteSent = byteToSend;

					byte input = down.readByte();
					// while (input == previousByteReceived) {
					// input = down.readByte();
					// System.out.println("Waiting for ack...");
					// }
					// Found difference, got reaction;
					// Extract information out of response;
					previousByteReceived = input;

					// System.out.println("Exctraded input bit: "
					// +extractBitFromInput(input));
					incomingData.add(extractBitFromInput(input));
					// System.out.println(Bytes.format((byte)incomingData.getByte()));
				}

				System.out.println("Finished byte-exchange \n");

			} catch (InvalidByteTransitionException e) {
				// TODO iets hierop doen.
				// Unrecoverable biterror?
				// Should not happen, maybe just try again.
				e.printStackTrace();
			}

			lastReceivedFrame = incomingData.getFullLength();
		} else {
			System.out.println("Not ready to exchange frames yet.");
		}
	}

	private void waitForSync() {
		if (checkLineInUse()) {
			// Other end detected, got sync;
			return;
		}
		System.out.println("No other end detected...");

		while (!connectionSync) {
			System.out.println("Waiting on other end...");
			down.sendByte((byte) 1);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			down.sendByte((byte) 0);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			checkForResponse();
		}
	}

	private boolean checkLineInUse() {
		boolean lineInUse = false;
		for (int i = 0; i < 20; i++) {
			if (!lineInUse && down.readByte() != previousByteReceived) {
				lineInUse = true;
				System.out.println("Checking end: Reaction detected");
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//A problem that might occur here is that this will keep sending 2, till it gets a response.
		//If it doenst get it the first time, I dont think keeping sending is very effective.
		//But that would mean the line is in use by something unresponsive
		//and will not be usable till that sender gets removed from the line.
		while (lineInUse) {
			down.sendByte((byte) 2);
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // TODO: dit slimmer doen;
			if (down.readByte() == 2) {
				previousByteSent = 2;
				previousByteReceived = 2;
				connectionSync = true;
				System.out.println("Checking end: Connected!");
			}
		}
		
		return connectionSync;
	}

	private boolean checkForResponse() {
		if (down.readByte() != previousByteReceived) {
			System.out.println("Waiting end: Reaction detected!");
			down.sendByte((byte) 2);
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // TODO: dit slimmer doen;
			if (down.readByte() == 2) {
				previousByteSent = 2;
				previousByteReceived = 2;
				connectionSync = true;
				System.out.println("Waiting end: Connected!");
			}
		}
		return connectionSync;
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

	private byte extractBitFromInput(byte input)
			throws InvalidByteTransitionException {
		if ((input & 1) == (previousByteReceived & 1)
				&& (input & 2) != (previousByteReceived & 2)) {
			// Both LSB are same, but it is still diferent > read LSB.
			return (byte) (input & 1);
		} else if ((input & 2) == (previousByteReceived & 2)) {
			// Both LSB are not same, but it is still different >
			return (byte) (input & 1);
		} else {
			throw new InvalidByteTransitionException();
		}
	}

	@Override
	public void sendByte(byte data) {
		frameToSendNext = new Frame(data);
		setFrameToSend = true;
	}

	@Override
	public byte readByte() {
		readFrame = true;
		return lastReceivedFrame.getByte();
	}
}

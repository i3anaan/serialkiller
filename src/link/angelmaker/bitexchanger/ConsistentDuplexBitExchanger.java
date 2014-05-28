package link.angelmaker.bitexchanger;

import java.util.concurrent.ArrayBlockingQueue;

import phys.PhysicalLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.manager.AMManager;
import link.jack.Frame;
import util.BitSet2;

/**
 * Basicly JackTheRippers' bit exchange system, with some small adjustments. It
 * just passes a stream of bits, it does not know the meaning of them. Has some
 * very basic sync systems. Requires there to ALWAYS be data.
 * 
 * @author I3anaan
 * @Requires AMManger.Server
 */
public class ConsistentDuplexBitExchanger extends Thread implements
		BitExchanger.MasterSlave, BitExchanger.AlwaysSending {

	private AMManager.Server manager;
	private ArrayBlockingQueue<Boolean> queueOut;
	private ArrayBlockingQueue<Boolean> queueIn;
	private boolean connectionSync = false;
	public static byte NO_BYTE = -4;
	public static final long TIMEOUT_NO_NEW_BIT_NANO = 10 * 1000000l;
	public static final long TIMEOUT_PANIC_NO_NEW_BIT_NANO = 10 * 1000000l;
	public static final long TIMEOUT_PANIC_EXTRA_SIGNALS_NANO = 100 * 1000000l;
	public static final long TIMEOUT_PANIC_EXTRA_SIGNALS_NO_NEW_BIT_NANO = 1 * 1000000l;
	public static final long TIMEOUT_SYNC_PROCEDURE_DESYNC_NANO = 100 * 1000000l;
	public static final long RANGE_SYNC_RANDOM_WAIT_NANO = 100 * 1000000l;

	byte lastSentSyncPing = 0;
	byte previousByteSent = 0;
	byte previousByteReceived = 0;
	PhysicalLayer down;
	Frame lastReceivedFrame;
	Frame frameToSendNext;
	protected boolean readFrame = false;
	protected boolean setFrameToSend = false;

	public static final String MASTER = "master";
	public static final String SLAVE = "slave";

	private String connectionRole = "unkown";

	public ConsistentDuplexBitExchanger(PhysicalLayer down, AMManager manager) throws IncompatibleModulesException{
		this.down = down;
		queueOut = new ArrayBlockingQueue<Boolean>(1024);
		queueIn = new ArrayBlockingQueue<Boolean>(1024);
		if(manager instanceof AMManager.Server){
			this.manager = (AMManager.Server) manager;
		}else{
			throw new IncompatibleModulesException();
		}
		this.start();
	}
	
	
	@Override
	public void sendBits(BitSet2 bits) {
		for (int i = 0; i < bits.length(); i++) {
			queueOut.add(bits.get(i));
		}
	}

	@Override
	public void emptyQueue() {
		queueOut.clear();

	}

	@Override
	public BitSet2 readBits() {
		BitSet2 result = new BitSet2();
		Boolean bit = queueIn.poll();
		while (bit != null) {
			result.addAtEnd(bit);
			bit = queueIn.poll();
		}
		return result;
	}

	@Override
	public boolean isMaster() {
		return connectionRole.equals(MASTER);
	}

	@Override
	public boolean isSlave() {
		return connectionRole.equals(SLAVE);
	}

	public void run() {
		while (true) {
			boolean bitExchangeSuccesful = false;
			Boolean bitToSendNext = queueOut.poll();
			
			if (bitToSendNext == null) {
				sendBits(manager.getNextNode().getConverted());
			} else {
				while (!bitExchangeSuccesful) {
					try {
						if (!connectionSync) {
							waitForSync();
						}

						byte byteSent = NO_BYTE;
						if (connectionRole == MASTER) {
							byteSent = sendBit(bitToSendNext);
						}

						try {
							byte receivedByte = NO_BYTE;
							receivedByte = readByte();
							// Succsefully exchanged a bit.
							if (byteSent != NO_BYTE) {
								previousByteSent = byteSent;
							}
							if (receivedByte != NO_BYTE) {
								queueIn.add(extractBitFromInput(receivedByte) == 1);
								previousByteReceived = receivedByte;
							}
							bitExchangeSuccesful = true;
						} catch (TimeOutException e) {
							// Timedout in reading, try again (send same byte
							// again).
						}
					} catch (InvalidByteTransitionException e) {
						signalAndWaitOnInvalidByteTransition();
						waitForSync();
					}
				}
			}
		}
	}

	private byte sendBit(boolean bit) {
		byte byteToSend = adaptBitToPrevious(bit);
		down.sendByte(byteToSend);
		return byteToSend;
	}

	private byte readByte() throws TimeOutException {
		byte input = down.readByte();
		long waitTime = TIMEOUT_NO_NEW_BIT_NANO + System.nanoTime();
		while (!(getStableInput() == previousByteReceived)) {
			input = down.readByte();
			if (System.nanoTime() > waitTime) {
				throw new TimeOutException();
			}
		}
		return input;
	}

	public boolean checkStable(byte input, int amount) {
		for (int i = 0; i < amount; i++) {
			if (input != down.readByte()) {
				return false;
			}
		}
		return true;
	}

	public byte getStableInput() {
		byte in = down.readByte();
		while (!checkStable(in, 4)) {
			in = down.readByte();
		}
		return in;
	}

	public byte getNewStableInput() {
		byte in = getStableInput();
		while (in == previousByteReceived) {
			in = getStableInput();
		}
		return in;
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

	private void signalAndWaitOnInvalidByteTransition() {
		boolean state = false;
		int signals = 0;

		while (signals < 3) {
			byte in = -1;
			try {
				AngelMaker.logger.error("PANIC: Sending:"
						+ (state ? (byte) 1 : (byte) 2)
						+ "  signals received: " + signals);
				down.sendByte(state ? (byte) 1 : (byte) 2);
				state = !state;
				long maxTime = System.nanoTime()
						+ TIMEOUT_PANIC_NO_NEW_BIT_NANO;
				in = getStableInput();
				while (in == previousByteReceived
						&& maxTime > System.nanoTime()) {
					in = getStableInput();
				}
				if (in != previousByteReceived) {
					extractBitFromInput(in);
					previousByteReceived = in;
					signals = 0;
				} else {
					AngelMaker.logger.error("PANIC: Timout on waiting for ack");
				}

			} catch (InvalidByteTransitionException e) {
				signals++;
				previousByteReceived = in;
			}

		}
		int extraSignals = 100;
		long maxTime = System.nanoTime() + TIMEOUT_PANIC_EXTRA_SIGNALS_NANO;
		while (extraSignals > 0 && maxTime > System.nanoTime()) {
			down.sendByte(state ? (byte) 1 : (byte) 2);
			state = !state;
			byte in = getStableInput();
			long maxTimeNewInput = System.nanoTime()
					+ TIMEOUT_PANIC_EXTRA_SIGNALS_NO_NEW_BIT_NANO;
			while (in == previousByteReceived
					&& maxTimeNewInput > System.nanoTime()) {
				in = getStableInput();
			}
			previousByteReceived = in;
			extraSignals--;
		}
		if (maxTime < System.nanoTime()) {
			AngelMaker.logger
					.error("PANIC: Timeout on sending extra panic signals, amount sent: "
							+ (100 - extraSignals));
		}
		AngelMaker.logger.error("PANIC: Signal and wait procedure complete");
	}

	private void waitForSync() {
		boolean lastToSend = false;
		down.sendByte((byte) 3);
		byte inputOne = down.readByte();
		while (!(inputOne == 3 && checkStable(inputOne, 5))) {
			// Wait on 11.
			inputOne = down.readByte();
		}
		long waitTime = (long) (Math.random() * RANGE_SYNC_RANDOM_WAIT_NANO)
				+ System.nanoTime();
		while (System.nanoTime() < waitTime && !lastToSend) {
			byte inputTwo = down.readByte();
			if (inputTwo == 0 && checkStable(inputTwo, 4)) {
				// First to see, last to send.
				connectionRole = MASTER;
				lastToSend = true;
				down.sendByte((byte) 1);
				// send ack.
				// Assume ok.
				previousByteSent = 1;
				previousByteReceived = 0;
			}
		}
		if (!lastToSend) {
			connectionRole = SLAVE;
			down.sendByte((byte) 0);
			long waitTill = System.nanoTime()
					+ TIMEOUT_SYNC_PROCEDURE_DESYNC_NANO;
			byte input = down.readByte();
			while (!(input == 1 && checkStable(input, 4))) {
				input = down.readByte();
				if (System.nanoTime() > waitTill) {
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
	}
	

	@Override
	public String toString(){
		return "ConsistentDuplexBitExchanger";
	}
	

	public class TimeOutException extends Exception {

	}

	public class InvalidByteTransitionException extends Exception {

	}

}

package link.angelmaker.bitexchanger;

import java.util.concurrent.ArrayBlockingQueue;

import link.angelmaker.AngelMaker;
import link.angelmaker.AngelMakerConfig;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.manager.AMManager;
import phys.PhysicalLayer;
import util.BitSet2;
import util.EmptyBitSet2;

/**
 * Right bit is always the data bit. Left bit is toggled to indicate
 * differences. 
 * 
 * 01-00-10-00-01-11-10-00
 *  1- 0- 0- 0- 1- 1- 0- 0
 * 
 * Will never hang, might skip or drop bits.
 * 
 * 
 * 
 * @Requires AMManager.Server
 * 
 * @author I3anaan
 * 
 */
public class SimpleBitExchanger extends Thread implements BitExchanger,
		BitExchanger.AlwaysSending {
	/**
	 * Bits received.
	 */
	public ArrayBlockingQueue<Boolean> queueIn;
	private String connectionRole;
	PhysicalLayer down;
	AMManager.Server manager;
	public static final String ROLE_MASTER = "master";
	public static final String ROLE_SLAVE = "slave";
	public static final String ROLE_UKNOWN = "unkown";

	protected byte previousByteSent;
	protected byte previousByteReceived;

	private int indexToSendNext;
	private BitSet2 bitsToSend;

	public SimpleBitExchanger() {
		queueIn = new ArrayBlockingQueue<Boolean>(2048);
		this.connectionRole = ROLE_UKNOWN;
		this.bitsToSend = EmptyBitSet2.getInstance();
		this.indexToSendNext = 1; // Make higher than 0 to make sure the
									// emptyBitSet does not get send;
	}

	@Override
	public void givePhysicalLayer(PhysicalLayer down) {
		this.down = down;
	}

	@Override
	public void giveAMManager(AMManager manager) {
		if (manager instanceof AMManager.Server) {
			this.manager = (AMManager.Server) manager;
		} else {
			throw new IncompatibleModulesException();
		}
	}

	@Override
	public void enable() {
		if (this.down != null && this.manager != null) {
			this.start();
			AngelMaker.logger.info("Enabled: " + this.toString());
		} else {
			AngelMaker.logger
					.warning("Trying to start BitExchanger without setting the PhysicalLayer or the AMManager");
		}
	}

	@Override
	public void sendBits(BitSet2 bits) {
		throw new UnsupportedOperationException();
		/**
		 * Due to optimization and implementation of other parts of AngelMaker
		 * it turned out this method was completely unused. In fact we removed
		 * the queueing mechanism for furhter optimization. Thus this method now
		 * does nothing.
		 */
	}

	@Override
	/**
	 * @return All the bits that are currently in the received queue.
	 * Blocks till it reads atleast 1 bit.
	 */
	public BitSet2 readBits() {
		BitSet2 bits = new BitSet2();
		Boolean bit = null;
		try {
			bit = queueIn.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			AngelMaker.logger
					.warning("Interrupted while reading bit from SimpleBitExchanger.queueIn");
		}
		while (bit != null) {
			bits.addAtEnd(bit);
			bit = queueIn.poll();
		}
		return bits;
	}

	/**
	 * Checks if the given input is stable on the physical layer. This is done
	 * by requiring the read byte to be read again several times in a row.
	 * 
	 * @param input
	 *            Input to check
	 * @param amount
	 *            Amount of time it should be the same.
	 * @return Whether or not the given input was stable for the given amount of
	 *         times.
	 */
	public boolean checkStable(byte input, int amount) {
		for (int i = 0; i < amount; i++) {
			if (input != down.readByte()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Stability is delegated to the checkStable() method. This method checks if
	 * it is different that the previous byte read.
	 * 
	 * @return A stable new input
	 */
	public byte getStableInput() {
		byte in = down.readByte();
		while (!checkStable(in, AngelMakerConfig.STABILITY)) {
			in = down.readByte();
		}
		return in;
	}

	/**
	 * Start synchronization of both sides. Both sides need to be in this method
	 * to set up synchronization. This is used to determine connection roles and
	 * to assure both time start at the same time.
	 * 
	 * It is done by listening for a given byte a random amount of time. After
	 * this time expired it assumes the line is idle, and proceeds to start
	 * signalling. When however during this listening a reaction is gotten, an
	 * acknowledgement is sent. The signalling side knows the sync is completed
	 * from that acknowledgement. When it is signalling for to long, it assumes
	 * it hasnt gotten an initial reaction, and resets the procedure.
	 */
	private void waitForSync() {
		boolean lastToSend = false;
		down.sendByte((byte) 3);
		byte inputOne = getStableInput();
		while (!(inputOne == 3)) {
			// Wait on 11.
			inputOne = getStableInput();
		}
		long waitTime = (long) (Math.random() * AngelMakerConfig.SYNC_RANGE_WAIT)
				+ System.nanoTime();
		while (System.nanoTime() < waitTime && !lastToSend) {
			byte inputTwo = getStableInput();
			if (inputTwo == 0) {
				// First to see, last to send.
				connectionRole = ROLE_MASTER;
				lastToSend = true;
				down.sendByte((byte) 1);
				// send ack.
				// Assume ok.
				previousByteSent = 1;
				previousByteReceived = 0;
			}
		}
		if (!lastToSend) {
			connectionRole = ROLE_SLAVE;
			down.sendByte((byte) 0);
			long waitTill = System.nanoTime()
					+ AngelMakerConfig.SYNC_TIMEOUT_DESYNC;
			byte input = getStableInput();
			while (!(input == 1)) {
				input = getStableInput();
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
		AngelMaker.logger.debug("Initial sync procedure complete.");
	}

	/**
	 * Attempt to read a byte from the physical layer.
	 * 
	 * @return The last byte read from the physical layer.
	 * @throws TimeOutException
	 *             when it takes to long.
	 */
	private byte readByte() throws TimeOutException {
		byte input = getStableInput();
		long waitTime = AngelMakerConfig.READ_TIMEOUT_NO_ACK
				+ System.nanoTime();

		while (!(input != previousByteReceived)) {
			input = getStableInput();
			down.sendByte(previousByteSent);
			if (System.nanoTime() > waitTime) {
				throw new TimeOutException();
			}
		}
		return input;
	}

	/**
	 * Adapts the given bit to the previous sent byte, for the other side to be
	 * able to extract the original bit.
	 * 
	 * @param nextData
	 *            The next dataBit to be send.
	 * @return The byte representing this dataBit to be placed on the physical
	 *         layer.
	 */
	public byte adaptBitToPrevious(byte previousByte, boolean nextData) {
		return (byte) (((previousByte ^ 2) & -2) | (nextData ? 1 : 0));
	}

	/**
	 * Extracts the bits adapted by the other end.
	 * 
	 * @param input
	 *            The byte read from the physical layer.
	 * @return The data bit this byte represents.
	 */
	public boolean[] extractBitFromInput(byte previousByteReceived, byte input) {
		return new boolean[] { (input & 1) == 1 };
	}

	/**
	 * @return The next Byte to be set on the line.
	 */
	public byte getNextByteToSend() {
		return adaptBitToPrevious(previousByteSent, getNextBitToSend());
	}

	/**
	 * @return The next data bit to be send.
	 */
	protected boolean getNextBitToSend() {
		if (indexToSendNext < bitsToSend.length()) {
			indexToSendNext++;
			return bitsToSend.get(indexToSendNext - 1);
		} else {
			bitsToSend = manager.getNextBits();
			indexToSendNext = 1;
			return bitsToSend.get(0);
		}
	}

	/**
	 * Keeps exchanging bits with the other side. Blocks if the queue gets full,
	 * resulting in timeouts being created for the other side (as this side is
	 * unable to react) Needs a AMManager.Server to be able to send filler data.
	 */
	@Override
	public void run() {
		AngelMaker.logger.info("Starting Sync procedure");
		waitForSync();
		if (connectionRole.equals(ROLE_MASTER)
				|| connectionRole.equals(ROLE_SLAVE)) {
			AngelMaker.logger.info("Assumed " + connectionRole
					+ " in this connection.");
		} else {
			AngelMaker.logger.warning("Assumed " + connectionRole
					+ " in this connection.");
		}
		boolean firstRound = true;
		while (true) {
			// Send bit. (Slave skips this first time)
			if (!firstRound || connectionRole.equals(ROLE_MASTER)) {
				byte byteToSendNext = getNextByteToSend();
				down.sendByte(byteToSendNext);
				previousByteSent = byteToSendNext;
			}

			// Read bit. (Skips on timeout).
			boolean[] bitsReceived = null;
			try {
				byte receivedByte = readByte();
				bitsReceived = extractBitFromInput(previousByteReceived,
						receivedByte);
				previousByteReceived = receivedByte;
				firstRound = false;
			} catch (TimeOutException e) {
				// Time-out, ignore, moving, don't hang.
				// AngelMaker.logger.debug("Time out waiting on ack. Currently on line:"+Bytes.format(down.readByte()));
			}

			if (bitsReceived != null) {
				for (boolean b : bitsReceived) {
					try {
						queueIn.put(b);
					} catch (InterruptedException e) {
						AngelMaker.logger
								.alert("Interrupted while trying to put received bit in SimpleBitExchanger.queueIn");
						e.printStackTrace();
					}
				}
			}

		}
	}

	public class TimeOutException extends Exception {

	}

	@Override
	public String toString() {
		return "SimpleBitExchanger, using: " + down.toString();
	}
}

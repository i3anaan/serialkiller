package link.angelmaker.bitexchanger;

import java.util.concurrent.ArrayBlockingQueue;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.manager.AMManager;
import link.angelmaker.nodes.Node;
import phys.PhysicalLayer;
import util.BitSet2;
import util.Bytes;

/**
 * Right bit is always the data bit.
 * Left bit is toggled to indicate differences.
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
public class SimpleBitExchanger extends Thread implements BitExchanger, BitExchanger.AlwaysSending {

	private ArrayBlockingQueue<Boolean> queueOut;
	private ArrayBlockingQueue<Boolean> queueIn;
	private String connectionRole;
	PhysicalLayer down;
	AMManager.Server manager;
	public static final String MASTER = "master";
	public static final String SLAVE = "slave";
	public static final int STABILITY = 4;
	public static final long SYNC_RANGE_WAIT = 100l*1000000l;
	public static final long SYNC_TIMEOUT_DESYNC = 1000l*1000000l;
	public static final long READ_TIMEOUT_NO_ACK = 1000000l*1000000l;
	private byte previousByteSent;
	private byte previousByteReceived;
	
	public SimpleBitExchanger(PhysicalLayer down, AMManager manager){
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
		for(int i=0;i<bits.length();i++){
			queueOut.offer(bits.get(i));
		}
	}

	@Override
	public void emptyQueue() {
		queueOut.clear();
	}

	@Override
	public BitSet2 readBits() {
		BitSet2 bits = new BitSet2();
		Boolean bit = queueIn.poll();
		while(bit!=null){
			bits.addAtEnd(bit);
			bit = queueIn.poll();
		}
		return bits;
	}
	
	
	
	
	/**
	 * Checks if the given input is stable on the physical layer.
	 * @param input	Input to check
	 * @param amount	Amount of time it should be the same.
	 * @return Whether or not the given input was stable for the given amount of times.
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
	 * Repeatedly checks the physical layer till it is the same STABILITY times in a row.
	 * @return A stable input from the physical layer.
	 */
	public byte getStableInput() {
		byte in = down.readByte();
		while (!checkStable(in, STABILITY)) {
			in = down.readByte();
		}
		return in;
	}
	
	/**
	 * Start synchronization of both sides.
	 * Both sides need to be in this method to set up synchronization.
	 * Currently this is only used to determine connection roles.
	 */
	private void waitForSync() {
		boolean lastToSend = false;
		down.sendByte((byte) 3);
		byte inputOne = getStableInput();
		while (!(inputOne==3)) {
			// Wait on 11.
			inputOne = getStableInput();
		}
		long waitTime = (long) (Math.random() * SYNC_RANGE_WAIT)
				+ System.nanoTime();
		while (System.nanoTime() < waitTime && !lastToSend) {
			byte inputTwo = getStableInput();
			if (inputTwo==0) {
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
					+ SYNC_TIMEOUT_DESYNC;
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
	 * @return	The last byte read from the physical layer.
	 * @throws TimeOutException	when it takes to long.
	 */
	private byte readByte() throws TimeOutException {
		byte input = getStableInput();
		long waitTime = READ_TIMEOUT_NO_ACK + System.nanoTime();
		
		while (!(input != previousByteReceived)) {
			//TODO can theoretically hang here when trying to get a stable input.
			input = getStableInput();
			if (System.nanoTime() > waitTime) {
				throw new TimeOutException();
			}
		}
		return input;
	}
	
	
	
	
	/**
	 * @param nextData The next dataBit to be send.
	 * @return	The byte representing this dataBit to be placed on the physical layer.
	 */
	public byte adaptBitToPrevious(byte previousByte,boolean nextData) {
		return (byte)(((previousByte^2)&-2)|(nextData ? 1 : 0));
		//TODO test.
	}
	
	/**
	 * @param input The byte read from the physical layer.
	 * @return	The data bit this byte represents.
	 */
	public boolean extractBitFromInput(byte input){
		return (input&1)==1;
		//TODO test;
	}
	
	/**
	 * Keeps exchanging bits with the other side.
	 * Can never hang, will skip and drop bits if necessary.
	 * Needs a AMManager.Server to be able to send filler data.
	 */
	public void run(){
		waitForSync();
		AngelMaker.logger.info("Assumed "+connectionRole+" in this connection.");
		boolean firstRound = true;
		int round = 0;
		while(true){
			//Send bit. (Slave skips this first time)
			if(!firstRound || connectionRole.equals(MASTER)){
				Boolean sendNext = queueOut.poll();
				while(sendNext==null){
					Node requested = manager.getNextNode();
					this.sendBits(requested.getConverted());
					sendNext = queueOut.poll();
				}
				byte byteToSendNext = adaptBitToPrevious(previousByteSent,sendNext);
				down.sendByte(byteToSendNext);
				previousByteSent = byteToSendNext;
				if(round<20){
				AngelMaker.logger.debug("Send Bit: "+sendNext);
				}
			}
			
			
			//Read bit. (Skips on timeout).
			Boolean bitReceived = null;
			try {
				byte receivedByte = readByte();
				bitReceived = extractBitFromInput(receivedByte);
				previousByteReceived = receivedByte; //Currently unused.
				firstRound = false;					
			} catch (TimeOutException e) {
				//Time-out, ignore, moving, don't hang.
				AngelMaker.logger.debug("Time out waiting on ack.");
				//TODO instead of not adding a bit here, it might be usefull to still add a (guessed) bit.
				//This might reduce out of sync problems.
			}
			
			if(bitReceived!=null){		
				queueIn.offer(bitReceived);
				if(round<20){
				AngelMaker.logger.debug("Received Bit: "+bitReceived);
				}
				round++;
				//TODO what to do when overflow.
			}
			
			
					
		}
	}
	
	
	public class TimeOutException extends Exception{
		
	}
	
	public String toString(){
		return "SimpleBitExchanger, using: "+down.toString();
	}
}
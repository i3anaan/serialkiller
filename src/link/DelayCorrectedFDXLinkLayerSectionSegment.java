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
	PhysicalLayer down;

	private String connectionRole = "unkown"; // Debug

	FlaggedFrame lastReceivedFrame = new FlaggedFrame();
	FlaggedFrame frameToSendNext = new FlaggedFrame();

	public static final String SENDER = "sender";
	public static final String RECEIVER = "receiver";
	public static final byte NO_BYTE = -1;

	protected boolean readFrame;
	protected boolean setFrameToSend;
	
	//TODO experiment with timeout values
	public static final long TIMEOUT_NO_NEW_BIT_NANO = 10*1000000l;
	public static final long TIMEOUT_PANIC_NO_NEW_BIT_NANO= 10*1000000l;
	public static final long TIMEOUT_PANIC_EXTRA_SIGNALS_NANO= 100*1000000l;
	public static final long TIMEOUT_PANIC_EXTRA_SIGNALS_NO_NEW_BIT_NANO= 1*1000000l;
	public static final long TIMEOUT_SYNC_PROCEDURE_DESYNC_NANO = 100*1000000l;
	public static final long RANGE_SYNC_RANDOM_WAIT_NANO = 100*1000000l;
	
	private int framesStartedSending;
	private int framesCompleted;

	public DelayCorrectedFDXLinkLayerSectionSegment(PhysicalLayer down) {
		this.down = down;
	}

	public void exchangeFrame() {
		if (readFrame && setFrameToSend) {
			readFrame = false;
			setFrameToSend = false;
			BitSet2 incomingData = new BitSet2();
			BitSet2 outgoingData = frameToSendNext.getDataBitSet();
			//log("Frame to send: " + frameToSendNext + "   outgoing bits: "
			//		+ outgoingData.toString());
			int bitsReceived = 0;
			int bitsSent = 0;
			boolean retry = false; //TODO uitzoeken waar deze gebruikt had moeten worden.

			try {
				framesStartedSending++;
				while (bitsReceived < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9
						|| bitsSent < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT * 9) {
					//while it has bits to send or receive.

					if (!connectionSync) {
						//log(connectionRole + "  Setting up sync..");
						waitForSync();
						//log(connectionRole + "  sync done");
					}

					byte byteSent = NO_BYTE;
					if (bitsReceived != 0 || connectionRole == SENDER) {
						byteSent = sendBit(outgoingData, bitsSent);

						//log("Sent byte ["+bitsSent+"]: " + previousByteSent);
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
							//log("Read byte ["+bitsReceived+"]: " + previousByteReceived);
						}
						bitsReceived++;

						retry = false;
						
					} catch (TimeOutException e) {
						retry = true;
					}
				}
				framesCompleted++;
				log("Frame complete  ["+framesCompleted+"/"+framesStartedSending+"]");
			} catch (InvalidByteTransitionException e) {
				// TODO restart exchangeframe?
				//e.printStackTrace();
				//log("signaling panic");
				log("FRAME FAILED");
				signalAndWaitOnInvalidByteTransition();
				//log("signal and wait for panic complete, starting sync");
				waitForSync();
				//log("sync complete");
			}
			lastReceivedFrame = new FlaggedFrame(incomingData);
			
			//log("Build received frame:  " + lastReceivedFrame.getPayload()
			//		+ "   from bits: " + incomingData);
		} else {
			log("Not ready to exchange frames yet.");
		}
	}

	private void signalAndWaitOnInvalidByteTransition() {
		boolean state = false;
		int signals = 0;
		
		while(signals<3){
			byte in = -1;
			try {
				log("PANIC: Sending:"+(state ? (byte) 1 : (byte) 2)+"  signals received: "+signals);
				down.sendByte(state ? (byte) 1 : (byte) 2);
				state = !state;
				long maxTime = System.nanoTime()+TIMEOUT_PANIC_NO_NEW_BIT_NANO;
				in = getStableInput();
				while(in==previousByteReceived && maxTime>System.nanoTime()){
					in = getStableInput();
				}
				if(in!=previousByteReceived){
					extractBitFromInput(in);
					previousByteReceived = in;
					signals = 0;
				}else{
					log("PANIC: Timout on waiting for ack");
				}
				
			} catch (InvalidByteTransitionException e) {
				//Both sides seen invalidTransition.
				//TODO might need to check for this multiple times
				//591652698927615	8	Sending 3
				signals++;
				previousByteReceived = in;	
				//log("Signal got: "+in);
			}
			
		}
		int extraSignals = 100;
		long maxTime = System.nanoTime()+TIMEOUT_PANIC_EXTRA_SIGNALS_NANO;
		while(extraSignals>0 && maxTime>System.nanoTime()){
			down.sendByte(state ? (byte) 1 : (byte) 2);
			state = !state;
			byte in = getStableInput();
			long maxTimeNewInput = System.nanoTime()+TIMEOUT_PANIC_EXTRA_SIGNALS_NO_NEW_BIT_NANO;
			while(in==previousByteReceived && maxTimeNewInput>System.nanoTime()){
				in = getStableInput();
			}
			previousByteReceived = in;
			extraSignals--;
		}
		if(maxTime<System.nanoTime()){
			log("PANIC: Timeout on sending extra panic signals, amount sent: "+(100-extraSignals));
		}
		log("PANIC: Signal and wait procedure complete");
	}

	private byte sendBit(BitSet2 outputData, int index) {
		byte byteToSend = adaptBitToPrevious(outputData.get(index));
		down.sendByte(byteToSend);
		return byteToSend;
	}

	private byte readBit() throws TimeOutException {
		byte input = down.readByte();
		long waitTime = TIMEOUT_NO_NEW_BIT_NANO + System.nanoTime();
		while (!(input != previousByteReceived && input == down.readByte()
				&& input == down.readByte() && input == down.readByte())) {
			input = down.readByte();
			if (System.nanoTime() > waitTime) {
				log("READBIT: Timeout on wait for ack loop!");
				throw new TimeOutException();
			}
		}
		return input;
	}

	private void waitForSync() {
		//log("On line before sync: " + down.readByte());
		boolean lastToSend = false;

		//log("Sending 3");
		down.sendByte((byte) 3);
		byte inputOne = down.readByte();
		while (!(inputOne == 3 && checkStable(inputOne,5))) {
			// Wait on 11.
			inputOne = down.readByte();
		}
		//log("Read 3.");
		long waitTime = (long) (Math.random() * RANGE_SYNC_RANDOM_WAIT_NANO) + System.nanoTime();
		while (System.nanoTime() < waitTime && !lastToSend) {
			byte inputTwo = down.readByte();
			if (inputTwo == 0 && checkStable(inputTwo, 4)){ 
					// First to see, last to send.
				connectionRole = SENDER;
				//log("Assumed role: " + connectionRole);
				lastToSend = true;
				down.sendByte((byte) 1);
				// send ack.
				// Assume ok.
				previousByteSent = 1;
				previousByteReceived = 0;
			}
		}
		if (!lastToSend) {
			//log("Wait over, sending now");
			// First to send, last to receive ack.
			connectionRole = RECEIVER;
			down.sendByte((byte) 0);
			long waitTill = System.nanoTime() + TIMEOUT_SYNC_PROCEDURE_DESYNC_NANO;
			byte input = down.readByte();
			while (!(input==1 && checkStable(input,4))){ 
					input = down.readByte();
				if (System.nanoTime() > waitTill) {
					log("SYNC: Waited too long for other side, expecting desync");
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
	
	
	public boolean checkStable(byte input, int amount){
		//TODO dit onderzoeken of hierdoor sync op errorLPT faalt.
		for(int i=0;i<amount;i++){
			if(input!=down.readByte()){
				return false;
			}
		}
		return true;
	}
	
	
	public byte getStableInput(){
		byte in = down.readByte();
		while(!checkStable(in,4)){
			in = down.readByte();
		}
		return in;
	}
	public byte getNewStableInput(){
		byte in = getStableInput();
		while(in==previousByteReceived){
			in = getStableInput();
		}
		//TODO maybe move setting previousByteReceived to here?
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

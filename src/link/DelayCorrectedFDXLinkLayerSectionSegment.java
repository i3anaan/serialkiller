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
	
	private String connectionRole = "unkown";	//Debug

	FlaggedFrame lastReceivedFrame = new FlaggedFrame();
	FlaggedFrame frameToSendNext = new FlaggedFrame();

	protected boolean readFrame;
	protected boolean setFrameToSend;

	public DelayCorrectedFDXLinkLayerSectionSegment(PhysicalLayer down) {
		this.down = down;
	}

	public void exchangeFrame() {
		//log("Exchaning frames!");
		if (readFrame && setFrameToSend) {
			readFrame = false;
			setFrameToSend = false;
			BitSet2 incomingData = new BitSet2();
			BitSet2 outgoingData = frameToSendNext.getBitSet();
			//log(Thread.currentThread().getId()+"  Frame to send: "+frameToSendNext+"   outgoing bits: "+outgoingData.toString());
			int bitsReceived = 0;
			int bitsSent = 0;
			try {
				while (bitsReceived < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT*9
						|| bitsSent < FlaggedFrame.FLAGGED_FRAME_UNIT_COUNT*9) {
					
					if (!connectionSync) {
						log(connectionRole+"  Setting up sync..");
						waitForSync();
						log(connectionRole+"  sync done");
					}
					
					
					byte byteToSend = adaptBitToPrevious(outgoingData
							.get(bitsSent));
					log("Previous byte sent: "+previousByteSent+" Sending now: "+byteToSend);
					down.sendByte(byteToSend);
					bitsSent++;
					totalBytesSend++;
					log("Total sent: "+totalBytesSend);
					previousByteSent = byteToSend;

					byte input = down.readByte();
					//log("Going in loop");
					if(input!=previousByteReceived){
						//log("Other side was waiting, ack already found: "+input+"  PreviousByteReceived: "+previousByteReceived);
					}
					while (input == previousByteReceived) {
						input = down.readByte();
						//log("Waiting for ack...");
					}
					log("Difference found! current: "+input+" Previous: "+previousByteReceived);
					log("Got ack, continueing.");
					// Found difference, got reaction;
					// Extract information out of response;
					previousByteReceived = input;

					incomingData.set(bitsReceived,
							extractBitFromInput(input) == 1);

					bitsReceived++;
					totalBytesReceived++;
					log("Total received: "+totalBytesReceived);
					//log("TotalSend:  "+totalBytesSend+" TotalReceived:  "+totalBytesReceived);
					/*try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				}
			} catch (InvalidByteTransitionException e) {
				// TODO restart exchangeframe?
				e.printStackTrace();
			}
			lastReceivedFrame = new FlaggedFrame(incomingData);
			log(Thread.currentThread().getId()+" Build received frame:  "+lastReceivedFrame +"   from bits: "+incomingData);
		} else {
			log("Not ready to exchange frames yet.");
		}
	}

	private void waitForSync() {
		log("On line before sync: "+down.readByte());
		boolean lastToSend = false;
		
		log("Sending 3");
		down.sendByte((byte)3);
		
		while(down.readByte()!=3){
			//Wait on 11.
		}
		log("Read 3.");
		long waitTime = (long)(Math.random()*1000000000) + System.nanoTime();
		log("CurrentTime:  "+System.nanoTime()+"  Wait till:  "+waitTime);
		while(System.nanoTime()<waitTime){
			//log("Randomly delaying sending, while reading.");
			if(down.readByte()==0){
				//First to see, last to send.
				connectionRole = "First to send";
				lastToSend = true;
				down.sendByte((byte)1);
				//send ack.
				//Assume ok.
				previousByteSent = 1;
				previousByteReceived = 0;
			}
		}
		if(!lastToSend){
			//First to send, last to receive ack.
			connectionRole = "First to receive";
			down.sendByte((byte)0);
			while(down.readByte()!=1){
				//Wait on 1 (ack from first to see)
			}
			//assume ok.
			previousByteSent = 0;
			previousByteReceived = 1;
		}
		connectionSync = true;
		/*
		if (checkLineInUse()) {
			// Other end detected, got sync;
			return;
		}
		log("No other end detected...");

		while (!connectionSync) {
			//log("Waiting on other end...");
			down.sendByte((byte) 1);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			down.sendByte((byte) 0);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			checkForResponse();
		}
		*/
	}

	private boolean checkLineInUse() {
		boolean lineInUse = false;
		for (int i = 0; i < 20+(Math.pow(Math.random()*20,2)); i++) {
			if (!lineInUse && down.readByte() != previousByteReceived) {
				lineInUse = true;
				log("Checking end: Reaction detected");
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// A problem that might occur here is that this will keep sending 2,
		// till it gets a response.
		// If it doenst get it the first time, I dont think keeping sending is
		// very effective.
		// But that would mean the line is in use by something unresponsive
		// and will not be usable till that sender gets removed from the line.
		while (lineInUse && !connectionSync) {
			down.sendByte((byte) 2);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // TODO: dit slimmer doen;
			if (down.readByte() == 2) {
				previousByteSent = 2;
				previousByteReceived = 2;
				connectionSync = true;
				log("Checking end: Connected!");
				connectionRole = "Client";
			}
		}

		return connectionSync;
	}

	private boolean checkForResponse() {
		if (down.readByte() != previousByteReceived) {
			//log("Waiting end: Reaction detected!");
			down.sendByte((byte) 2);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // TODO: dit slimmer doen;
			if (down.readByte() == 2) {
				previousByteSent = 2;
				previousByteReceived = 2;
				connectionSync = true;
				log("Waiting end: Connected!");
				connectionRole = "Server";
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
			// Both LSB are not same, but it is still different >
			return (byte) (input & 1);
		} else {
			throw new InvalidByteTransitionException();
		}
	}

	public void sendFrame(FlaggedFrame data) {
		frameToSendNext = data;
		//log("Units to send next: "+Arrays.toString(data.payload.units));
		setFrameToSend = true;
	}

	public Frame readFrame() {
		readFrame = true;
		//log("readFrame():  "+Arrays.toString(lastReceivedFrame.getPayload().units));
		
		return lastReceivedFrame.getPayload().getClone();
	}
	
	public synchronized void log(String msg){
		if(Thread.currentThread().getId()==12){
			System.err.println(System.nanoTime()+"\t"+Thread.currentThread().getId()+"\t"+msg);
		}else{
			System.out.println(System.nanoTime()+"\t"+Thread.currentThread().getId()+"\t"+msg);
		}
		System.out.flush();	
		System.err.flush();	
	}
}

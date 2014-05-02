package link;

import phys.PhysicalLayer;

/*
public class DelayCorrectedFDXLinkLayerSectionSegment extends LinkLayer{
	
	boolean connectionSync;
	byte lastSentSyncPing = 0;
	byte previousDataSent = 0;
	byte previousDataReceived = 0;
	
	Frame lastReceivedFrame;
	Frame frameToSendNext;
	
	boolean readFrame;
	boolean setFrameToSend;
	
	
	public DelayCorrectedFDXLinkLayerSectionSegment(PhysicalLayer down){
		this.down = down;
	}
	
	
	public void exchangeFrame(){
		if(readFrame && setFrameToSend){
			readFrame = false;
			setFrameToSend = false;
			Frame incomingData;
			while(!incomingData.isComplete()){
				System.out.println("Waiting on other end...");
				down.sendByte(adaptBitToPrevious(frameToSendNext.nextBit()));
				Thread.sleep(100);
				//Sync nodig
				byte input = down.readByte();
				if(input!=previousDataReceived){
					//Found difference, got reaction;
					//Extract information out of response;
					frameToSendNext.removeBit();
					incomingData.add(extractBitFromInput(input));
				}
			}
			
			lastReceivedFrame = Frame.getData();
		}else{
			System.out.println("Not ready to exchange frames yet.");
		}
	}
	
	private byte adaptBitToPrevious(byte nextData){
		if((nextData&1) == (previousDataSent&1)){
			//Same databit, different clockbit
			if((previousDataSent&2)==2){ //Invert clockbit
				return (byte)(0|(previousDataSent&1));
			}else{
				return (byte)(2|(previousDataSent&1));
			}
		}else{
			//Different databit, same clockbit
			return (byte)((nextData&1) | previousDataSent&2);
		}
	}
	
	private byte extractBitFromInput(byte input) throws InvalidByteTransitionException{
		if((input&1) == (previousDataReceived&1) && (input&2) != (previousDataReceived&2)){
			//Both LSB are same, but it is still diferent > read LSB.
			return (byte)(input&1);
		}else if((input&2) == (previousDataReceived&2)){
			//Both LSB are not same, but it is still different > 
			return (byte)(input&1);
		}else{
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
*/

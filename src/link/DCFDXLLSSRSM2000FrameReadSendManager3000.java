package link;

import java.util.ArrayList;

import util.ByteArrays;

public class DCFDXLLSSRSM2000FrameReadSendManager3000 extends FrameLinkLayer{

	DCFDXLLSSReadSendManager2000 down;
	
	
	public DCFDXLLSSRSM2000FrameReadSendManager3000(DCFDXLLSSReadSendManager2000 down){
		this.down = down;
	}
	
	@Override
	public void sendFrame(byte[] data) {
		for(byte b : data){
			down.sendByte(b);
		}
		down.sendUnit(new Unit(Unit.FLAG_END_OF_FRAME));
	}

	@Override
	public byte[] readFrame() {
		ArrayList<Byte> dataFrame = new ArrayList<Byte>();
		boolean frameComplete = false;
		while(frameComplete){
			Unit u = down.readUnit();
			if(!u.isSpecial){//Is data
				dataFrame.add(u.b);
			}else if(u.isEndOfFrame()){
				frameComplete = true;
			}
		}
		return ByteArrays.fromList(dataFrame);
	}
	

}

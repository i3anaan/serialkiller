package link.jack;

import java.util.ArrayList;

import link.FrameLinkLayer;
import util.BitSet2;
import util.ByteArrays;
import util.encoding.HammingCode;

public class JackTheRipper extends FrameLinkLayer{

	public static final Unit UNIT_IN_USE = new HammingUnit(ByteArrays.toBitSet(new byte[]{HammingUnit.FLAG_FILLER_DATA}),true, new HammingCode(4));
	public static final HammingCode hc = new HammingCode(4);
	
	
	DCFDXLLSSReadSendManager2000 down;
	
	
	public JackTheRipper(DCFDXLLSSReadSendManager2000 down){
		this.down = down;
	}
	
	@Override
	public void sendFrame(byte[] data) {
		BitSet2 dataAsBitSet = ByteArrays.toBitSet(data);
		for(int i=0;i<data.length*8;i=i+4){
			down.sendUnit(new HammingUnit(dataAsBitSet.get(i, i+3),hc));
		}
		down.sendUnit(new HammingUnit(HammingUnit.FLAG_END_OF_FRAME,hc));
	}

	@Override
	public byte[] readFrame() {
		ArrayList<Byte> dataFrame = new ArrayList<Byte>();
		boolean frameComplete = false;
		while(frameComplete){
			HammingUnit u1 = (HammingUnit) down.readUnit();
			HammingUnit u2 = (HammingUnit) down.readUnit();
			BitSet2 fullByte = BitSet2.concatenate(u2.getDecodedPayloadAsBitSet2(),u1.getDecodedPayloadAsBitSet2());
			
			if(!u1.isSpecial() != !u1.isSpecial()){//Error
				//TODO ERROR, out of sync.
			}else if(!u1.isSpecial() && !u1.isSpecial()){//Data
				dataFrame.add(fullByte.toByteArray()[0]);
			}else if(u1.getDecodedPayload()==u2.getDecodedPayload() && u1.isEndOfFrame()){ //flag
				frameComplete = true;
			}
		}
		return ByteArrays.fromList(dataFrame);
	}
}

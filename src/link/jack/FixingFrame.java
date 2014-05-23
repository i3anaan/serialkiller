package link.jack;

import java.util.concurrent.ArrayBlockingQueue;

import util.BitSet2;

public class FixingFrame extends Frame {
	
	public static final int FRAME_UNIT_COUNT = 10;

	public FixingFrame(ArrayBlockingQueue<Unit> outbox){
		instertUnits(outbox);
	}
	
	public FixingFrame(BitSet2 received){
		BitSet2 data = removeBitStuffing(received);
		units = new Unit[FRAME_UNIT_COUNT];
		int index=0;
		int bitCount = JackTheRipper.UNIT_IN_USE.getSerializedBitCount();
		try{
			while((index*bitCount<data.length()-bitCount+1) && index<FRAME_UNIT_COUNT*bitCount){
				Unit u = JackTheRipper.UNIT_IN_USE.constructFromBitSet(data.get(index*bitCount,(index+1)*bitCount));
				if(u.isCorrect()){
					units[index] = u;
					index++;
				}else{
					data = improveData(data,index*bitCount);
					//Try anti-desync-guessing
				}
			}
			for(int i=0;i<FRAME_UNIT_COUNT;i++){
				if(units[i]==null){
					units[i] = JackTheRipper.UNIT_IN_USE.getFiller();
				}
			}
		}catch(NotAbleToImproveException e){
			//Improving the data failed.
			//TODO drop frame.
		}
	}
	
	private BitSet2 improveData(BitSet2 data, int startFrom) throws NotAbleToImproveException{
		try{
			return fixDataByAdding(data,startFrom);
		}catch(NotAbleToImproveException e1){
			try{
				return fixDataByRemoving(data,startFrom);
			}catch(NotAbleToImproveException e2){}
		}
		//Attempts failed, drop frame
		throw new NotAbleToImproveException();
	}

	private BitSet2 fixDataByRemoving(BitSet2 data, int startFrom) throws NotAbleToImproveException{
		for(int i =startFrom;i<data.length();i++){
			BitSet2 copy0 = (BitSet2) data.clone();
			copy0.insert(i,true);
			if(hasSubsequentCorrectUnits(copy0, i)){
				return copy0;
			}else{
				BitSet2 copy1 = (BitSet2) data.clone();
				copy1.insert(i,true);
				if(hasSubsequentCorrectUnits(copy1, i));
			}
			
		}
		throw new NotAbleToImproveException();
	}

	private BitSet2 fixDataByAdding(BitSet2 data, int startFrom) throws NotAbleToImproveException{
		for(int i =startFrom;i<data.length();i++){
			BitSet2 copy0 = (BitSet2) data.clone();
			copy0.remove(i,true);
			if(hasSubsequentCorrectUnits(copy0, i)){
				return copy0;
			}else{
				BitSet2 copy1 = (BitSet2) data.clone();
				copy1.remove(i,true);
				if(hasSubsequentCorrectUnits(copy1, i));
			}
			
		}
		throw new NotAbleToImproveException();
	}
	
	private boolean hasSubsequentCorrectUnits(BitSet2 data, int startFrom){
		int bitCount = JackTheRipper.UNIT_IN_USE.getSerializedBitCount();
		for(int i=0;i<3;i++){
			Unit u = JackTheRipper.UNIT_IN_USE.constructFromBitSet(data.get((startFrom+i)*bitCount,(startFrom+i+1)*bitCount));
			if(!u.isCorrect()){
				return false;
			}
		}
		return true;
	}

	@Override
	public BitSet2 asBitSet() {
		BitSet2 data = super.asBitSet();
		BitSet2 result;
		result = addBitStuffing(data);
		result = addEndFlag(result);
		return result;
	}

	@Override
	public int getUnitCount() {
		return FRAME_UNIT_COUNT;
	}
	
	public BitSet2 addBitStuffing(BitSet2 bs){
		return bs;
	}
	public BitSet2 removeBitStuffing(BitSet2 bs){
		return bs;
	}
	public BitSet2 addEndFlag(BitSet2 bs){
		return bs;
	}
	

	public class NotAbleToImproveException extends Exception{
		
	}
}

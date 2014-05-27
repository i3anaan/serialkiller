package link.jack;

import java.util.concurrent.ArrayBlockingQueue;

import util.BitSet2;

public class FixingFrame extends Frame {
	
	public static final int FRAME_UNIT_COUNT = 10;
	public static final BitSet2 FLAG_END_OF_FRAME = new BitSet2(new boolean[]{true,true,true,true,true,true,true,true,true,true}); 

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
			e.printStackTrace();
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
				BitSet2 copy1 = (BitSet2) copy0.clone();
				copy1.flip(i);
				if(hasSubsequentCorrectUnits(copy1, i));
			}
			
		}
		throw new NotAbleToImproveException();
	}

	private BitSet2 fixDataByAdding(BitSet2 data, int startFrom) throws NotAbleToImproveException{
		for(int i =startFrom;i<data.length();i++){
			BitSet2 copy0 = (BitSet2) data.clone();
			copy0.remove(i);
			if(hasSubsequentCorrectUnits(copy0, i)){
				return copy0;
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
	
	@Override
	public int getFullBitCount(){
		return getUnitCount()*JackTheRipper.UNIT_IN_USE.getSerializedBitCount() + FLAG_END_OF_FRAME.length();
	}
	@Override
	public int getMinimumBitCount() {
		return getFullBitCount() - FLAG_END_OF_FRAME.length()/2;
	}
	
	public static Frame getDummy() {
		return new FixingFrame(new BitSet2());
	}
	
	/**
	 * Adds bitstuffing to escape the FLAG_END_OF_FRAME
	 * Returns a new BitSet2 with the stuffing.
	 */
	public static BitSet2 addBitStuffing(BitSet2 bs){
		int index = 0;
		BitSet2 result = (BitSet2)bs.clone();
		while(index<=result.length()-FLAG_END_OF_FRAME.length()){
			if(result.get(index,index+FLAG_END_OF_FRAME.length()).equals(FLAG_END_OF_FRAME)){
				result.insert(index+FLAG_END_OF_FRAME.length()-1, !FLAG_END_OF_FRAME.get(FLAG_END_OF_FRAME.length()-1));
			}
			index++;
		}
		
		return result;
	}
	
	/**
	 * Removes bitstuffing (as done by addBitStuffing()).
	 * Returns a new BitSet2 containing the data without bitstuffing.
	 * @param bs
	 * @return
	 */
	public static BitSet2 removeBitStuffing(BitSet2 bs){
		int index = 0;
		BitSet2 result = (BitSet2)bs.clone();
		BitSet2 flag = FLAG_END_OF_FRAME.get(0,FLAG_END_OF_FRAME.length()-1);
		while(index<=result.length()-FLAG_END_OF_FRAME.length()-1){//1 lower, since the flaglike data is now length flag+1
			if(result.get(index,index+FLAG_END_OF_FRAME.length()-1).equals(flag)){
				result.remove(index+FLAG_END_OF_FRAME.length()-1);
				index = index+FLAG_END_OF_FRAME.length();
			}else{
			index++;
			}
		}
		return result;
	}
	/**
	 * Adds the end flag to a bitset.
	 * Returns a new BitSet2 consisting of the original + flag.
	 */
	public static BitSet2 addEndFlag(BitSet2 bs){
		return BitSet2.concatenate(bs, FLAG_END_OF_FRAME);
	}

	public class NotAbleToImproveException extends Exception{
		
	}
	
}

package link.jack;

import java.util.concurrent.ArrayBlockingQueue;

import util.BitSet2;

public abstract class Frame {
	
	protected Unit[] units;
	
	public void instertUnits(ArrayBlockingQueue<Unit> outbox){
		units = new Unit[getUnitCount()];
		int added= 0;
		while(added<getUnitCount()){
			Unit u = outbox.poll();
			if(u!=null){
				units[added] = u;				
			}else{
				units[added] = JackTheRipper.UNIT_IN_USE.getFiller();
			}
			added++;
		}
	}
	
	public Unit[] getUnits(){
		return units;
	}
	
	public BitSet2 asBitSet(){
		BitSet2 result = new BitSet2();
		for(int i=0;i<getUnitCount();i++){
			result = BitSet2.concatenate(result, units[i].serializeToBitSet());
		}
		return result;
	}

	
	public String toString(){
		String s = "["+units[0];
		for(int i=1;i<units.length;i++){
			s = s+", "+units[i];
		}
		return s;
	}
	public abstract int getUnitCount();
	
	public int minimumBits() {
		return getUnitCount()*JackTheRipper.UNIT_IN_USE.getSerializedBitCount()-5;
	}

}

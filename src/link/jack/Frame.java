package link.jack;

import util.BitSet2;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Sets starting from the left (MSB), reads starting from the right (LSB).
 * 
 * @author I3anaan
 * 
 */
public class Frame {

	private Unit[] units = new Unit[FRAME_UNIT_COUNT];

	public static final int FRAME_UNIT_COUNT = 10;
	
	/**
	 * Constructs a frame, taking as much Units from the queue as possible.
	 * @param outbox
	 */
	public Frame(ArrayBlockingQueue<Unit> outbox){
		int added= 0;
		while(added<FRAME_UNIT_COUNT){
			Unit u = outbox.poll();
			if(u!=null){
				units[added] = u;				
			}else{
				units[added] = JackTheRipper.UNIT_IN_USE.getFiller();
			}
			added++;
		}
	}
	
	/**
	 * Constructs a Units from the received data, and saves those in this frame.
	 * @param received
	 */
	public Frame(BitSet2 received){
		int index=0;
		int bitCount = JackTheRipper.UNIT_IN_USE.getSerializedBitCount();
		while((index*bitCount<received.length()-bitCount+1) && index<FRAME_UNIT_COUNT*bitCount){
			units[index] = JackTheRipper.UNIT_IN_USE.constructFromBitSet(received.get(index*bitCount,(index+1)*bitCount));
			index++;
		}
		for(int i=0;i<FRAME_UNIT_COUNT;i++){
			if(units[i]==null){
				units[i] = JackTheRipper.UNIT_IN_USE.getFiller();
			}
		}
	}
	
	public Unit[] getUnits(){
		return units;
	}
	
	public BitSet2 asBitSet(){
		BitSet2 result = new BitSet2();
		for(int i=0;i<FRAME_UNIT_COUNT;i++){
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
}

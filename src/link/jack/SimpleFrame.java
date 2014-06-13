package link.jack;

import util.BitSet2;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Sets starting from the left (MSB), reads starting from the right (LSB).
 * 
 * @author I3anaan
 * 
 */
public class SimpleFrame extends Frame{
	public static final int FRAME_UNIT_COUNT = 10;
	
	/**
	 * Constructs a frame, taking as much Units from the queue as possible.
	 * @param outbox
	 */
	public SimpleFrame(ArrayBlockingQueue<Unit> outbox){
		instertUnits(outbox);
	}
	
	/**
	 * Constructs a Units from the received data, and saves those in this frame.
	 * @param received
	 */
	public SimpleFrame(BitSet2 received){
		units = new Unit[FRAME_UNIT_COUNT];
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

	@Override
	public int getUnitCount() {
		return FRAME_UNIT_COUNT;
	}

	@Override
	public int minimumBits() {
		return getUnitCount()*JackTheRipper.UNIT_IN_USE.getSerializedBitCount()-5;
	}
}

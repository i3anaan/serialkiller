package link;

import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

import util.BitSets;
import util.ByteArrays;
import util.Bytes;

/**
 * Sets starting from the left (MSB), reads starting from the right (LSB).
 * 
 * @author I3anaan
 * 
 */
public class Frame {

	protected Unit[] units = new Unit[PAYLOAD_UNIT_COUNT];

	public static final int PAYLOAD_UNIT_COUNT = 10;
	//public static final int PAYLOAD_SIZE_BITS = PAYLOAD_SIZE_BYTES * 8;

	// Imagine this as a pointer pointing to the end of the bit sequence.
	// To the left is the last placed, valid, bit.
	// To the right a new bit would be placed;
	// XXXXXXXXXXXXX ---------
	// -------------^---------
	// -------------|---------
	int currentReaderIndex = 0;

	public Frame() {
		for(int i=0;i<units.length;i++){
			units[i] = new Unit(Unit.FLAG_FILLER_DATA);
		}
	}
	
	public Frame(Unit[] units) {
		for(int i=0;i<units.length;i++){
			this.units[i] = units[i];
		}
		//Fill empty spots with Filler data flags.
		for(int i=0;i<units.length;i++){
			if(units[i]==null){
				units[i] = new Unit(Unit.FLAG_FILLER_DATA);
			}
		}
	}

	public Frame(BitSet data) throws FrameSizeTooSmallException {
		//Put as much data as possible in units.
		for(int i=0;i<data.size()-7;i=i+8){
			units[i/8] = new Unit(Bytes.fromBitSet(data, i));
		}
		//Fill empty spots with Filler data flags.
		for(int i=0;i<units.length;i++){
			if(units[i]==null){
				units[i] = new Unit(Unit.FLAG_FILLER_DATA);
			}
		}
	}
	
	/**
	 * Returns only the data contained in this Frame.
	 * (Flags and stuffings are filtered out).
	 * @return
	 */
	public BitSet getBitSet(){
		BitSet result = new BitSet();
		for(Unit u : units){
				result = BitSets.concatenate(result, u.dataAsBitSet());
		}
		return result;
	}

	public Unit getUnit(int unitIndex) {
		return units[unitIndex];
	}

	public Frame getClone() {
		Unit[] newUnits = new Unit[PAYLOAD_UNIT_COUNT];
		for(int i=0;i<units.length;i++){
			newUnits[i] = new Unit(units[i].b);
		}
		return new Frame(newUnits);
	}
}

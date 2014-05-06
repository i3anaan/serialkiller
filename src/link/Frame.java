package link;

import java.util.Arrays;

import util.BitSet2;

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
		//System.out.println("Frame()  "+Arrays.toString(units));
	}
	
	public Frame(Unit[] units) {
		for(int i=0;i<units.length;i++){
			this.units[i] = units[i];
		}
		//Fill empty spots with Filler data flags.
		for(int i=0;i<PAYLOAD_UNIT_COUNT;i++){
			if(this.units[i]==null){
				this.units[i] = new Unit(Unit.FLAG_FILLER_DATA);
			}
		}
		//System.out.println("Frame(Unit[] units)  "+Arrays.toString(units));
	}

	public Frame(BitSet2 data) throws FrameSizeTooSmallException {
		//Put as much data as possible in units.
		for(int i=0;i<data.length()-7;i=i+8){
			this.units[i/8] = new Unit(Bytes.fromBitSet(data, i));
		}
		//Fill empty spots with Filler data flags.
		for(int i=0;i<PAYLOAD_UNIT_COUNT;i++){
			if(this.units[i]==null){
				this.units[i] = new Unit(Unit.FLAG_FILLER_DATA);
			}
		}
		//System.out.println("Frame(BitSet2 data)  "+Arrays.toString(units));
	}
	
	/**
	 * Returns only the data contained in this Frame.
	 * (Flags and stuffings are filtered out).
	 * @return
	 */
	public BitSet2 getBitSet(){
		BitSet2 result = new BitSet2();
		for(Unit u : units){
			//System.out.println(result.length());
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
			newUnits[i] = new Unit(units[i].b,units[i].isSpecial);
		}
		return new Frame(newUnits);
	}
	
	public String toString(){
		return Arrays.toString(this.units);
	}
}

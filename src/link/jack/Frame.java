package link.jack;

import java.util.Arrays;

import util.BitSet2;

import java.util.concurrent.ArrayBlockingQueue;
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

	public Frame(){
		
	}
	
	/*
	public Frame() {
		for(int i=0;i<units.length;i++){
			units[i] = new Unit(PureUnit.FLAG_FILLER_DATA);
		}
		//System.out.println("Frame()  "+Arrays.toString(units));
	}*/
	
	public Frame(Unit[] units) {
		for(int i=0;i<units.length;i++){
			this.units[i] = units[i];
		}
		//Fill empty spots with Filler data flags.
		for(int i=0;i<PAYLOAD_UNIT_COUNT;i++){
			if(this.units[i]==null){
				this.units[i] = new PureUnit(PureUnit.FLAG_FILLER_DATA);
			}
		}
		//System.out.println("Frame(Unit[] units)  "+Arrays.toString(units));
	}

	public Frame(BitSet2 data){
		//Put as much data as possible in units.
		for(int i=0;i<data.length()-7;i=i+8){
			this.units[i/8] = new PureUnit(Bytes.fromBitSet(data, i));
		}
		//Fill empty spots with Filler data flags.
		for(int i=0;i<PAYLOAD_UNIT_COUNT;i++){
			if(this.units[i]==null){
				this.units[i] = new PureUnit(PureUnit.FLAG_FILLER_DATA);
			}
		}
		//System.out.println("Frame(BitSet2 data)  "+Arrays.toString(units));
	}
	
	/**
	 * Returns only the data contained in this Frame.
	 * (Flags and stuffings are filtered out).
	 * @return
	 */
	public BitSet2 getDataBitSet(){
		BitSet2 result = new BitSet2();
		for(Unit u : units){
			//System.out.println(result.length());
				result = BitSet2.concatenate(result, u.dataAsBitSet());
		}
		return result;
	}
	
	/**
	 * Returns all the bits in this payload
	 * (Flags and stuffings are left in).
	 * @return
	 */
	public BitSet2 getFullBitSet(){
		BitSet2 result = new BitSet2();
		for(Unit u : units){
			//System.out.println(result.length());
				result = BitSet2.concatenate(result, u.fullAsBitSet());
		}
		return result;
	}

	public Unit getUnit(int unitIndex) {
		if(unitIndex<units.length){
			return units[unitIndex];
		}else{
			return null;
		}
	}
	
	public Unit[] getUnits(){
		return units;
	}

	public Frame getClone() {
		Unit[] newUnits = new Unit[PAYLOAD_UNIT_COUNT];
		for(int i=0;i<units.length;i++){
			newUnits[i] = units[i].getClone();
		}
		return new Frame(newUnits);
	}
	
	public String toString(){
		return Arrays.toString(this.units);
	}
}

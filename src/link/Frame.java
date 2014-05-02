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

	protected Unit[] units = new Unit[PAYLOAD_SIZE_UNITS];

	public static final int PAYLOAD_SIZE_UNITS = 10;
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

	public Frame(BitSet data) throws FrameSizeTooSmallException {
		if (data.size() > PAYLOAD_SIZE_BITS) {
			throw new FrameSizeTooSmallException();
		}
		dataStored = new BitSet(PAYLOAD_SIZE_BITS);
		dataStored.set(0, dataStored.size());
		dataStored.and(data);
		currentReaderIndex = data.size(); // off by 1 danger zone
		fillerBytes=0;
	}

	/**
	 * @require 0<length<=Frame.LENGTH;
	 * @param data
	 * @param length
	 */
	public Frame(BitSet data, int length) throws FrameSizeTooSmallException {
		if (data.size() > PAYLOAD_SIZE_BITS) {
			throw new FrameSizeTooSmallException();
		}
		dataStored = new BitSet(PAYLOAD_SIZE_BITS);
		dataStored.set(0, dataStored.size());
		dataStored.and(data);
		currentReaderIndex = length; // off by 1 danger zone
		fillerBytes=0;
	}
	
	
	
	public byte readBit() {
		boolean bit = dataStored.get(currentReaderIndex);
		if (bit) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public boolean hasNext(){
		return currentReaderIndex>0;
	}

	public void moveReaderBack() {
		currentReaderIndex--;
	}

	public void moveReaderForward() {
		currentReaderIndex++;
	}

	public void add(byte bit) throws InvalidBitException, FrameSizeTooSmallException {
		if(dataStored.size()>=PAYLOAD_SIZE_BITS){
			throw new FrameSizeTooSmallException();
		}
		if (bit == 1) {
			dataStored.set(dataStored.size(), true);
		} else if (bit == 0) {
			dataStored.set(dataStored.size(), false);
		} else {
			throw new InvalidBitException();
		}
	}

	public void addByte(byte byteToAdd) throws FrameSizeTooSmallException{
		if(dataStored.size()+8>=PAYLOAD_SIZE_BITS){
			throw new FrameSizeTooSmallException();
		}
		byte[] converter = new byte[] { byteToAdd };
		dataStored = BitSets.concatenate(dataStored,
				ByteArrays.toBitSet(converter));
	}

	public byte getByte(int byteIndex) {
		return Bytes.fromBitSet(dataStored, byteIndex * 8);
	}

	public Frame getClone() {
		try {
			return new Frame(dataStored);
		} catch (FrameSizeTooSmallException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addFillerByte(){
		fillerBytes++;
	}
	
	public int getFillerBytes(){
		return fillerBytes;
	}

	public byte[] getByteArray() throws IncompleteByteException {
		if (dataStored.size() % 8 == 0) {
			return ByteArrays.fromBitSet(this.dataStored);
		} else {
			throw new IncompleteByteException();
		}
	}

}

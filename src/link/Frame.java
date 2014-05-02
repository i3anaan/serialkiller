package link;

import java.util.BitSet;

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

	protected BitSet dataStored;

	public static final int PAYLOAD_SIZE_BYTES = 10;
	public static final int PAYLOAD_SIZE_BITS = PAYLOAD_SIZE_BYTES * 8;

	// Imagine this as a pointer pointing to the end of the bit sequence.
	// To the left is the last placed, valid, bit.
	// To the right a new bit would be placed;
	// XXXXXXXXXXXXX ---------
	// -------------^---------
	// -------------|---------
	int currentReaderIndex = 0;

	public Frame() {
		dataStored = new BitSet(PAYLOAD_SIZE_BITS);
		currentReaderIndex = 0;
	}

	public Frame(BitSet data) throws FrameSizeTooSmallException {
		if (data.size() > PAYLOAD_SIZE_BITS) {
			throw new FrameSizeTooSmallException();
		}
		dataStored = new BitSet(PAYLOAD_SIZE_BITS);
		dataStored.set(0, dataStored.size());
		dataStored.and(data);
		currentReaderIndex = data.size(); // off by 1 danger zone
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
	}

	public boolean isComplete() {
		// Hier was vroeger een off by one error
		// (return currentLength==Frame.LENGTH;)
		// Hierdoor start currentLength nu op 0, en heeft nextBit/add een -1;
		return dataStored.length() == Frame.PAYLOAD_SIZE_BITS;
	}

	public byte readBit() {
		boolean bit = dataStored.get(currentReaderIndex);
		if (bit) {
			return 1;
		} else {
			return 0;
		}
	}

	public void moveReaderBack() {
		currentReaderIndex--;
	}

	public void moveReaderForward() {
		currentReaderIndex++;
	}
	
	public void add(byte bit) throws InvalidBitException,
			FrameSizeTooSmallException {
		if (isComplete()) {
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

	public void addByte(byte byteToAdd) throws FrameSizeTooSmallException {
		byte[] converter = new byte[] { byteToAdd };
		if (dataStored.size() + 8 < PAYLOAD_SIZE_BITS) {
			dataStored = BitSets.concatenate(dataStored,
					ByteArrays.toBitSet(converter));
		} else {
			throw new FrameSizeTooSmallException();
		}
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

	public byte[] getByteArray() throws IncompleteByteException {
		if (dataStored.size() % 8 == 0) {
			return ByteArrays.fromBitSet(this.dataStored);
		} else {
			throw new IncompleteByteException();
		}
	}

}

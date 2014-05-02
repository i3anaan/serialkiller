package link;

import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

import util.BitSets;
import util.ByteArrays;
import util.Bytes;

/**
 * Serves as a header surrounding the normal Frame.
 * 
 * @author I3anaan
 * 
 */
public class FlaggedFrame extends Frame {
	Frame payload;

	private boolean receivedEndFlag;

	public static final byte FILLER_DATA = 0;
	public static final byte END_OF_FRAME_FLAG = 1;

	public static final byte[] BYTE_SIZE_FLAGS = new byte[] { FILLER_DATA,
			END_OF_FRAME_FLAG };
	// TODO research better flags
	// Flag-like bytes have a 9th bit, true means flag, false means data.

	public static final int FULL_SIZE = 80;

	public FlaggedFrame() {
		this.payload = new Frame();
		this.dataStored = new BitSet(FULL_SIZE);
	}

	public FlaggedFrame(Frame payload) {
		this.payload = payload;
		this.dataStored = new BitSet();

		for (int i = 0; i < Frame.PAYLOAD_SIZE_BYTES; i++) {
			dataStored = BitSets.concatenate(dataStored,
					escapeFlags(payload.getByte(i)));
		}
	}
	
	public FlaggedFrame(ArrayBlockingQueue<Byte> queue){
		byte[] bytesTaken = new byte[8];
		int byteCount = 0;
		while(byteCount<PAYLOAD_SIZE_BYTES && !queue.isEmpty()){
				try {
					bytesTaken[byteCount] = queue.take();
					byteCount++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		dataStored = ByteArrays.toBitSet(bytesTaken);
		currentReaderIndex = byteCount*8;
	}

	@Override
	public void addByte(byte byteToAdd) {
		byte[] converter = new byte[] { byteToAdd };
		dataStored = BitSets.concatenate(dataStored,
				ByteArrays.toBitSet(converter));
		checkFlags();
	}

	@Override
	public void add(byte bit) throws InvalidBitException {
		if (bit == 1) {
			dataStored.set(dataStored.size(), true);
		} else if (bit == 0) {
			dataStored.set(dataStored.size(), false);
		} else {
			throw new InvalidBitException();
		}
		checkFlags();
	}

	public void checkFlags(){
		int i = 0;
		while(i<dataStored.size()){
			byte b = Bytes.fromBitSet(dataStored, i);
			i = i + 8;
			if (Arrays.asList(BYTE_SIZE_FLAGS).contains(b)){
				i++;
				if(dataStored.get(i + 8)){
					if(b==END_OF_FRAME_FLAG){
						this.receivedEndFlag = true;
						//TODO what if this flag is found not at end?
						//Currently does not care.
					}
				}
			}
		}
	}

	public BitSet escapeFlags(byte b) {
		byte[] convertor = new byte[] { b };
		BitSet result = ByteArrays.toBitSet(convertor);
		if (Arrays.asList(BYTE_SIZE_FLAGS).contains(b)) {
			BitSet escapeSign = new BitSet();
			escapeSign.set(0, true);
			result = BitSets.concatenate(result, escapeSign);
		}
		return result;
	}

	public Frame getPayloadFrame() throws PayloadNotCompleteException {
		Frame currentPayload = new Frame();
		int i = 0;
		while (i < this.dataStored.size()) {
			byte b = Bytes.fromBitSet(dataStored, i);
			//TODO Might request bytes not in the BitSet (get me 8 bits from 15, whil length is only 16).
			i = i + 8;
			if (Arrays.asList(BYTE_SIZE_FLAGS).contains(b) && dataStored.get(i)) {
				// Is a flag;
				i++;
				if (b == FILLER_DATA) {
					currentPayload.addFillerByte();
				}
				// TODO do something with flag.
			} else {// Is data.
				i++;
				try {
					currentPayload.addByte(b);
				} catch (FrameSizeTooSmallException e) {
					// TODO Should never happen.
					e.printStackTrace();
				}
			}
		}
		return currentPayload;
	}

	public boolean isComplete() {
		return receivedEndFlag;
	}
}

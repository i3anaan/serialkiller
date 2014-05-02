package link;

import java.util.Arrays;
import java.util.BitSet;

import util.BitSets;
import util.ByteArrays;
import util.Bytes;

/**
 * Serves as a header surrounding the normal Frame.
 * @author I3anaan
 *
 */
public class FlaggedFrame extends Frame{
	Frame payload;
	
	public static final byte FILLER_DATA = 0;
	
	public static final byte[] BYTE_SIZE_FLAGS = new byte[]{0,1};
	//TODO research better flags
	//Flag-like bytes have a 9th bit, true means flag, false means data.
	
	public static final int FULL_SIZE = 80;
	
	public FlaggedFrame(){
		this.payload = new Frame();
		this.dataStored = new BitSet(FULL_SIZE);
	}
	
	public FlaggedFrame(Frame payload){
		this.payload = payload;
		this.dataStored = new BitSet();
		
		
		for(int i=0;i<Frame.PAYLOAD_SIZE_BYTES;i++){
			dataStored = BitSets.concatenate(dataStored,escapeFlags(payload.getByte(i)));
		}		
	}
	
	public BitSet escapeFlags(byte b){
		byte[] convertor = new byte[]{b};
		BitSet result = ByteArrays.toBitSet(convertor);
		if(Arrays.asList(BYTE_SIZE_FLAGS).contains(b)){
			BitSet escapeSign = new BitSet();
			escapeSign.set(0,true);
			result = BitSets.concatenate(result, escapeSign);
		}
		return result;		
	}
	
	public byte[] getPayload() throws PayloadNotCompleteException{
		Frame currentPayload = new Frame();
		int i = 0;
		while(i<this.dataStored.size()){
			byte b = Bytes.fromBitSet(dataStored, i);
			i = i+8;
			if(Arrays.asList(BYTE_SIZE_FLAGS).contains(b) && dataStored.get(i+1)){
				//Is a flag;
					//TODO do something with flag.
			}else{//Is data.
				try {
					currentPayload.addByte(b);
				} catch (FrameSizeTooSmallException e) {
					// TODO Should never happen.
					e.printStackTrace();
				}
			}
		}
		
		try {
			return currentPayload.getByteArray();
		} catch (IncompleteByteException e) {
			// TODO Wat nu te doen?
			e.printStackTrace();
			return null;
		}
	}
	
	
	public boolean isComplete(){
		try {
			return getPayload().length==Frame.PAYLOAD_SIZE_BYTES;
		} catch (PayloadNotCompleteException e) {
			return false;
		}
	}

}

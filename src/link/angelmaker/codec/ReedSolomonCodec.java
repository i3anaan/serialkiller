package link.angelmaker.codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import link.angelmaker.codec.rs.GenericGF;
import link.angelmaker.codec.rs.ReedSolomonDecoder;
import link.angelmaker.codec.rs.ReedSolomonEncoder;
import link.angelmaker.codec.rs.ReedSolomonException;

import util.BitSet2;

import com.google.common.base.Optional;

public class ReedSolomonCodec implements Codec {
	private ReedSolomonEncoder rse;
	private ReedSolomonDecoder rsd;
	
	public ReedSolomonCodec() {
		rse = new ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256);
		rsd = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);
	}
	
	@Override
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		byte[] dataBytes = input.toByteArray();
	    int numDataBytes = 1;
	    int numEcBytesInBlock = 1;
	    
	    System.out.printf("Encode before: %s\n", Arrays.toString(dataBytes));
	    
	    int[] toEncode = new int[numDataBytes + numEcBytesInBlock];
	    for (int i = 0; i < numDataBytes; i++) {
	      toEncode[i] = dataBytes[i] & 0xFF;
	    }
	    
	    System.out.printf("               %s\n", Arrays.toString(toEncode));

	    rse.encode(toEncode, numEcBytesInBlock);
	    
	    System.out.printf("Encode after:  %s\n", Arrays.toString(toEncode));

	    byte[] ecBytes = new byte[numDataBytes + numEcBytesInBlock];
	    for (int i = 0; i < numEcBytesInBlock + numDataBytes; i++) {
	      ecBytes[i] = (byte) toEncode[i];
	    }
	    
	    System.out.printf("Encode after:  %s\n", Arrays.toString(ecBytes));
	    
	    return new BitSet2(ecBytes);
	}

	@Override
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException {
		byte[] codewordBytes = input.toByteArray();
		
	    int numDataCodewords = 1;
	    int numECCodewords = 1;
	    int numCodewords = numDataCodewords + numECCodewords;
	    byte[] output = new byte[numDataCodewords];
	    
	    System.out.printf("Decode before: %s\n", Arrays.toString(codewordBytes));


	    // First read into an array of ints
	    int[] codewordsInts = new int[numCodewords];
	    for (int i = 0; i < numCodewords; i++) {
	      codewordsInts[i] = codewordBytes[i] & 0xFF;
	    }
	    System.out.printf("               %s\n", Arrays.toString(codewordsInts));

	    try {
	        rsd.decode(codewordsInts, numECCodewords);
	    } catch (ReedSolomonException ignored) {
	    	ignored.printStackTrace();
	    }
	    
	    for (int i = 0; i < numDataCodewords; i++) {
	        output[i] = (byte) codewordsInts[i];
	    }
	    
	    System.out.printf("Decode after: %s\n", Arrays.toString(output));
	    
	    return Optional.of(new BitSet2(output));
	}

}

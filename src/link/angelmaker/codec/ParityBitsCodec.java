package link.angelmaker.codec;

import com.google.common.base.Optional;

import util.BitSet2;

/**
 * A Codec that implements a two-bits-per-byte parity check.
 */
public class ParityBitsCodec implements Codec {

	/**
	 * Encodes a BitSet of input data by adding two parity bits for every
	 * byte.
	 * 
	 * @throws IllegalArgumentException if the BitSet is not a BitSet of bytes
	 */
	@Override
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		if (input.length() % 8 != 0) {
			String msg = "encode: input.length() must be a multiple of 8 bits";
			throw new IllegalArgumentException(msg);
		}
		
		BitSet2 out = new BitSet2();
		
		for (int i = 0; i < input.length(); i += 8) {
			BitSet2 oneByte = input.get(i, i+8);
			byte par = (byte) (oneByte.cardinality() % 4);
			
			BitSet2 bitsSet = new BitSet2(par);

			
			out.addAtEnd(oneByte);
			out.addAtEnd(bitsSet.get(6, 8));
		}
		
		return out;
	}
	
	/**
	 * Decodes a BitSet that was encoded by encode.
	 * 
	 * @throws IllegalArgumentException if the BitSet is not encoded parity
	 */
	@Override
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException {
		if (input.length() % 10 != 0) {
			String msg = "decode: input.length() must be a multiple of 10 bits";
			throw new IllegalArgumentException(msg);
		}
		
		return Optional.absent();
	}

}

package link.angelmaker.codec;

import com.google.common.base.Optional;

import util.BitSet2;

/**
 * A Codec that implements a two-bits-per-byte parity check. In other words,
 * the encoding adds a two-bit population count to every data byte, like so:
 * 
 * 0000 0000 -> 0000 0000 00
 * 0001 0001 -> 0001 0001 10
 * 1111 1111 -> 1111 1111 00
 * 
 * Both decode and encode throw IllegalArgumentExceptions when they get an
 * input with a wrong amount of bits (i.e. they will never encode or decode
 * parts of bytes).
 * 
 * The decode function will return an empty Optional when the data is long
 * enough but invalid.
 * 
 * No error correction is attempted.
 */
public class ParityBitsCodec implements Codec {
	/** The amount of bits in a byte. */
	public static final int BYTE = 8;
	
	/** The amount of overhead per byte as bits. */
	public static final int OVERHEAD = 2;
	
	/** The space required to store one encoded byte, in bits. */
	public static final int ENCODED_BYTE = BYTE + OVERHEAD;

	/**
	 * Encodes a BitSet of input data by adding two parity bits for every
	 * byte.
	 * 
	 * @throws IllegalArgumentException if the BitSet is not a BitSet of bytes
	 */
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		if (input.length() % 8 != 0) {
			String msg = "encode: input.length() must be a multiple of 8 bits";
			throw new IllegalArgumentException(msg);
		}
		
		BitSet2 out = new BitSet2();
		
		for (int i = 0; i < input.length(); i += 8) {
			BitSet2 oneByte = input.get(i, i+8);
			BitSet2 parBits = parity(oneByte);
			
			out.addAtEnd(oneByte);
			out.addAtEnd(parBits);
		}
		
		return out;
	}
	
	/**
	 * Decodes a BitSet that was encoded by encode.
	 * 
	 * @throws IllegalArgumentException if the BitSet is not encoded parity
	 */
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException {
		if (input.length() % 10 != 0) {
			String msg = "decode: input.length() must be a multiple of 10 bits";
			throw new IllegalArgumentException(msg);
		}
		
		BitSet2 out = new BitSet2();
		
		for (int i = 0; i < input.length(); i += 10) {
			BitSet2 oneByte = input.get(i, i + 8);
			BitSet2 parBits = input.get(i + 8, i + 10);
			BitSet2 dataPar = parity(oneByte);
			
			if (!dataPar.equals(parBits)) {
				return Optional.absent();
			}
			
			out.addAtEnd(oneByte);
		}
		
		return Optional.of(out);
	}

	private static BitSet2 parity(BitSet2 input) {
		byte par = (byte) (input.cardinality() % 4);
		return new BitSet2(par).get(6, 8);
	}
}

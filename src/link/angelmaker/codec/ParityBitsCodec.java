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
	private int byteLength;
	
	/** The amount of overhead per byte as bits. */
    public static final int OVERHEAD = 2;
	private int overheadLength;
	
	/** The space required to store one encoded byte, in bits. */
    public static final int ENCODED_BYTE = BYTE + OVERHEAD;
	private int encodedByteLength;

    public ParityBitsCodec() {
        this(BYTE, OVERHEAD);
    }

    public ParityBitsCodec(int byteLength, int overheadLength) {
        super();
        this.byteLength = byteLength;
        this.overheadLength = overheadLength;
        this.encodedByteLength = byteLength + overheadLength;
    }

	/**
	 * Encodes a BitSet of input data by adding two parity bits for every
	 * byte.
	 * 
	 * @throws IllegalArgumentException if the BitSet is not a BitSet of bytes
	 */
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		if (input.length() % byteLength != 0) {
			String msg = String.format("encode: input.length() must be a multiple of %d bits", byteLength);
			throw new IllegalArgumentException(msg);
		}
		
		BitSet2 out = new BitSet2();
		
		for (int i = 0; i < input.length(); i += byteLength) {
			BitSet2 oneByte = input.get(i, i+ byteLength);
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
		if (input.length() % encodedByteLength != 0) {
			String msg = String.format("decode: input.length() must be a multiple of %d bits", encodedByteLength);
			throw new IllegalArgumentException(msg);
		}
		
		BitSet2 out = new BitSet2();
		
		for (int i = 0; i < input.length(); i += encodedByteLength) {
			BitSet2 oneByte = input.get(i, i + byteLength);
			BitSet2 parBits = input.get(i + byteLength, i + encodedByteLength);
			BitSet2 dataPar = parity(oneByte);
			
			if (!dataPar.equals(parBits)) {
				return Optional.absent();
			}
			
			out.addAtEnd(oneByte);
		}
		
		return Optional.of(out);
	}

	private BitSet2 parity(BitSet2 input) {
		byte par = (byte) (input.cardinality() % (int) Math.pow(2, overheadLength));
		return new BitSet2(par).get(8 - overheadLength, 8);
	}
}

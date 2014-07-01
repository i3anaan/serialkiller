package link.angelmaker.codec;

import com.google.common.base.Optional;

import util.BitSet2;

/**
 * A codec that can encode and decode bits.
 * Decoding Encoded bits should return the original data.
 * Decode returns an empty Optional if the given encoded data was incorrect.
 * @author I3anaan
 *
 */
public interface Codec {
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException;
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException;
}

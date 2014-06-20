package link.angelmaker.codec;

import com.google.common.base.Optional;

import util.BitSet2;

public interface Codec {
	/**
	 * Encodes a bit set in an implementation-defined way, returning a new 
	 * BitSet2.
	 * 
	 * @require input != null
	 * @ensure result != null
	 */
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException;
	
	/**
	 * Decodes a bit set encoded in an implementation-defined way. If no
	 * input can be decoded, return an absent value (Optional.absent()).
	 * 
	 * @require input != null
	 * @ensure result != null
	 */
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException;
}

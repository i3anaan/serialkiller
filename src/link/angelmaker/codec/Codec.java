package link.angelmaker.codec;

import com.google.common.base.Optional;

import util.BitSet2;

public interface Codec {
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException;
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException;
}

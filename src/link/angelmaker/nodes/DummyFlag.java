package link.angelmaker.nodes;

import util.BitSet2;

/**
 * Flag implementation that does not do any stuffing or unstuffing.
 */
public class DummyFlag implements Flag{

	public final BitSet2 flag;
	/*
	 * Escaped flag has the a first bit appended, opposite of the original
	 * flags first bit Before the last bit an extra bit is inserted, the
	 * opposite of the last bit. Together these two stuffings should make it
	 * impossible for the original flag to appear somewhere.
	 */

	public DummyFlag(BitSet2 flag) {
		this.flag = flag;
	}

	@Override
	public void stuff(BitSet2 bits) {
	}

	@Override
	public void unStuff(BitSet2 bits) {
	}

	@Override
	public BitSet2 getFlag() {
		return flag;
	}

}

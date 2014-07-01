package link.angelmaker.flags;

import util.BitSet2;

/**
 * Flag implementation that does not do any stuffing or unstuffing.
 */
public class DummyFlag implements Flag{

	public final BitSet2 flag;
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

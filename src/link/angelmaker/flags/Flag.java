package link.angelmaker.flags;

import util.BitSet2;

public interface Flag {
	public void stuff(BitSet2 bits);
	public void unStuff(BitSet2 bits);
	public BitSet2 getFlag();
}

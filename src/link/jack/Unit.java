package link.jack;

import util.BitSet2;
import util.Bytes;

public interface Unit {
	public BitSet2 fullAsBitSet();
	public BitSet2 dataAsBitSet();
	public boolean isSpecial();
	public Unit getClone();
	public boolean isFiller();
	public byte getByte();
	public Unit getFiller();
}

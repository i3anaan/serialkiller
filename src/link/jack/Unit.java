package link.jack;

import util.BitSet2;
import util.Bytes;

public interface Unit {
	public byte b = -1;
	public BitSet2 fullAsBitSet();
	public BitSet2 dataAsBitSet();
	public boolean isSpecial();
	public PureUnit getClone();
	public boolean isFiller();
}

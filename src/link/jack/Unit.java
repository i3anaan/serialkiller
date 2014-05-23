package link.jack;

import util.BitSet2;

public abstract class Unit {	
	public abstract boolean isFiller();
	public abstract boolean isSpecial();
	public abstract boolean isEndOfFrame();
	
	public abstract BitSet2 serializeToBitSet();
	public abstract Unit getFlag(byte flag);
	public abstract Unit getFiller();
	public abstract Unit getEndOfFrame();
	public abstract Unit constructFromBitSet(BitSet2 bs);
	public abstract int getSerializedBitCount();
	
	//Useful for testing;
	public abstract Unit getRandomUnit();
}

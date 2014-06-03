package link.angelmaker.nodes;

import util.BitSet2;

public class BasicFlag implements Flag{

	public final BitSet2 flag;
	/*
	 * Escaped flag has the a first bit appended, opposite of the original
	 * flags first bit Before the last bit an extra bit is inserted, the
	 * opposite of the last bit. Together these two stuffings should make it
	 * impossible for the original flag to appear somewhere.
	 */
	BitSet2 escapedFlag;

	public BasicFlag(BitSet2 flag) {
		this.flag = flag;
		escapedFlag = (BitSet2) flag.clone();
		escapedFlag.insert(0, !escapedFlag.get(0));
		escapedFlag.insert(escapedFlag.length() - 1,
				!escapedFlag.get(escapedFlag.length() - 1));
	}

	// TODO SHOULD TEST
	public void stuff(BitSet2 bits) {
		// If data contains escaped flag, double it.
		int containsEscapedFlag = bits.contains(escapedFlag);
		if (containsEscapedFlag > 0) {
			bits.insert(containsEscapedFlag + escapedFlag.length(), escapedFlag);
		}

		// If data contains flag, escape it.
		int containsFlag = bits.contains(flag);
		if (containsFlag > 0) {
			bits.insert(containsFlag, !flag.get(0));
			bits.insert(containsFlag + flag.length()+1-1,
					!flag.get(flag.length() - 1));
		}
	}

	public void unStuff(BitSet2 bits) {
		int contains = bits.contains(escapedFlag);
		if(contains>0){
			if(bits.get(contains+flag.length(),contains+flag.length()*2).equals(escapedFlag)){
				//2 EscapedFlags in a row > remove second, ignore first.
				bits.remove(contains+flag.length(),contains+flag.length()*2);
			}else{
				//Just 1 EscapedFlag > unstuff first.
				bits.remove(contains+escapedFlag.length()-2);
				bits.remove(contains);
			}
		}
	}

	@Override
	public BitSet2 getFlag() {
		return flag;
	}

}

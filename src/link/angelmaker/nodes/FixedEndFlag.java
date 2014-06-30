package link.angelmaker.nodes;

import util.BitSet2;

public class FixedEndFlag implements Flag{

	public final BitSet2 flag = new BitSet2("001101");
	/*
	 * Escaped flag has the a first bit appended, opposite of the original
	 * flags first bit Before the last bit an extra bit is inserted, the
	 * opposite of the last bit. Together these two stuffings should make it
	 * impossible for the original flag to appear somewhere.
	 */
	BitSet2 escapedFlag = new BitSet2("110010");
	BitSet2 realEscapedFlag = new BitSet2("1100100");	//had flag in data.
	BitSet2 escapedEscapedFlag = new BitSet2("1100101");		//had escapedFlag in data.

	public FixedEndFlag() {
	}

    @Override
	public void stuff(BitSet2 bits) {
		int index = 0;
        int contains = bits.contains(escapedFlag, index);

        while (contains >= 0) {
            // Replace escapedFlag with escapedEscapedFlag
            bits.remove(contains, contains + escapedFlag.length());
            bits.insert(contains, escapedEscapedFlag);

            index = contains + escapedEscapedFlag.length();
            contains = bits.contains(escapedFlag, index);
        }

        index = 0;
        contains = bits.contains(flag, index);

        while (contains >= 0) {
            // Replace flag with realEscapedFlag
            bits.remove(contains, contains + flag.length());
            bits.insert(contains, realEscapedFlag);

            index = contains + realEscapedFlag.length();
            contains = bits.contains(escapedFlag, index);
        }
	}

    @Override
	public void unStuff(BitSet2 bits) {
		int index = 0;
        int contains = bits.contains(realEscapedFlag, index);

        while (contains >= 0) {
            // Replace realEscapedFlag with flag
            bits.remove(contains, contains + realEscapedFlag.length());
            bits.insert(contains, flag);

            index = contains + flag.length();
            contains = bits.contains(realEscapedFlag, index);
        }

		index = 0;
		contains = bits.contains(escapedEscapedFlag, index);

		while (contains >= 0) {
            // Replace escapedEscapedFlag with escapedFlag
            bits.remove(contains, contains + escapedEscapedFlag.length());
            bits.insert(contains, escapedFlag);

            index = contains + escapedFlag.length();
            contains = bits.contains(escapedEscapedFlag, index);
		}
	}

	@Override
	public BitSet2 getFlag() {
		return flag;
	}

}

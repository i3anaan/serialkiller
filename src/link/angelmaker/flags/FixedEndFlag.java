package link.angelmaker.flags;

import util.BitSet2;

/**
 * A specific made EndFlag. This flag has a hardcoded bit flag.
 * 
 * Stuffing is done by escaping the flag (insert the inverse of the last bit
 * before the last bit). However this escaped real flag needs to be
 * distinguishable from a normal bit sequence in the data, that is similair to
 * the escaped flag. In order to do this, a set amount of bits is appended at
 * the end to be able to distinguish between the two. The Escaped flags are
 * chosen in such a way that it is impossible for them to form new flags or
 * escaped flags around it when the stuffing is done.
 * 
 * @author I3anaan
 * 
 */
public class FixedEndFlag implements Flag {

	/**
	 * The Actual flag.
	 */
	public final BitSet2 flag = new BitSet2("001101");
	BitSet2 escapedFlag = new BitSet2("00011001");
	BitSet2 realEscapedFlag = new BitSet2("00011001 111"); // had flag in data.
	BitSet2 escapedEscapedFlag = new BitSet2("00011001 011"); // had escapedFlag
																// in data.

	/**
	 * Used to test the flag.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start test");
		System.out.println(new FixedEndFlag().alwaysWorks());
		System.out.println("Test done");
	}

	@Override
	public void stuff(BitSet2 bits) {
		int index = 0;
		int contains = bits.contains(escapedFlag, index);

		while (contains >= 0) {
			// Replace escapedFlag with escapedEscapedFlag
			bits.replace(contains, contains + escapedFlag.length(),
					escapedEscapedFlag);

			index = contains + escapedEscapedFlag.length();
			contains = bits.contains(escapedFlag, index);
		}

		index = 0;
		contains = bits.contains(flag, index);

		while (contains >= 0) {
			// Replace flag with realEscapedFlag
			bits.replace(contains, contains + flag.length(), realEscapedFlag);

			index = contains + realEscapedFlag.length();
			contains = bits.contains(flag, index);
		}
	}

	@Override
	public void unStuff(BitSet2 bits) {
		int index = 0;
		int contains = bits.contains(realEscapedFlag, index);

		while (contains >= 0) {
			// Replace realEscapedFlag with flag
			bits.replace(contains, contains + realEscapedFlag.length(), flag);

			index = contains + flag.length();
			contains = bits.contains(realEscapedFlag, index);
		}

		index = 0;
		contains = bits.contains(escapedEscapedFlag, index);

		while (contains >= 0) {
			// Replace escapedEscapedFlag with escapedFlag
			bits.replace(contains, contains + escapedEscapedFlag.length(),
					escapedFlag);

			index = contains + escapedFlag.length();
			contains = bits.contains(escapedEscapedFlag, index);
		}
	}

	@Override
	public BitSet2 getFlag() {
		return flag;
	}

	/**
	 * Test method, returns true if this flag works for every possible
	 * bitsequence.
	 * 
	 * @return
	 */
	private int additionCount;

	public boolean alwaysWorks() {
		additionCount = escapedFlag.length() * 2;
		return testBitSequence(new BitSet2(), additionCount);
	}

	private boolean testBitSequence(BitSet2 current, int additionsLeft) {
		if (additionsLeft <= 0) {
			// test sequence
			BitSet2 clone = (BitSet2) current.clone();
			stuff(clone);
			boolean containsFlag = clone.contains(flag) >= 0;
			boolean equal = false;
			if (!containsFlag) {
				unStuff(clone);
				equal = clone.equals(current);
				if (!equal) {
					stuff(current);
				}
			} else {
			}
			return !containsFlag && equal;
		} else {
			BitSet2 clone = (BitSet2) current.clone();
			current.addAtEnd(false);
			boolean resultFalse = testBitSequence(current, additionsLeft - 1);
			boolean resultTrue = false;
			if (resultFalse) {
				clone.addAtEnd(true);
				resultTrue = testBitSequence(clone, additionsLeft - 1);
			}
			return resultFalse && resultTrue;
		}
	}

}

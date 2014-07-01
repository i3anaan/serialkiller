package link.angelmaker.flags;

import util.BitSet2;

/**
 * Similar to the FixedEndFlag, however this flag is not fixed. It support any
 * bit flag, given with the constructor. A side effect of this is that the
 * escaped flags need to be dynamically created. To achieve this, significantly
 * more overhead is used compared to the fixedEndFlag.
 * 
 * This flag was created first, later on we optimized the specific flag we had
 * into the FixedEndFlag. This class therefor is currently unused (but still
 * functional)
 * 
 * Stuffing is done by escaping the flag (insert the inverse of the last bit
 * before the last bit). However this escaped real flag needs to be
 * distinguishable from a normal bit sequence in the data, that is similair to
 * the escaped flag. In order to do this, a set amount of bits is appended at
 * the end to be able to distinguish between the two. The Escaped flags are
 * chosen in such a way that it is impossible for them to form new flags or
 * escaped flags around it when the stuffing is done.
 */
public class BasicFlag implements Flag {

	public final BitSet2 flag;
	/*
	 * Escaped flag has the a first bit appended, opposite of the original flags
	 * first bit Before the last bit an extra bit is inserted, the opposite of
	 * the last bit. Together these two stuffings should make it impossible for
	 * the original flag to appear somewhere.
	 */
	BitSet2 escapedFlag;
	BitSet2 realEscapedFlag; // had flag in data.
	BitSet2 escapedEscapedFlag; // had escapedFlag in data.

	public BasicFlag(BitSet2 flag) {
		this.flag = flag;
		escapedFlag = (BitSet2) flag.clone();
		escapedFlag.insert(escapedFlag.length() - 1,
				!escapedFlag.get(escapedFlag.length() - 1));
		for (int i = 0; i < (flag.length() + 1); i++) {
			escapedFlag.insert(flag.length() + 1 - 1 - i,
					!escapedFlag.get(flag.length() + 1 - 1 - i));
		}

		// Guaranteed to have a lonely single bit.

		boolean startWith = !flag.get(flag.length() - 1);
		boolean endWith = !flag.get(0);
		int minPairs = (int) Math.max(4, Math.ceil(escapedFlag.length() / 2));
		realEscapedFlag = new BitSet2(new boolean[] { startWith, startWith });
		escapedEscapedFlag = new BitSet2(new boolean[] { startWith, startWith });
		for (int i = 0; i < minPairs - 2; i++) {
			realEscapedFlag.addAtEnd(false);
			realEscapedFlag.addAtEnd(false);
			escapedEscapedFlag.addAtEnd(true);
			escapedEscapedFlag.addAtEnd(true);
		}
		realEscapedFlag.addAtEnd(endWith);
		realEscapedFlag.addAtEnd(endWith);
		escapedEscapedFlag.addAtEnd(endWith);
		escapedEscapedFlag.addAtEnd(endWith);
	}

	@Override
	public void stuff(BitSet2 bits) {
		int index = 0;
		int containsEscapedFlag = bits.contains(escapedFlag);
		while (containsEscapedFlag >= 0) {
			bits.insert(containsEscapedFlag + escapedFlag.length(),
					escapedEscapedFlag);
			index = containsEscapedFlag + escapedFlag.length()
					+ escapedEscapedFlag.length() + 6;
			containsEscapedFlag = bits.contains(escapedFlag, index); // Ignore
																		// already
																		// flagged
																		// part.
		}

		// If data contains flag, escape it.
		int containsFlag = bits.contains(flag);
		while (containsFlag >= 0) {
			bits.remove(containsFlag, containsFlag + flag.length());
			bits.insert(containsFlag,
					BitSet2.concatenate(escapedFlag, realEscapedFlag));

			containsFlag = bits.contains(flag);
		}
	}

	@Override
	public void unStuff(BitSet2 bits) {
		int index = 0;
		int contains = bits.contains(escapedFlag);
		while (contains >= 0) {
			int start = contains + escapedFlag.length();
			int end = contains + escapedFlag.length()
					+ realEscapedFlag.length();
			if (end <= bits.length()
					&& bits.get(start, end).equals(realEscapedFlag)) {
				// Was real flag.
				bits.remove(contains, end);
				bits.insert(contains, flag);
			}
			index = index + 1;
			contains = bits.contains(escapedFlag, index);
		}
		index = 0;
		contains = bits.contains(escapedFlag);
		while (contains >= 0) {
			int end = contains + escapedFlag.length()
					+ realEscapedFlag.length();
			// Was escaped flag.
			bits.remove(contains + escapedFlag.length(), end);
			index = contains + 1;// Ignore already unstuffed parts.
			contains = bits.contains(escapedFlag, index);
		}
	}

	/**
	 * Test method, returns true if this flag works for every possible
	 * bitsequence.
	 * 
	 * @return
	 */
	private int additionCount;
	private int testProgress = 0;

	public boolean alwaysWorks() {
		additionCount = escapedFlag.length() * 2;
		return testBitSequence(new BitSet2(), additionCount);
	}

	private boolean testBitSequence(BitSet2 current, int additionsLeft) {
		if (additionsLeft <= 0) {
			// test sequence
			BitSet2 clone = (BitSet2) current.clone();
			stuff(clone);
			boolean containsFlag = clone.contains(flag) > 0;
			boolean equal = false;
			if (!containsFlag) {
				unStuff(clone);
				equal = clone.equals(current);
				if (!equal) {
					System.out.println("NOT EQUAL.");
					System.out.println("Flag:\t\t\t" + flag);
					System.out.println("EscapedFlag:\t\t" + escapedFlag);
					System.out.println("EscapedEscapedFlag:\t"
							+ escapedEscapedFlag);
					System.out.println("RealEscapedFlag:\t" + realEscapedFlag);
					System.out.println("original:\t" + current);
					stuff(current);
					System.out.println("stuffed:\t" + current);
					System.out.println("unstuffed:\t" + clone);
				}
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
			testProgress++;
			System.out.println("testProgress: " + (testProgress)
					/ 68719476736.0 + "%");
			return resultFalse && resultTrue;
		}
	}

	@Override
	public BitSet2 getFlag() {
		return flag;
	}

}

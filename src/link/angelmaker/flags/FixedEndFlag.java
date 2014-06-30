package link.angelmaker.flags;

import util.BitSet2;

public class FixedEndFlag implements Flag{

	public final BitSet2 flag = new BitSet2("001101");
	/*
	 * Escaped flag has the a first bit appended, opposite of the original
	 * flags first bit Before the last bit an extra bit is inserted, the
	 * opposite of the last bit. Together these two stuffings should make it
	 * impossible for the original flag to appear somewhere.
	 */
	BitSet2 escapedFlag = 			new BitSet2("00011001");
	BitSet2 realEscapedFlag = 		new BitSet2("00011001 111");	//had flag in data.
	BitSet2 escapedEscapedFlag = 	new BitSet2("00011001 011");		//had escapedFlag in data.


	public static void main(String[] args){
		System.out.println("Start test");
		System.out.println(new FixedEndFlag().alwaysWorks());
		System.out.println("Test done");
	}
	
	public FixedEndFlag() {
	}

    @Override
	public void stuff(BitSet2 bits) {
    	//System.out.println("Stuffing: "+bits);
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
            contains = bits.contains(flag, index);
            //System.out.println("Bits: "+bits+"\t["+contains+"]");
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
	
	
	
	
	
	/**
	 * Test method, returns true if this flag works for every possible bitsequence.
	 * @return
	 */
	private int additionCount;
	private int testProgress = 0;
	
	public boolean alwaysWorks(){
		additionCount = escapedFlag.length()*2;
		return testBitSequence(new BitSet2(), additionCount);
	}
	
	private boolean testBitSequence(BitSet2 current, int additionsLeft){
		if(additionsLeft<=0){
			//test sequence
			BitSet2 clone = (BitSet2) current.clone();
			stuff(clone);
			boolean containsFlag = clone.contains(flag)>=0;
			boolean equal = false;
			if(!containsFlag){
				unStuff(clone);
				equal = clone.equals(current);
				if(!equal){
					System.out.println("NOT EQUAL.");
					System.out.println("Flag:\t\t\t"+flag);
					System.out.println("EscapedFlag:\t\t"+escapedFlag);
					System.out.println("EscapedEscapedFlag:\t"+escapedEscapedFlag);
					System.out.println("RealEscapedFlag:\t"+realEscapedFlag);
					System.out.println("original:\t"+current);
					stuff(current);
					System.out.println("stuffed:\t"+current);
					System.out.println("unstuffed:\t"+clone);
				}
			}else{
				System.out.println("Flag:\t\t\t"+flag);
				System.out.println("EscapedFlag:\t\t"+escapedFlag);
				System.out.println("EscapedEscapedFlag:\t"+escapedEscapedFlag);
				System.out.println("RealEscapedFlag:\t"+realEscapedFlag);
				
				System.out.println("unstuffed:\t"+current);
				System.out.println("stuffed:\t"+clone);
			}
			return !containsFlag && equal;
		}else{
			BitSet2 clone = (BitSet2) current.clone();
			current.addAtEnd(false);
			boolean resultFalse = testBitSequence(current, additionsLeft-1);
			boolean resultTrue = false;
			if(resultFalse){
				clone.addAtEnd(true);
				resultTrue = testBitSequence(clone, additionsLeft-1);
			}
			//if(additionsLeft>additionCount-14){
				testProgress++;
				//System.out.println("testProgress: "+(testProgress)/68719476736.0+"%");
				System.out.println(current);
			//}
			return resultFalse && resultTrue;
		}
	}

}

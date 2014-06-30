package link.angelmaker.nodes;

import util.BitSet2;

public class FixedFlag implements Flag{

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

	public FixedFlag() {
	}

	// TODO SHOULD TEST
	public void stuff(BitSet2 bits) {
		//System.out.println("Original:\t"+bits);
		// If data contains escaped flag, place escape escaper after it.
		int index = 0;
		int containsEscapedFlag = bits.contains(escapedFlag);
		while(containsEscapedFlag>=0){
			bits.insert(containsEscapedFlag + escapedFlag.length(), escapedEscapedFlag);
			index = containsEscapedFlag + escapedFlag.length() + escapedEscapedFlag.length()+6;
			//System.out.println("Index = "+index+"  length="+bits.length());
			containsEscapedFlag = bits.contains(escapedFlag,index); //Ignore already flagged part.
			//System.out.println("Iteration:\t"+bits);
		}

		// If data contains flag, escape it.
		int containsFlag = bits.contains(flag);
		while(containsFlag>=0){
			//bits.insert(containsFlag, !flag.get(0));
			bits.remove(containsFlag,containsFlag+flag.length());
			bits.insert(containsFlag, BitSet2.concatenate(escapedFlag,realEscapedFlag));
			
			containsFlag = bits.contains(flag);
		}
		//System.out.println("Stuffed:\t"+bits);
	}

	public void unStuff(BitSet2 bits) {
		//System.out.println("To be unstuffed: "+bits);
		int index = 0;
		int contains = bits.contains(escapedFlag);
		while(contains>=0){
			int start = contains+escapedFlag.length();
			int end = contains+escapedFlag.length()+realEscapedFlag.length();
			if(end<=bits.length() && bits.get(start,end).equals(realEscapedFlag)){
				//Was real flag.				
				bits.remove(contains, end);
				//System.out.println("rmvd sffg:\t"+bits);
				bits.insert(contains, flag);
			}
			index = index+1;
			contains = bits.contains(escapedFlag,index);
		}
		//System.out.println("Unstuffed real flags.");
		index = 0;
		contains = bits.contains(escapedFlag);
		while(contains>=0){
			int end = contains+escapedFlag.length()+realEscapedFlag.length();
			//Was escaped flag.
			bits.remove(contains+escapedFlag.length(),end);
			index = contains+1;//Ignore already unstuffed parts.
			contains = bits.contains(escapedFlag,index);
		}
		//System.out.println("unstuffed: "+bits);
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
			boolean containsFlag = clone.contains(flag)>0;
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
				System.out.println("testProgress: "+(testProgress)/68719476736.0+"%");
			//}
			return resultFalse && resultTrue;
		}
	}
	

	@Override
	public BitSet2 getFlag() {
		return flag;
	}

}

package util;

import util.BitSet2;

public class BitSets {

	 /**
     * Concatenates the first and second BitSet2 together.
     * Will return a new BitSet2 whos length = |first|+|second|
     * with the first bit from the second BitSet2 getting index |first|.
     * @param frist		The left BitSet2
     * @param second	The right BitSet2
     * @return A new BitSet2 being a concatenation of both.
     */
    public static BitSet2 concatenate(BitSet2 first, BitSet2 second) {
    	int newSize = first.length()+second.length();
    	BitSet2 result = new BitSet2(newSize);
    	int f = 0;
    	int s = 0;
    	for(f=0;f<first.length();f++){
    		result.set(f+s,first.get(f));
    	}
    	for(s=0;s<second.length();s++){
    		result.set(f+s,second.get(s));
    	}
    	
    	System.out.println(result);
    	return result;
    	//TODO test this method.
    }
}

package util;

import java.util.BitSet;

public class BitSets {

	 /**
     * Concatenates the first and second BitSet together.
     * Will return a new BitSet whos length = |first|+|second|
     * with the first bit from the second BitSet getting index |first|.
     * @param frist		The left BitSet
     * @param second	The right BitSet
     * @return A new BitSet being a concatenation of both.
     */
    public static BitSet concatenate(BitSet first, BitSet second) {
    	int newSize = first.size()+second.size();
    	BitSet result = new BitSet(newSize);
    	int f = 0;
    	int s = 0;
    	for(f=0;f<first.size();f++){
    		result.set(f+s,first.get(f));
    	}
    	for(s=0;s<second.size();s++){
    		result.set(f+s,second.get(s));
    	}
    	
    	return result;
    	//TODO test this method.
    }
}

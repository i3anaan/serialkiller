package util;

import java.util.BitSet;

import util.BitSet2;

/**
 * Custom BitSet extension class which actually has a useful length method.
 * Length now means the index of the highest bit set (ever on this instance) + 1.
 * Will return BitSet2 where a BitSet would return a BitSet.
 * @author I3anaan
 *
 */
public class BitSet2 extends BitSet {
	private static final long serialVersionUID = 1L;
	private int length; //actually works
	
	public BitSet2(){
		super();
		this.length = 0;
	}
	public BitSet2(boolean[] arr){
		super();
		this.length = arr.length;
		for(int i=0;i<arr.length;i++){
			this.set(i,arr[i]);
		}
	}
	
	public BitSet2(byte[] arr){
		for(int i=0;i<arr.length;i++){
			for(int bit=0;bit<8;bit++){
				this.set(i*8+bit,((arr[i]>>bit)&1)==1);
			}
		}
		//TODO unit test dit.
	}
	
	
	
	
	public BitSet2(byte b){
		super();
		for(int i=0;i<8;i++){
			this.set(7-i,((b>>i)&1)==1);
		}
	}
	
	public BitSet2(BitSet bitset){
		super();
		this.length = 0;
		for(int i=0;i<bitset.length();i++){
			this.set(i,bitset.get(i));
		}
	}
	
	public BitSet2(int length){
		super();
		this.length = length;
	}
	@Override
	public void set(int bitIndexFrom){
		super.set(bitIndexFrom);
		this.length = Math.max(length, bitIndexFrom+1);
	}
	@Override
	public void set(int bitIndexFrom, boolean value){
		super.set(bitIndexFrom,value);
		this.length = Math.max(length, bitIndexFrom+1);
	}
	@Override
	public void set(int bitIndexFrom, int bitIndexTo){
		super.set(bitIndexFrom,bitIndexTo);
		this.length = Math.max(length, bitIndexTo+1);
	}
	@Override
	public void set(int bitIndexFrom, int bitIndexTo, boolean value){
		super.set(bitIndexFrom,bitIndexTo,value);
		this.length = Math.max(length, bitIndexTo+1);
	}
	@Override
	public int length(){
		return length;
	}
	
	/**
	 * functions as length() method of BitSet.
	 */
	public int notLengthButLastBitSet(){
		return super.length();
	}
	@Override
	public String toString(){
		String s = "";
		for(int i=0;i<length();i++){
			s = s + (this.get(i) ? "1" : "0");
		}
		return s;
	}
	
	@Override
	public BitSet2 get(int fromIndex, int toIndex){
		//TODO Workaround because java fails.
		BitSet2 bs2 = new BitSet2();
		for(int i = 0;i<toIndex-fromIndex;i++){
			bs2.set(i,this.get(fromIndex+i));
		}
		return bs2;
		//return new BitSet2(super.get(fromIndex, toIndex));
	}
	@Override
	public Object clone(){
		return (Object) new BitSet2(this);
	}
	
	/**
	 * Adds a bit at the end of this bitset2, same as set(length(), boolean)
	 */
	public void addAtEnd(boolean bit){
		this.set(length,bit);
	}
	
	/**
	 * Inserts a boolean value on the given index.
	 * This means that the boolean previously on the index will now be on index+1
	 * @param index
	 * @param b
	 */
	public void insert(int index, boolean b) {
		for(int i=this.length()-1;i>=index;i--){
			this.set(i+1,this.get(i));
		}
		this.set(index,b);
	}
	
	/**
	 * Removes a boolean value, moving every subsequent boolean 1 index down.
	 * @param index
	 */
	public void remove(int index) {
		for(int i=index;i<this.length();i++){
			this.set(i,this.get(i+1));
		}
		this.length--;
	}
	
	@Override
	public boolean equals(Object object){
		boolean isEqual = true;
		if(object instanceof BitSet2){
			BitSet2 other = (BitSet2) object;
			if(this.length()==other.length()){
				for(int i=0;i<this.length() && isEqual;i++){
					if(this.get(i)!=other.get(i)){
						isEqual = false;
					}
				}
			}else{
				isEqual = false;
			}
		}else{
			isEqual = false;
		}
		return isEqual;
	}
	
	public boolean contains(BitSet2 bs){
		if(bs.length()>this.length()){
			return false;
		}
		for(int i=0;i<this.length()-bs.length()+1;i++){
			if(this.get(i,i+bs.length()).equals(bs)){
				return true;
			}
		}
		return false;
	}
	
	
	//Utility methods from java 7
	
	/**
     * Convert a BitSet2 object to a byte array.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet2.toByteArray() is preferred.
     * @param data The BitSet2 object to convert.
     * @return The byte array.
     */
    public byte[] toByteArray() {
        int len = (int) Math.ceil((double) this.length() / 8);
        byte[] bytes = new byte[len];

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < 8; j++) {
                byte bit = (this.get((i*8)+j)) ? (byte) 1 : (byte) 0;
                bytes[i] = (byte) (bytes[i] | (bit << (7-j)));
            }
        }

        return bytes;
    }

    /**
     * Convert a byte array to a BitSet2 object.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet2.valueOf(byte[] bytes) is preferred.
     * @param bytes The byte array to convert.
     * @return The BitSet2 object.
     */
    public static BitSet2 valueOf(byte[] bytes) {
        BitSet2 data = new BitSet2(bytes.length * 8);

        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                boolean val = ((bytes[i] >> (7-j)) & 1) == 1;
                data.set((i*8)+j, val);
            }
        }
        return data;
    }
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
    	
    	return result;
    }
	
}
package link;

public class Frame {

	byte dataStored;
	
	public static final int LENGTH = 8;
	int currentLength = 0;
	
	
	public Frame(byte data){
		dataStored = data;
		currentLength = 8;
	}
	
	/**
	 * @require 0<length<=Frame.LENGTH;
	 * @param data
	 * @param length
	 */
	public Frame(byte data, int length){
		dataStored = data;
		currentLength = length;
	}
	
	
	public boolean isComplete(){
		return currentLength==Frame.LENGTH;
	}
	
	
	public byte nextBit(){
		return (byte)((dataStored>>(Frame.LENGTH-currentLength))&1);
	}
	
	public void removeBit(){
		currentLength--;
	}
	
	public void add(byte bit){
		if(bit==0){
			byte mask = (byte)~(1<<(Frame.LENGTH-currentLength-1));
			dataStored = (byte)(dataStored & mask);
		}else if(bit==1){
			byte mask = (byte)(1<<(Frame.LENGTH-currentLength-1));
			dataStored = (byte)(dataStored | mask);
		}else{
			System.out.println("Adding invalid bit!");
		}
		
	}
	
	public byte getByte(){
		return dataStored;
	}
	
	
	public Frame getFullLength(){
		return new Frame(dataStored);
	}
	
}

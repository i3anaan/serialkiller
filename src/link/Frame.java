package link;

import util.Bytes;

/**
 * Sets starting from the left (MSB), reads starting from the right (LSB).
 * @author I3anaan
 *
 */
public class Frame {

	byte dataStored;
	
	public static final int LENGTH = 8;
	int currentLength = 0;
	
	public Frame(){
		dataStored = 0;
		currentLength = 1;
	}
	
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
		System.out.println(Frame.LENGTH-currentLength);
		System.out.println(Bytes.format((byte)(dataStored>>(Frame.LENGTH-currentLength))));
		
		return (byte)((dataStored>>(Frame.LENGTH-currentLength))&1);
	}
	
	public void removeBit(){
		currentLength--;
	}
	
	public void add(byte bit){
		if(bit==0){
			byte mask = (byte)~(1<<(Frame.LENGTH-currentLength));
			dataStored = (byte)(dataStored & mask);
			//System.out.println(Bytes.format((byte)mask) +"  |   "+Bytes.format(dataStored));
			currentLength++;
		}else if(bit==1){
			byte mask = (byte)(1<<(Frame.LENGTH-currentLength));
			
			dataStored = (byte)(dataStored | mask);
			currentLength++;
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

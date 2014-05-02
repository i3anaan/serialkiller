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
		currentLength = 0;
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
		//Hier was vroeger een off by one error
		//(return currentLength==Frame.LENGTH;)
		//Hierdoor start currentLength nu op 0, en heeft nextBit/add een -1;
		return currentLength==Frame.LENGTH;
	}
	
	
	public byte nextBit(){
		//System.out.println("Returning bit: "+(byte)((dataStored>>(currentLength-1))&1));
		return (byte)((dataStored>>(currentLength-1))&1);
	}
	
	public void removeBit(){
		currentLength--;
	}
	
	public void add(byte bit){
		if(bit==0){
			byte mask = (byte)~(1<<(Frame.LENGTH-currentLength-1));
			dataStored = (byte)(dataStored & mask);
			//System.out.println(Bytes.format((byte)mask) +"  |   "+Bytes.format(dataStored));
			currentLength++;
		}else if(bit==1){
			byte mask = (byte)(1<<(Frame.LENGTH-currentLength-1));
			
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

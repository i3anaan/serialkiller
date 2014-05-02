package link;

import java.util.BitSet;

import util.Bytes;

/**
 * Sets starting from the left (MSB), reads starting from the right (LSB).
 * @author I3anaan
 *
 */
public class Frame {

	protected BitSet dataStored;
	
	public static final int PAYLOAD_SIZE = 80; //In bits;
	
	//Imagine this as a pointer pointing to the end of the bit sequence.
	//To the left is the last placed, valid, bit.
	//To the right a new bit would be placed;
	//XXXXXXXXXXXXX ---------
	//-------------^---------
	//-------------|---------
	int currentLength = 0;
	
	public Frame(){
		dataStored = new BitSet(PAYLOAD_SIZE);
		currentLength = 0;
	}
	
	public Frame(BitSet data) throws FrameSizeTooSmallException{
		if(data.size()>PAYLOAD_SIZE){
			throw new FrameSizeTooSmallException();
		}
		dataStored = new BitSet(PAYLOAD_SIZE);
		dataStored.set(0, dataStored.size());
		dataStored.and(data);
		currentLength = data.size(); //off by 1 danger zone
	}
	
	/**
	 * @require 0<length<=Frame.LENGTH;
	 * @param data
	 * @param length
	 */
	public Frame(BitSet data, int length) throws FrameSizeTooSmallException{
		if(data.size()>PAYLOAD_SIZE){
			throw new FrameSizeTooSmallException();
		}
		dataStored = new BitSet(PAYLOAD_SIZE);
		dataStored.set(0, dataStored.size());
		dataStored.and(data);
		currentLength = length; //off by 1 danger zone
	}
	
	
	public boolean isComplete(){
		//Hier was vroeger een off by one error
		//(return currentLength==Frame.LENGTH;)
		//Hierdoor start currentLength nu op 0, en heeft nextBit/add een -1;
		return currentLength==Frame.PAYLOAD_SIZE;
	}
	
	
	public byte nextBit(){
		//System.out.println("Returning bit: "+(byte)((dataStored>>(currentLength-1))&1));
		boolean bit = dataStored.get(currentLength);
		currentLength++;
		if(bit){
			return 1;
		}else{
			return 0;
		}
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

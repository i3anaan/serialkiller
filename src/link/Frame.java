package link;

public class Frame {

	byte dataStored;
	
	public static final int LENGTH = 8;
	int currentLength = 0;
	
	
	public Frame(byte data){
		dataStored = data;
		currentLength = 8;
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
		/*if(bit==0){
			byte 1<<(Frame.LENGTH-currentLength-1);
			dataStored = dataStored
		}else if(bit==1){
			
		}else{
			System.out.println("Adding invalid bit!");
		}*/
		
	}
	
	
}

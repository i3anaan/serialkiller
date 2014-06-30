package link.angelmaker.bitexchanger;

public class NonInvertingBitExchanger extends SimpleBitExchanger {

	@Override
	public byte adaptBitToPrevious(byte previousByte,boolean nextData) {
		if(nextData == ((previousByte&1)==1)){
			return (byte)(((previousByte&2)^2)|(nextData ? 1 : 0));
		}else{
			return (byte)((previousByte&2)|(nextData ? 1 : 0));
		}
		
	}
	
	
	@Override
	public String toString(){
		return "NonInvertingBitExchanger";
	}
}

package link.angelmaker.bitexchanger;

import link.angelmaker.nodes.Node;

public class HighSpeedBitExchanger extends SimpleBitExchanger {

	/*
	 * XD
	 * D = data bit, always contains 1 data bit
	 * X = extra bit, can be clock bit or extra data bit, depending on previousByte received and databit D in this byte.
	 */
	
	/**
	 * @param nextData The next dataBit to be send.
	 * @return	The byte representing this dataBit to be placed on the physical layer.
	 */
	@Override
	public byte adaptBitToPrevious(byte previousByte,boolean nextData) {
		if((previousByte&1)==(nextData ? 1 : 0)){
			//no change, use extra bit as clock bit.
			//System.out.println("Adapt " + (nextData ? "1":"0")+ " To: "+Bytes.format(previousByte)+"\tResult: "+Bytes.format((byte)((nextData ? 1 : 0)|((previousByte&2) ^ 2))));
			return (byte)((nextData ? 1 : 0)|((previousByte&2) ^ 2));
		}else{
			//Already changes, use extra bit as data bit
			return (byte)((nextData ? 1 : 0)|(getNextBitToSend() ? 2 :0));
		}
	}
	/**
	 * Test method
	 * @param previousByte
	 * @param nextData
	 * @param extraDataBit
	 * @return
	 */
	public byte adaptBitToPrevious(byte previousByte,boolean nextData,boolean extraDataBit) {
		if((previousByte&1)==(nextData ? 1 : 0)){
			//no change, use extra bit as clock bit.
			//System.out.println("Use clock bit");
			return (byte)((nextData ? 1 : 0)|(previousByte&2) ^ 2);
		}else{
			//Already changes, use extra bit as data bit
			//System.out.println("Add extra data bit");
			//System.out.println(Bytes.format((byte)((nextData ? 1 : 0)|(extraDataBit ? 2 :0))));
			return (byte)((nextData ? 1 : 0)|(extraDataBit ? 2 :0));
		}
	}
	
	@Override
	public byte getNextByteToSend(){
		return adaptBitToPrevious(previousByteSent,getNextBitToSend());
	}
	
	/**
	 * @param input The byte read from the physical layer.
	 * @return	The data bit this byte represents.
	 */
	@Override
	public boolean[] extractBitFromInput(byte previousByte, byte input){
		//System.out.println("Old: "+Bytes.format(previousByte)+"\tNew: "+Bytes.format(input));
		if((previousByte&1)==(input&1)){
			//no change, used extra bit as clock bit.
			boolean[] arr = new boolean[1];
			arr[0] = (input&1)==1;
			return arr;
		}else{
			//Already changes, use extra bit as data bit
			boolean[] arr = new boolean[2];
			arr[0] = (input&1)==1;
			arr[1] = (input&2)==2;
			return arr;
		}
	}
	
	@Override
	public String toString(){
		return "HighSpeedBitExchanger";
	}
	
}

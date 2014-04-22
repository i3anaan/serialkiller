package link;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lpt.Lpt;

public class LinkLayer {

	private Lpt lpt;
	private byte oldByte = Byte.MAX_VALUE;
	
	private static final byte[] testBytes = {0,32,16,48,0,32,16,48};

	public static void main(String[] args) {
		LinkLayer linkLayer = new LinkLayer(new Lpt());
		//System.out.println(Integer.toBinaryString(-52));
		//linkLayer.sendByte((byte) -52 );
		//System.out.println("Reading byte");
		//linkLayer.testReadByte(testBytes);
		
		//linkLayer.testReadByte(linkLayer.testSendByte((byte)-50));
		linkLayer.sendFlag();
	}

	public LinkLayer(Lpt lpt) {
		this.lpt = lpt;
		oldByte = lpt.readLPT();
	}

	public void sendByte(byte data) {
		for (int i = 0; i <8; i=i+1) {
			byte bit = (byte)(((data>>i) & 1));
			byte aBit = (byte)(bit ^ ((i%2)*2));
			System.out.println("bit:\t"+Integer.toBinaryString(bit));
			System.out.println("Abit:\t"+Integer.toBinaryString(aBit));
			//System.out.println(aBit<<4);
			//Stuurd minst significante bit eerst.
			System.out.println("Sending:   "+aBit);
			lpt.writeLPT(aBit);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*public byte[] testSendByte(byte data){
		byte[] fullData = new byte[8];
		for (int i = 0; i <8; i=i+1) {
			byte bit = (byte)(((data>>i) & 1));
			byte aBit = (byte)(bit ^ ((i%2)*2));
			
			fullData[i] = (byte)(aBit<<4);
			//Stuurd minst significante bit eerst.
			//lpt.writeLPT(bit);
			System.out.println("Sending:   "+(byte)(aBit<<4));
		}
		return fullData;
	}*/
	
	public byte readByte(){
		byte result = 0;
		int b = 0; //Incoming byte number
		//oldByte = Byte.MAX_VALUE;
		while(b<8){
			byte in = lpt.readLPT();
			
			if(in!=oldByte){
				//Nieuwe bit binnen.
				System.out.println("New Byte detected:"+in +"\t "+Integer.toBinaryString(in));
				result = (byte)((((in>>5 & 1)<<b) | result));
				oldByte = in;
				b++;
			}
		}
		System.out.println((int)(result) + "\t = \t"+Integer.toBinaryString(result));
		return result;
	}
	
	public void sendFlag(){
		byte flagBit1 = 3;
		byte flagBit2 = 2;
		System.out.println(Integer.toBinaryString(flagBit1));
		System.out.println(Integer.toBinaryString(flagBit2));
		
		for (int i = 0; i <20; i=i+1) {
			if(i%2==0){
				lpt.writeLPT(flagBit1);
			}else{
				lpt.writeLPT(flagBit2);
			}
		}
	}
	
	
	/*
	public byte testReadByte(byte[] fullData){
		byte result = 0;
		int b = 0; //Incoming byte number
		while(b<8){
			byte in = fullData[b];
			if(in!=oldByte){
				//Nieuwe bit binnen.
				//Leest minst significante bit eerst (want die wordt ook eerst gestuurd)
				result = (byte)((((in>>4 & 1)<<b) | result));
				b++;
			}
		}
		System.out.println((int)(result) + "\t = \t"+Integer.toBinaryString(result));
		return result;
	}*/

}

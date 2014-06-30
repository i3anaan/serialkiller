package test;

import com.google.common.base.Charsets;

import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import phys.LptErrorHardwareLayer;
import phys.PhysicalLayer;

public class ReceiveTest {	
	public static void main(String[] args) {
		PhysicalLayer phys = new LptErrorHardwareLayer();
		FrameLinkLayer am = new AngelMaker(phys);
		System.out.println(am);
		System.out.println("BEGIN TEST");

		int bytesReceivedCount=0;
		int bytesCorrectCount=0;
		long startTime = System.currentTimeMillis();
		while (true) {
			byte[] arr = am.readFrame();
			if (arr.length > 0) {
				for(int i=0;i<arr.length;i++){
					if(arr[i]==SendTest.STRING_TO_SEND.charAt(bytesReceivedCount % SendTest.STRING_TO_SEND.length())){
						bytesCorrectCount++;
					}	
					bytesReceivedCount++;
				}
				//System.out.print(new String(arr,Charsets.US_ASCII));
				int i = 0;
				if((bytesReceivedCount % SendTest.STRING_TO_SEND.length())==0 && System.currentTimeMillis()-startTime >= i*2000){
					System.out.print("\n["+bytesCorrectCount+"/"+bytesReceivedCount+"]\tSpeed (bytes/s): "+((double)bytesReceivedCount/(((double)System.currentTimeMillis()-(double)startTime)/1000.0))+"\n");
					i++;
				}
			}
		}
		
	}
}

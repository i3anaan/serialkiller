package test;

import com.google.common.base.Charsets;

import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import phys.LptErrorHardwareLayer;
import phys.PhysicalLayer;


public class DuplexTest {
	public static final String STRING_TO_SEND = "Such test, such amazing, wow, up to 420 gigadoge! #swag\n";
	public static void main(String[] args) {
		DuplexTest tester = new DuplexTest();
		tester.test();
	}
	public void test(){
		PhysicalLayer phys = new LptErrorHardwareLayer();
		AngelMaker am = new AngelMaker(phys);
		System.out.println(am);
		System.out.println("BEGIN TEST");
	
		Sender sender = new Sender(am);
		sender.start();
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
				int i = 0;
				if((bytesReceivedCount % SendTest.STRING_TO_SEND.length())==0 && System.currentTimeMillis()-startTime >= i*2000){
					System.out.print("\n["+bytesCorrectCount+"/"+bytesReceivedCount+"]\tSpeed (bytes/s): "+((double)bytesReceivedCount/(((double)System.currentTimeMillis()-(double)startTime)/1000.0))+"\n");
					i++;
				}
			}
		}
	}
	
	class Sender extends Thread{
		AngelMaker am;
		public Sender(AngelMaker am){
			this.am = am;
		}
		
		public void run(){
			System.out.println(am.toString());
			byte[] bytesToSend = STRING_TO_SEND.getBytes(Charsets.US_ASCII);
			System.out.println(STRING_TO_SEND+"\n["+STRING_TO_SEND.length()+","+bytesToSend.length+"]\n");
			System.out.println("START SENDING");
			
			while(true){
				for(int i=0;i<bytesToSend.length;i++){
					am.sendFrame(new byte[]{bytesToSend[i]});
				}			
			}
		}
	}
}

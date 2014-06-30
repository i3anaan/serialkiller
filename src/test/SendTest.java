package test;
import com.google.common.base.Charsets;

import phys.LptErrorHardwareLayer;
import phys.PhysicalLayer;
import util.Bytes;
import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
public class SendTest {

	public static final String STRING_TO_SEND = "Such test, such amazing, wow, up to 420 gigadoge! #swag\n";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PhysicalLayer phys = new LptErrorHardwareLayer();
		FrameLinkLayer am = new AngelMaker(phys);
		
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
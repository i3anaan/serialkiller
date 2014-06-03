package test;
import com.google.common.base.Charsets;

import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
public class SendTest {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PhysicalLayer phys = new LptHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		FrameLinkLayer am = new AngelMaker(phys, null, manager, new SimpleBitExchanger(phys, manager));
		System.out.println(am.toString());
		String stringToSend = "Sending:\nSuch test, such amazing, wow, up to 420 gigadoge! #swag\n";
		byte[] bytesToSend = stringToSend.getBytes(Charsets.US_ASCII);
		System.out.println(stringToSend+"\n["+stringToSend.length()+","+bytesToSend.length+"]\n");
		System.out.println("START SENDING");
		
		while(true){
			am.sendFrame(bytesToSend);
		}
	}
}

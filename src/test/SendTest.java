package test;
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
		PhysicalLayer phys = new LptErrorHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		FrameLinkLayer am = new AngelMaker(phys, null, manager, new SimpleBitExchanger(phys, manager));
		System.out.println(am.toString());
		System.out.println("Sending:\nSuch test, such amazing, wow, up to 420 gigadoge! #swag\n");
		System.out.println("START SENDING");
		
		while(true){
			am.sendFrame("Such test, such amazing, wow, up to 420 gigadoge! #swag\n".getBytes());
		}
	}
}

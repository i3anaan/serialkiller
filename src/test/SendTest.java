package test;
import phys.LptHardwareLayer;
import link.FrameLinkLayer;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
public class SendTest {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FrameLinkLayer jkr = new JackTheRipper(new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new LptHardwareLayer())));
		System.out.println(jkr.toCoolString());
		System.out.println("Sending:  Such test, such amazing, wow, up to 420 gigadoge! #swag\n");
		System.out.println("START SENDING");
		
		while(true){
			jkr.sendFrame("Such test, such amazing, wow, up to 420 gigadoge! #swag\n".getBytes());
		}
	}
}

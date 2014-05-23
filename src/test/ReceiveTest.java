package test;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import phys.LptErrorHardwareLayer;

public class ReceiveTest {

	/*public static void main(String[] args) {
		DCFDXLLSSReadSendManager2000 ll = new DCFDXLLSSReadSendManager2000(
				new DelayCorrectedFDXLinkLayerSectionSegment(
						new LptErrorHardwareLayer()));
		String received = "";
		System.out.println("BEGIN TEST");
		
		while (true) {
			byte b = ll.readUnit().b;
			received = received + (char) (b & 0xFF);
			System.out.println("Received so far:\n"+received);
		}
	}*/
}

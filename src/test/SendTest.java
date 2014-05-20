package test;

import com.google.common.base.Charsets;

import phys.LptErrorHardwareLayer;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
public class SendTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DCFDXLLSSReadSendManager2000 ll = new DCFDXLLSSReadSendManager2000(
				new DelayCorrectedFDXLinkLayerSectionSegment(
						new LptErrorHardwareLayer()));
		System.out.println("Start Sending");
		ll.setRun(true);
		int count = 0;
		while (count<3) {
			// for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
			// ll.sendByte((byte) b);
			// }
			for (byte b : "Dit is een test message. 112233\n"
					.getBytes(Charsets.UTF_8)) {
				ll.sendByte((byte) b);
			}
			count++;
			
		}
		System.out.println("Stopping exchanger thread");
		//ll.setRun(false);
	}
}

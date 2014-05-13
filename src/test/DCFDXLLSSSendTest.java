package test;

import com.google.common.base.Charsets;

import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.FileHardwareLayer;
import phys.LptHardwareLayer;
import util.Bytes;
import link.DCFDXLLSSReadSendManager2000;
import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.HighSpeedHDXLinkLayer;
import link.LinkLayer;

public class DCFDXLLSSSendTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DCFDXLLSSReadSendManager2000 ll = new DCFDXLLSSReadSendManager2000(
				new DelayCorrectedFDXLinkLayerSectionSegment(
						new LptHardwareLayer()));
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
		ll.setRun(false);
	}
}

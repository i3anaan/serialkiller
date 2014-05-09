package test;

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
		while (true) {
			for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
				ll.sendByte((byte) b);
			}
		}
	}
}

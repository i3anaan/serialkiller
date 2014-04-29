package test;

import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.LptHardwareLayer;
import util.Bytes;
import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.HighSpeedHDXLinkLayer;
import link.LinkLayer;

public class AlwaysSendingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkLayer ll = new DelayCorrectedFDXLinkLayerSectionSegment(new CleanStartPhysicalLayer(new LptHardwareLayer()));
		
		while (true) {
			for (byte b = 1; b < Byte.MAX_VALUE; b++) {
				ll.sendByte((byte) b);
			}
		}
	}
}

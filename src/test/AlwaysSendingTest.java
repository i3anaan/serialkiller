package test;

import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.HardwareLayer;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import util.Bytes;
import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.HighSpeedHDXLinkLayer;
import link.LinkLayer;

public class AlwaysSendingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HardwareLayer ll = new LptErrorHardwareLayer();
		
		while (true) {
				ll.sendByte((byte) 11);
		}
	}
}

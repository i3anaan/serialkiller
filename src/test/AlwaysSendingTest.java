package test;

import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.LptHardwareLayer;
import util.Bytes;
import link.HighSpeedHDXLinkLayer;

public class AlwaysSendingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HighSpeedHDXLinkLayer hshdxll = new HighSpeedHDXLinkLayer(new DumpingPhysicalLayer(new DebouncePhysicalLayer(new CleanStartPhysicalLayer(new DelayPhysicalLayer(new LptHardwareLayer(), 1000)))));
		
		while (true) {
			for (byte b = 1; b < Byte.MAX_VALUE; b++) {
				System.out.println(Bytes.format((byte) ((byte) 22 + b)));
				hshdxll.sendByte((byte) ((byte) 22 + b));
			}
		}
	}
}

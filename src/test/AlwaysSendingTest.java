package test;

import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.LptHardwareLayer;
import link.HighSpeedHDXLinkLayer;

public class AlwaysSendingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HighSpeedHDXLinkLayer hshdxll = new HighSpeedHDXLinkLayer(new DebouncePhysicalLayer(new CleanStartPhysicalLayer(new LptHardwareLayer())));
		
		while (true) {
			for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
				hshdxll.sendByte(b);
			}
		}
	}
}

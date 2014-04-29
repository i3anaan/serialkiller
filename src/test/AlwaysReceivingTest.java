package test;

import link.HighSpeedHDXLinkLayer;
import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.LptHardwareLayer;
import util.Bytes;

public class AlwaysReceivingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HighSpeedHDXLinkLayer hshdxll = new HighSpeedHDXLinkLayer(new DebouncePhysicalLayer(new CleanStartPhysicalLayer(new LptHardwareLayer())));

		while (true) {
			byte b = hshdxll.readByte();
			System.out.printf("%s %d\n", Bytes.format(b), b);
		}
	}

}

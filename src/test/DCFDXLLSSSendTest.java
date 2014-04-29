package test;

import link.HighSpeedHDXLinkLayer;
import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.LptHardwareLayer;

public class DCFDXLLSSSendTest {
	public static void main(String[] args) {
		HighSpeedHDXLinkLayer hshdxll = new HighSpeedHDXLinkLayer(new DumpingPhysicalLayer(new DebouncePhysicalLayer(new CleanStartPhysicalLayer(new DelayPhysicalLayer(new LptHardwareLayer(), 1000)))));
		
		while (true) {
			for (byte b = 1; b < Byte.MAX_VALUE; b++) {
				hshdxll.sendByte((byte) 22);
			}
		}
	}
}

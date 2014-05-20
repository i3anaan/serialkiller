package test;

import phys.HardwareLayer;
import phys.LptErrorHardwareLayer;

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

package test;

import link.DCFDXLLSSReadSendManager2000;
import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.LinkLayer;
import phys.CleanStartPhysicalLayer;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import util.Bytes;

public class DCFDXLLSSReceiveTest {

	public static void main(String[] args) {
		DCFDXLLSSReadSendManager2000 ll = new DCFDXLLSSReadSendManager2000(
				new DelayCorrectedFDXLinkLayerSectionSegment(
						new LptErrorHardwareLayer()));
		byte old = -1;
		String received = "";
		System.out.println("BEGIN TEST");
		
		while (true) {
			byte b = ll.readByte();
			System.out.print((char) (b & 0xFF));
		}
	}
}

package test;

import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.LinkLayer;
import phys.CleanStartPhysicalLayer;
import phys.LptHardwareLayer;
import util.Bytes;

public class DCFDXLLSSReceiveTest {

	
	public static void main(String[] args) {
		LinkLayer ll = new DelayCorrectedFDXLinkLayerSectionSegment(new CleanStartPhysicalLayer(new LptHardwareLayer()));
		byte old = -1;
		while (true) {
			byte b = ll.readByte();
			if(b!=old){
				System.out.println(Bytes.format(b));
				old = b;
			}
			
		}
	}
}

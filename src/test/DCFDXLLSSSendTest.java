package test;

import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.FileHardwareLayer;
import phys.LptHardwareLayer;
import util.Bytes;
import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.HighSpeedHDXLinkLayer;
import link.LinkLayer;

public class DCFDXLLSSSendTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DelayCorrectedFDXLinkLayerSectionSegment ll = new DelayCorrectedFDXLinkLayerSectionSegment(new CleanStartPhysicalLayer(new FileHardwareLayer("C:\\Users\\I3anaan\\Desktop\\New Text Document.txt")));
		System.out.println("Start Sending");
		System.out.println(Bytes.format((byte)-123));
		//while (true) {
			for (byte b = 1; b < 2; b++) {
				ll.sendByte((byte) -123);
				ll.readByte();
				//System.out.println(ll.readByte());
				ll.exchangeFrame();
			}
		//}
	}
}

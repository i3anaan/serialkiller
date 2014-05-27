package test;

import com.google.common.base.Charsets;

import link.FrameLinkLayer;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
import phys.LptHardwareLayer;

public class ReceiveTest {

	public static void main(String[] args) {
		FrameLinkLayer jkr = new JackTheRipper(new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new LptHardwareLayer())));
		System.out.println(jkr.toCoolString());
		System.out.println("BEGIN TEST");
		
		while (true) {
			String received = new String(jkr.readFrame(),Charsets.UTF_8);
			System.out.println(received);
		}
	}
}

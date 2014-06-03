package test;

import com.google.common.base.Charsets;
import common.Graph;

import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import util.Bytes;

public class ReceiveTest {

	public static void main(String[] args) {
		PhysicalLayer phys = new LptHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		FrameLinkLayer am = new AngelMaker(phys, null, manager,
				new SimpleBitExchanger(phys, manager));
		System.out.println(am);
		System.out.println("BEGIN TEST");

		while (true) {
			byte[] receivedBytes = am.readFrame();
			
			if (receivedBytes.length > 0) {
				System.out.println(Bytes.format(receivedBytes[0]));
				String received = new String(receivedBytes, Charsets.US_ASCII);
				//System.out.println(received);
			}
		}
	}
}

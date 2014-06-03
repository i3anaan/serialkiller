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

		byte[] totalReceived = new byte[65];
		int currentIndex = 0;
		
		while (true) {
			byte[] receivedBytes = am.readFrame();
			
			if (receivedBytes.length > 0) {
				System.arraycopy(receivedBytes, 0, totalReceived, currentIndex, receivedBytes.length);
				currentIndex = currentIndex + receivedBytes.length;
				for(byte b : receivedBytes){
					System.out.println(Bytes.format(b));
				}
				String received = new String(totalReceived, Charsets.UTF_8);
				System.out.println(received);
				//Se, 420 gigadoge! #swag

			}
		}
	}
}

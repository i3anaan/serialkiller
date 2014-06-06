package test;

import java.util.ArrayList;

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
import util.BitSet2;
import util.Bytes;

public class ReceiveTest {

	public static void main(String[] args) {
		PhysicalLayer phys = new LptHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		FrameLinkLayer am = new AngelMaker(phys, null, manager,
				new SimpleBitExchanger(phys, manager));
		System.out.println(am);
		System.out.println("BEGIN TEST");

		
		int currentIndex = 0;
		BitSet2 totalReceived = new BitSet2();
		while (true) {
			byte[] receivedBytes = am.readFrame();
			if (receivedBytes.length > 0 && totalReceived.length()<500) {
				totalReceived = BitSet2.concatenate(totalReceived,new BitSet2(receivedBytes));
				currentIndex = currentIndex + receivedBytes.length;
				for(byte b : receivedBytes){
					System.out.println(Bytes.format(b));
				}
				String received = new String(totalReceived.toByteArray(), Charsets.UTF_8);
				System.out.println(received);
				//Se, 420 gigadoge! #swag
				if(currentIndex == receivedBytes.length-1){
					currentIndex= 0;
				}

			}
		}
		
	}
}

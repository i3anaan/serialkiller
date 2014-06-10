package test;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.base.Charsets;

import common.Graph;
import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.FrameNode;
import link.angelmaker.nodes.Node;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import util.BitSet2;
import util.Bytes;

public class ReceiveTest {

	/*
	 * STATUS
	 * werkt momenteel op foutloze link (stability 400)
	 * Error LPT faalt opeens, onduidelijk hoe, hij krijgt steeds timeouts.
	 * Buffer size tweaken.
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	public static void main(String[] args) {
		PhysicalLayer phys = new LptErrorHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		Node node = new FrameNode<Node>(null,3);
		node = new FlaggingNode(null,8);
		FrameLinkLayer am = new AngelMaker(phys, node, manager,
				new SimpleBitExchanger(phys, manager));
		System.out.println(am);
		System.out.println("BEGIN TEST");

		BitSet2 totalReceived = new BitSet2();
		while (true) {
			byte[] receivedBytes = am.readFrame();
			//System.out.println("received:"+Arrays.toString(receivedBytes));
			if (receivedBytes.length > 0) {
				totalReceived = BitSet2.concatenate(totalReceived,new BitSet2(receivedBytes));
				String received = new String(totalReceived.toByteArray(), Charsets.US_ASCII);
				System.out.println(received);

			}
		}
		
	}
}

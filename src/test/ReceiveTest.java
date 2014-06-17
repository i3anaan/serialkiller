package test;

import com.google.common.base.Charsets;
import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.manager.ThreadedAMManagerServer;
import link.angelmaker.nodes.FillablePureNode;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.FrameNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.PureNode;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import util.BitSet2;

public class ReceiveTest {	
	public static void main(String[] args) {
		PhysicalLayer phys = new LptHardwareLayer();
		AMManager manager = new ThreadedAMManagerServer();
		Node node = new FrameNode<Node>(null,3);
		node = new FlaggingNode(null,8);
		FrameLinkLayer am = new AngelMaker(phys, node, manager,
				new SimpleBitExchanger());
		System.out.println(am);
		System.out.println("BEGIN TEST");

		BitSet2 totalReceived = new BitSet2();
		while (true) {
			byte[] receivedBytes = am.readFrame();
			if (receivedBytes.length > 0) {
				totalReceived = BitSet2.concatenate(totalReceived,new BitSet2(receivedBytes));
				String received = new String(totalReceived.toByteArray(), Charsets.US_ASCII);
				System.out.println("#######################################################\n"+received);
			}
		}
		
	}
}

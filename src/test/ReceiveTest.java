package test;

import com.google.common.base.Charsets;

import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.manager.ConstantRetransmittingManager;
import link.angelmaker.manager.ThreadedAMManagerServer;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.PureNode;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import sun.nio.cs.US_ASCII;
import util.BitSet2;

public class ReceiveTest {	
	public static void main(String[] args) {
<<<<<<< HEAD
		PhysicalLayer phys = new LptErrorHardwareLayer();
		AMManager manager = new ThreadedAMManagerServer();
		Node node = new FrameNode<Node>(null,3);
		node = new FlaggingNode(null,8);
		FrameLinkLayer am = new AngelMaker(phys, node, manager,
				new SimpleBitExchanger());
=======
		PhysicalLayer phys = new LptHardwareLayer();
		FrameLinkLayer am = new AngelMaker(phys);
		
>>>>>>> 6a2590102431ab9ce1ac7284789cde0a8cdc9c93
		System.out.println(am);
		System.out.println("BEGIN TEST");

		int bytesReceivedCount=0;
		int bytesCorrectCount=0;
		while (true) {
			byte[] arr = am.readFrame();
			if (arr.length > 0) {
				for(int i=0;i<arr.length;i++){
					if(arr[i]==SendTest.STRING_TO_SEND.charAt(bytesReceivedCount % SendTest.STRING_TO_SEND.length())){
						bytesCorrectCount++;
					}	
					bytesReceivedCount++;
				}
				System.out.print(new String(arr,Charsets.US_ASCII));
				if((bytesReceivedCount % SendTest.STRING_TO_SEND.length())==0){
					System.out.print("\n["+bytesCorrectCount+"/"+bytesReceivedCount+"]\n");
				}
			}
		}
		
	}
}

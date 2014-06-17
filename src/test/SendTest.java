package test;
import com.google.common.base.Charsets;

import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import util.Bytes;
import link.FrameLinkLayer;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.nodes.FillablePureNode;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.FrameNode;
import link.angelmaker.nodes.Node;
public class SendTest {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PhysicalLayer phys = new LptHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		Node node = new FrameNode<Node>(null,3);
		node = new FillablePureNode(null,8);
		FrameLinkLayer am = new AngelMaker(phys, node, manager,
				new SimpleBitExchanger());
		System.out.println(am.toString());
		String stringToSend = "Such test, such amazing, wow, up to 420 gigadoge! #swag\n";
		byte[] bytesToSend = stringToSend.getBytes(Charsets.US_ASCII);
		System.out.println(stringToSend+"\n["+stringToSend.length()+","+bytesToSend.length+"]\n");
		for(byte b : bytesToSend){
			System.out.println(Bytes.format(b));
		}
		System.out.println("START SENDING");
		
		while(true){
			am.sendFrame(bytesToSend);
		}
	}
}
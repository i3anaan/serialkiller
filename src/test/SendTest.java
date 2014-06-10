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
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.FrameNode;
import link.angelmaker.nodes.Node;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
public class SendTest {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PhysicalLayer phys = new LptHardwareLayer();
		AMManager manager = new BlockingAMManagerServer();
		Node node = new FrameNode<Node>(null,3);
		node = new FlaggingNode(null,8);
		FrameLinkLayer am = new AngelMaker(phys, node, manager,
				new SimpleBitExchanger(phys, manager));
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
/*
01010011
01110101
01100011
01101000
00100000
01110100
01100101
01110011
01110100
00101100
*/
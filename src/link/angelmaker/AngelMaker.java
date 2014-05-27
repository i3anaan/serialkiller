package link.angelmaker;

import util.BitSet2;
import common.Stack;
import common.Startable;
import link.FrameLinkLayer;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.BitExchangerManager;
import link.angelmaker.bitexchanger.BlockingExchangerManager;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;

/**
 *http://en.wikipedia.org/wiki/Angel_Makers_of_Nagyr%C3%A9v
 * @author I3anaan
 *
 */
//TODO implement this class more serious.
public class AngelMaker extends FrameLinkLayer implements Startable{
	public static final Node TOP_NODE_IN_USE = new BasicLeafNode(null);
	BitExchanger bitExchanger = null;
	BitExchangerManager bitExchangerManager = null;
	
	
	@Override
	public void sendFrame(byte[] data) {
		Node newNode = TOP_NODE_IN_USE.getClone();
		newNode.giveOriginal(new BitSet2(data));
		try {
			bitExchangerManager.sendNode(newNode);
		} catch (NotSupportedNodeException e) {
			//Incompatible modules.
			//TODO maybe make this impossible, this is kind of ugly.
			e.printStackTrace();
		}		
	}

	@Override
	public byte[] readFrame() {
		return bitExchangerManager.readNode().getOriginal().toByteArray();
	}

	@Override
	public String toCoolString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Thread start(Stack stack) {
		bitExchangerManager = new BlockingExchangerManager(bitExchanger);
		return null;
	}

}

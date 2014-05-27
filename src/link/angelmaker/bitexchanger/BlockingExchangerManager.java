package link.angelmaker.bitexchanger;

import link.angelmaker.AngelMaker;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.Node;
import util.BitSet2;

/**
 * Takes a Node, and sends this over the current BitExchanger.
 * ReadNode() does block.
 * 
 * @author I3anaan
 *
 */
public class BlockingExchangerManager implements BitExchangerManager{
	BitExchanger exchanger;
	
	public BlockingExchangerManager(BitExchanger exchanger){
		this.exchanger = exchanger;
	}

	@Override
	public void sendNode(Node node) {
		exchanger.sentBits(node.getConverted());
	}

	@Override
	public Node readNode() {
		Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
		while(!node.isComplete()){
			BitSet2 received = exchanger.readBits();
			if(received.length()!=0){
				node.giveConverted(received);
			}
		}
		return node;
	}
	
}

package link.angelmaker.manager;

import com.sun.media.sound.AlawCodec;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
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
public class BlockingAMManager implements AMManager{
	BitExchanger exchanger;
	boolean enabled = false;
	Node lastNodeSend;
	
	public BlockingAMManager(){
	}
	@Override
	public void setExchanger(BitExchanger exchanger){
		this.exchanger = exchanger;
	}
	@Override
	public void enable(){
		enabled = true;
	}
	
	@Override
	public void sendNode(Node node) {
		if(node.isReady()){
			lastNodeSend = node;
			exchanger.sendBits(node.getConverted());
		}else{
			AngelMaker.logger.error("Node trying to send is not ready to be send");
			//TODO;
		}
	}

	@Override
	public Node readNode() {
		Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
		while(!node.isReady() && !node.isFull()){
			BitSet2 received = exchanger.readBits();
			if(received.length()!=0){
				node.giveConverted(received);
			}
		}
		if(node.isFull() && !node.isReady()){
			AngelMaker.logger.error("Node full, but not ready to be read.");
			//TODO;
		}
		return node;
	}
	
	@Override
	public Node getCurrentSendingNode() {
		return lastNodeSend;
	}
	
	
	
	
}

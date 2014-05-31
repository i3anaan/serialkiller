package link.angelmaker.manager;

import com.sun.media.sound.AlawCodec;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.Node;
import util.BitSet2;
import util.Bytes;

/**
 * Takes a Node, and sends this over the current BitExchanger.
 * ReadNode() does block.
 * Will fail if the datachunks trying to be send do not make the Node ready.
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
	public void sendBytes(byte[]  bytes) {
		Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
		//System.out.println("BlockingAMManager: "+Bytes.format(bytes[0]));
		//System.out.println("BlockingAMManager: "+new BitSet2(bytes));
		node.giveOriginal(new BitSet2(bytes));
		//AngelMaker.logger.debug("Sending new Node: "+node);
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
		do{
			BitSet2 received = exchanger.readBits();
			if(received.length()!=0){
				node.giveConverted(received);
			}
		}while(!(node.isReady() || node.isFull()));
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

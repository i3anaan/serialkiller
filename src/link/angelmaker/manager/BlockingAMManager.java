package link.angelmaker.manager;

import common.Graph;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
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
	Node lastNodeReceived;
	
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
		BitSet2 remaining = new BitSet2(bytes);
		while(remaining.length()>0){
			Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
			remaining = node.giveOriginal(remaining);
			if(node.isReady()){
				lastNodeSend = node;
				System.out.println("Node to be send:\t"+node);
				exchanger.sendBits(node.getConverted());
			}else{
				AngelMaker.logger.error("Node trying to send is not ready to be send");
				//TODO;
			}
		}
	}

	@Override
	public Node readNode() {
		System.out.println("Reading node...");
		Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
		lastNodeReceived = node;
		do{
			
			BitSet2 received = exchanger.readBits();
			if(received.length()>0){
				//System.out.println("Handing over bits:"+received);
				node.giveConverted(received);
			}else{
				//System.out.println("QueueIn Size: "+((SimpleBitExchanger)exchanger).queueIn.size());
				//System.out.println("QueueOut Size: "+((SimpleBitExchanger)exchanger).queueOut.size());
			}
		}while(!(node.isFull()));
		//System.out.println("Done filling");
		if(node.isFull() && !node.isReady()){
			AngelMaker.logger.error("Node full, but not ready to be read.");
			//TODO;
		}
		System.out.println("Done Reading Node: "+node);
		Graph.makeImage(Graph.getFullGraphForNode(node, true));
		return node;
	}
	
	@Override
	public Node getCurrentSendingNode() {
		return lastNodeSend;
	}	
	
	@Override
	public Node getCurrentReceivingNode() {
		return lastNodeReceived;
	}
}

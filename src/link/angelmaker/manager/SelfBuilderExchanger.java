package link.angelmaker.manager;

import java.util.concurrent.ArrayBlockingQueue;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;

/**
 * Threaded class that delegates frame building to the Node.
 * Constantly tries to build Nodes, puts it in public queues.
 * @author I3anaan
 * @Requires Node.SelfBuilding
 *
 *TODO make this also an interface?
 *
 */
public class SelfBuilderExchanger extends Thread{
	
	BitExchanger exchanger;
	public ArrayBlockingQueue<Node.SelfBuilding> queueOut;
	public ArrayBlockingQueue<Node.SelfBuilding> queueIn;
	private Node lastNodeSend;
	
	public SelfBuilderExchanger(BitExchanger exchanger){
		this.exchanger = exchanger;
	}
	public void exchangeNode(Node.SelfBuilding nodeToSend){
		Node.SelfBuilding received = (Node.SelfBuilding)AngelMaker.TOP_NODE_IN_USE.getClone();
		lastNodeSend = nodeToSend;
		received.buildSelf(exchanger,nodeToSend);
		
		try {
			if(received.isReady()){
				queueIn.put(received);
			}else{
				AngelMaker.logger.error("Node received is not ready to be read.");
				//TODO;
			}
		} catch (InterruptedException e) {
			//TODO halp?
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(true){
			try {
				exchangeNode(queueOut.take());
			} catch (InterruptedException e) {
				// TODO HALP
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return Node currently being send.
	 */
	public Node getCurrentSendingNode(){
		return lastNodeSend;
	}
}

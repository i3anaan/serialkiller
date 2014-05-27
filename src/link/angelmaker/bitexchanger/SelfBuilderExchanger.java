package link.angelmaker.bitexchanger;

import java.util.concurrent.ArrayBlockingQueue;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;
import link.angelmaker.nodes.SelfBuildingNode;

public class SelfBuilderExchanger extends Thread{
	
	MasterSlaveBitExchanger exchanger;
	public ArrayBlockingQueue<SelfBuildingNode.Duplex> queueOut;
	public ArrayBlockingQueue<SelfBuildingNode.Duplex> queueIn;
	
	public SelfBuilderExchanger(MasterSlaveBitExchanger exchanger){
		this.exchanger = exchanger;
	}
	public void exchangeNode(SelfBuildingNode.Duplex nodeToSend){
		SelfBuildingNode.Duplex received = (SelfBuildingNode.Duplex)AngelMaker.TOP_NODE_IN_USE.getClone();
		received.buildSelf(exchanger,nodeToSend);
		
		try {
			queueIn.put(received);
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
}

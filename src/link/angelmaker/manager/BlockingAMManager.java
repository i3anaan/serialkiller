package link.angelmaker.manager;

import common.Graph;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
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
	BitSet2 received;
	
	public BlockingAMManager(){
		received = new BitSet2();
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
				//System.out.println("Node to be send:\t"+node);
				exchanger.sendBits(node.getConverted());
			}else{
				AngelMaker.logger.error("Node trying to send is not ready to be send");
				//TODO;
			}
		}
	}

	@Override
	public byte[] readBytes() {
		BitSet2 result = new BitSet2();
		//System.out.println("AMManager readBytes START");
		received = BitSet2.concatenate(received,exchanger.readBits());
		//System.out.println("Received: "+received);
		while(received.length()>0){
			Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
			lastNodeReceived = node;
			do{
				received = BitSet2.concatenate(received,exchanger.readBits());
				if(received.length()>0){
					received = node.giveConverted(received);
				}
			}while(!(node.isFull()));
			if(node.isFull() && !node.isReady()){
				AngelMaker.logger.error("Node full, but not ready to be read.");
				//TODO;
			}
			//System.out.println(node);
			result.addAtEnd(node.getOriginal());
			
		}
		//System.out.println("AMManager readBytes END");
		return result.toByteArray();
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

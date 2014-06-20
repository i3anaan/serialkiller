package link.angelmaker.manager;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;

/**
 * Not blocking, self-building meaning it delegates the building to the node.
 * @author I3anaan
 * @Requires Node.SelfBuilding
 */
public class SelfBuilderAMManager implements AMManager {

	SelfBuilderExchanger sbExchanger;
	@Override
	public void setExchanger(BitExchanger exchanger){
		sbExchanger= new SelfBuilderExchanger(exchanger);
	}
	@Override
	public void enable(){
		sbExchanger.start();
	}
	
	/**
	 * Non blocking. Queues up the given node for sending.
	 * @param node Node to send.
	 */
	@Override
	public void sendBytes(byte[] bytes){
		Node node = AngelMaker.TOP_NODE_IN_USE.getClone();
		node.giveOriginal(new BitSet2(bytes));
		if(node instanceof Node.SelfBuilding){
			if(node.isReady()){
				try {
					sbExchanger.queueOut.put((Node.SelfBuilding) node);
				} catch (InterruptedException e) {
					// TODO HALP??
					e.printStackTrace();
				}
			}else{
				AngelMaker.logger.error("Node trying to send is not ready to be send");
				//TODO;
			}
		}else{
			throw new IncompatibleModulesException();
		}
	}

	
	/**
	 * Non blocking.
	 * @return null when nothing new to read, otherwise 1 Node.
	 * 
	 */
	@Override
	public byte[] readBytes() {
		return sbExchanger.queueIn.poll().getOriginal().toByteArray();
	}
	@Override
	public Node getCurrentSendingNode() {
		return sbExchanger.getCurrentSendingNode();
	}
	@Override
	public Node getCurrentReceivingNode() {
		// TODO Auto-generated method stub
		return sbExchanger.getCurrentReceivingNode();
	}

}

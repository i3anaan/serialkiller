package link.angelmaker.manager;

import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.SelfBuilderExchanger;
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
	public void sendNode(Node node) throws NotSupportedNodeException {
		if(node instanceof Node.SelfBuilding){
			try {
				sbExchanger.queueOut.put((Node.SelfBuilding) node);
			} catch (InterruptedException e) {
				// TODO HALP??
				e.printStackTrace();
			}
		}else{
			throw new NotSupportedNodeException();
		}
	}

	
	/**
	 * Non blocking.
	 * @return null when nothing new to read, otherwise 1 Node.
	 * 
	 */
	@Override
	public Node readNode() {
		return sbExchanger.queueIn.poll();
	}

}

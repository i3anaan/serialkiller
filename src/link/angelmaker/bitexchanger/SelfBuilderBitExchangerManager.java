package link.angelmaker.bitexchanger;

import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;
import link.angelmaker.nodes.SelfBuildingNode;
import link.angelmaker.nodes.SelfBuildingNode.Duplex;

/**
 * Not blocking, self-building meaning it delegates the building to the node.
 * @author I3anaan
 *
 */
public class SelfBuilderBitExchangerManager implements BitExchangerManager {

	SelfBuilderExchanger sbExchanger;
	
	public SelfBuilderBitExchangerManager(MasterSlaveBitExchanger exchanger){
		sbExchanger= new SelfBuilderExchanger(exchanger);
		sbExchanger.start();
	}
	
	@Override
	public void sendNode(Node node) throws NotSupportedNodeException {
		if(node instanceof SelfBuildingNode.Duplex){
			try {
				sbExchanger.queueOut.put((SelfBuildingNode.Duplex) node);
			} catch (InterruptedException e) {
				// TODO HALP??
				e.printStackTrace();
			}
		}else{
			throw new NotSupportedNodeException();
		}
	}

	@Override
	public Node readNode() {
		return sbExchanger.queueIn.poll();
	}

}

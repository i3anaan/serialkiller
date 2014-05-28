package link.angelmaker.manager;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.Node;


//TODO is it possible to nest this?
//i.e. Make BlockingAMManager.Server (without having a BlockingAMManger instance)


/**
 * Extends the BlockingAMManger to implement the Server interface.
 * This means that it can also deliver a Node on demand.
 * It does this by giving a clone of the TOP_NODE_IN_USE.
 * This Node should be empty, meaning asking the Converted BitSet2 should be filler.
 * This however requires the Node to be Fillable.
 * @author I3anaan
 * @Requires Node.Fillable
 */
public class BlockingAMManagerServer extends BlockingAMManager implements AMManager.Server{
	public BlockingAMManagerServer(){
		super();
	}
	
	@Override
	public void setExchanger(BitExchanger exchanger){
		this.exchanger = exchanger;
		if(exchanger instanceof BitExchanger.AlwaysSending && !(AngelMaker.TOP_NODE_IN_USE instanceof Node.Fillable)){
			throw new IncompatibleModulesException();
		}
	}

	@Override
	public Node getNextNode() {
		return AngelMaker.TOP_NODE_IN_USE.getClone();
	}
	
	@Override
	public String toString(){
		return "BlockingAMManagerServer";
	}
}

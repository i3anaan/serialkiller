package link.angelmaker;

import phys.diag.VirtualPhysicalLayer;
import util.BitSet2;
import common.Stack;
import common.Startable;
import link.FrameLinkLayer;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.ConsistentDuplexBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.FrameNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;
import log.Logger;
import log.LogMessage.Subsystem;

/**
 *http://en.wikipedia.org/wiki/Angel_Makers_of_Nagyr%C3%A9v
 * @author I3anaan
 *
 */
//TODO implement this class more serious.
public class AngelMaker extends FrameLinkLayer implements Startable{
	public static Node TOP_NODE_IN_USE;
	public static final Logger logger =  new Logger(Subsystem.LINK);
	public AMManager manager;
	public BitExchanger bitExchanger;
	
	public static void main(String[] args){
		//test
		AngelMaker am = new AngelMaker();
		am.start(null);
		//System.out.println(am);
		am.sendFrame(new byte[]{1,5,3,7,34,59});
	}
	
	@Override
	public void sendFrame(byte[] data) {
		Node newNode = TOP_NODE_IN_USE.getClone();
		newNode.giveOriginal(new BitSet2(data));
		System.out.println("NewNode = "+newNode);
		try {
			manager.sendNode(newNode);
		} catch (NotSupportedNodeException e) {
			//Incompatible modules.
			//TODO maybe make this impossible, this is kind of ugly.
			e.printStackTrace();
		}		
	}

	@Override
	public byte[] readFrame() {
		return manager.readNode().getOriginal().toByteArray();
	}

	@Override
	public String toCoolString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Thread start(Stack stack) {
		setup();
		return null;
	}
	
	
	public void setup(){
		//TODO severities set correct?
		
		logger.notice("Setting up ANGEL_MAKER");
		try{
		TOP_NODE_IN_USE = new FrameNode<Node>(null, 10);
		logger.info("Top Node build.");
		manager = new BlockingAMManagerServer();
		logger.info("Manager constructed.");
		bitExchanger = new ConsistentDuplexBitExchanger(new VirtualPhysicalLayer(), manager);
		logger.info("BitExchanger constructed.");
		manager.setExchanger(bitExchanger);
		logger.info("Handed exchanger to manager.");
		manager.enable();
		logger.info("Manager enabled.");
		
		logger.notice("All done and ready for use.");
		}catch(IncompatibleModulesException e){
			logger.bbq("Incompatible modules. ANGEL_MAKER could not start.");
			//e.printStackTrace();
		}
	}
	
	public String toString(){
		String s = "ANGEL_MAKER\n"
				+ "Consisting of:\n"
				+ "\tAMManager:\t"+manager.toString()
				+ "\n\tBitExchanger:\t"+bitExchanger.toString()
				+ "\n\tNode:\t\t"+TOP_NODE_IN_USE.toString()+"\n";
		
		return s;
	}

}

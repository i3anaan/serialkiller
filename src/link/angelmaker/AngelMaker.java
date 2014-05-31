package link.angelmaker;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import phys.PhysicalLayer;
import phys.diag.VirtualPhysicalLayer;
import util.BitSet2;
import util.Bytes;
import common.Graph;
import common.Stack;
import common.Startable;
import link.FrameLinkLayer;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.ConsistentDuplexBitExchanger;
import link.angelmaker.bitexchanger.DummyBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.FrameCeptionNode;
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
	
	public static Node TOP_NODE_IN_USE = new FrameCeptionNode<Node>(null, 0);
	public static final Logger logger =  new Logger(Subsystem.LINK);
	public AMManager manager;
	public BitExchanger bitExchanger;
	private Stack stack;
	
	public AngelMaker(PhysicalLayer phys){
		setup(phys);
	}
	
	
	@Override
	public void sendFrame(byte[] data) {
		//System.out.println("ANGEL_MAKER sending data: "+Bytes.format(data[0]));
		manager.sendBytes(data);
	}

	@Override
	public byte[] readFrame() {
		
		byte[] result = manager.readNode().getOriginal().toByteArray();
		//while(result.length==0){
		//	result = manager.readNode().getOriginal().toByteArray();
		//}
		//System.out.println("Returning bytes:  "+Arrays.toString(result));
		return result;
	}

	@Override
	public String toCoolString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Thread start(Stack stack) {
		this.stack = stack;
		setup(stack.physLayer);
		return null;
	}
	
	
	public void setup(PhysicalLayer phys){
		
		//TODO severities set correct?
		//TODO thread name on AMManger is TPPHandler, why is this?
		logger.info("Setting up ANGEL_MAKER");
		try{
		//TOP_NODE_IN_USE = new FrameNode<Node>(null, 10);
		TOP_NODE_IN_USE = new FrameCeptionNode<Node>(null, 0);
		logger.debug("Top Node build.");
		manager = new BlockingAMManagerServer();
		logger.debug("Manager constructed.");
		//bitExchanger = new ConsistentDuplexBitExchanger(phys, manager);
		bitExchanger = new DummyBitExchanger();
		logger.debug("BitExchanger constructed.");
		manager.setExchanger(bitExchanger);
		logger.debug("Handed exchanger to manager.");
		manager.enable();
		logger.debug("Manager enabled.");
		
		//System.out.println("\n\n"+Graph.getFullGraphForNode(TOP_NODE_IN_USE,true)+"\n\n");
		logger.info("All done and ready for use.");
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
	
	/**
	 * Can return null. Depends on AMManager.
	 * @return The (most likely) currently being send Node. (Or last to have been sent)
	 */
	public Node getCurrentSendingNode(){
		return manager.getCurrentSendingNode();
	}
}

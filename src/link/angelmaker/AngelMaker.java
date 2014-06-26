package link.angelmaker;

import java.io.File;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import phys.PhysicalLayer;
import phys.diag.NullPhysicalLayer;
import phys.diag.VirtualPhysicalLayer;
import util.BitSet2;
import util.Bytes;
import common.Graph;
import common.Stack;
import common.Startable;
import link.FrameLinkLayer;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.DummyBitExchanger;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManager;
import link.angelmaker.manager.BlockingAMManagerServer;
import link.angelmaker.manager.ConstantRetransmittingManager;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;
import link.angelmaker.nodes.PureNode;
import log.Logger;
import log.LogMessage.Subsystem;

/**
 * The upper class of the ANGEL_MAKER system.
 * This class acts like a FrameLinkLayer.
 * It sets up all the things necessary for the ANGEL_MAKER system to work.
 * instances of Node, BitExchanger and AMManager get chosen and build here.
 * 
 * Node:
 * The data storage, encodes, decodes, corrects errors, checks data.
 * 
 * BitExchanger:
 * Gets a bitstream from one end to another end, does not know meaning of data.
 * 
 * AMManager:
 * Combines Node and BitExchanger, decides when the Node is sent (depending on its Full/Ready status)
 * And decides when a Node is received correctly (Depending on the Node's Full/Ready status)
 * 
 * 
 * 
 * These 3 interfaces started out as interchangeable modules, however that has changed slightly.
 * Currently they often need a certain extension of the other base modules.
 * 
 * 
 * 
 * TODO:
 * #>Implement working BitExchanger.
 * #>Test Graphing for thread safety.
 * #>Build smart Frame, Receives stream, consumes till it finds start of frame flag, then reads till end of frame flag.
 * Check if correct, if not issues retransmit. recursively apply.
 * #>Make Non-Blocking AMManager.
 * #>More and better unitTests.
 * 
 * 
 * 
 *http://en.wikipedia.org/wiki/Angel_Makers_of_Nagyr%C3%A9v
 * @author I3anaan
 *
 */
//TODO implement this class more serious.
public class AngelMaker extends FrameLinkLayer implements Startable{
	
	public static AngelMaker instance;
	
	/**
	 * Standard classes to use when nothing else specified.	
	 */
	private PhysicalLayer STANDARD_PHYS = new NullPhysicalLayer();
	public static Node TOP_NODE_IN_USE = new FlaggingNode(null);
	//TODO set changed zodat zeker dat leeg is.
	private AMManager STANDARD_MANAGER = new ConstantRetransmittingManager();
	private BitExchanger STANDARD_EXCHANGER = new SimpleBitExchanger();
	
	
	
	public static final Logger logger =  new Logger(Subsystem.LINK);
	public AMManager manager;
	public BitExchanger bitExchanger;
	private Stack stack;
	
	public static AngelMaker getInstanceOrNull(){
		return instance;
	}
	
	
	public AngelMaker(PhysicalLayer phys,Node topNode,AMManager manager, BitExchanger exchanger){
		standardSetup(phys,topNode,manager,exchanger);
		instance = this;
	}
	public AngelMaker(PhysicalLayer phys){
		standardSetup(phys,null,null,null);
		instance = this;
	}
	public AngelMaker(){
		//TODO deze moet niet standard setup aanropen.
		standardSetup(null,null,null,null);
		instance = this;
	}
	
	
	@Override
	public void sendFrame(byte[] data) {
		manager.sendBytes(data);
	}

	@Override
	public byte[] readFrame() {
		return manager.readBytes();
	}

	@Override
	public String toCoolString() {
		return toString();
	}

	@Override
	public Thread start(Stack stack) {
		this.stack = stack;
		standardSetup(stack.physLayer,null,null,null);
		return null;
	}
	
	public void standardSetup(PhysicalLayer phys,Node topNode, AMManager manager, BitExchanger exchanger){
		Node topNodeUsed = topNode;
		AMManager managerUsed = manager;
		BitExchanger exchangerUsed = exchanger;
		PhysicalLayer physUsed = phys;
		if(physUsed==null){
			physUsed = STANDARD_PHYS;
		}
		if(topNodeUsed==null){
			topNodeUsed = TOP_NODE_IN_USE;
		}
		if(managerUsed==null){
			managerUsed = STANDARD_MANAGER;
		}
		if(exchangerUsed==null){
			exchangerUsed =STANDARD_EXCHANGER;
		}
		TOP_NODE_IN_USE = topNodeUsed;
		exchangerUsed.givePhysicalLayer(physUsed);
		exchangerUsed.giveAMManager(managerUsed);
		setup(physUsed,topNodeUsed,managerUsed,exchangerUsed);
	}
	
	
	public void setup(PhysicalLayer phys,Node topNode,AMManager manager, BitExchanger exchanger){
		logger.info("Building ANGEL_MAKER with: "+phys+" | "+topNode+" | "+manager+" | "+exchanger);
		//TODO thread name on AMManger is TPPHandler, why is this?
		logger.info("Setting up ANGEL_MAKER");
		try{
		TOP_NODE_IN_USE = topNode;
		this.manager = manager;
		this.bitExchanger = exchanger;
		this.manager.setExchanger(bitExchanger);
		logger.debug("Connected Modules.");
		exchanger.enable();
		manager.enable();
		logger.debug("Enabled Modules.");
		logger.info("ANGEL_MAKER setup done.");
		}catch(IncompatibleModulesException e){
			logger.bbq("Incompatible modules. ANGEL_MAKER could not start.");
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
	
	/**
	 * Can return null. Depends on AMManager.
	 * @return The (most likely) currently being received Node. (Either in progress, or finished)
	 */
	public Node getCurrentReceivingNode(){
		return manager.getCurrentReceivingNode();
	}
}

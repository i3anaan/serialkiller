package link.angelmaker;

import link.PacketFrameLinkLayer;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.nodes.Node;
import log.LogMessage.Subsystem;
import log.Logger;
import phys.PhysicalLayer;
import common.Stack;
import common.Startable;

/**
 * The upper class of the ANGEL_MAKER system.
 * This class acts like a PacketFrameLinkLayer.
 * It sets up all the things necessary for the ANGEL_MAKER system to work.
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
 * Currently optimization has led to almost hard coding of the different modules.
 * @author I3anaan
 *
 */
public class AngelMaker extends PacketFrameLinkLayer implements Startable{
	
	/**
	 * AngelMaker acts as a semi Singleton to support diagram drawing, this is the saved instance.
	 */
	public static AngelMaker instance;
	/**
	 * Logger used for LinkLayer
	 */
	public static final Logger logger =  new Logger(Subsystem.LINK);
	/**
	 * The manager in use.
	 */
	public AMManager manager;
	/**
	 * The BitExchanger in use.
	 */
	public BitExchanger bitExchanger;
	
	/**
	 * @return The most recent AngelMaker instance, or null.
	 */
	public static AngelMaker getInstanceOrNull(){
		return instance;
	}
	
	/**
	 * Builds Angel maker with the given parameters.
	 * Parameters may be null, then the default setting for that module will be used.
	 * @param phys		PhysicalLayer to use
	 * @param topNode	Node to use
	 * @param manager	Manager to use
	 * @param exchanger	BitExchanger to use
	 */
	public AngelMaker(PhysicalLayer phys,Node topNode,AMManager manager, BitExchanger exchanger){
		standardSetup(phys,topNode,manager,exchanger);
		instance = this;
	}
	/**
	 * Same as AngelMaker(phys,null,null,null);
	 * @param phys
	 */
	public AngelMaker(PhysicalLayer phys){
		standardSetup(phys,null,null,null);
		instance = this;
	}
	
	/**
	 * Empty constructor for the Starter.
	 * This constructor does almost nothing, start() needs to be called afterwards.
	 */
	public AngelMaker(){
		instance = this;
	}
	
	/**
	 * Hands bytes to send to the AMManager in use.
	 */
	@Override
	public void sendFrame(byte[] data) {
		manager.sendBytes(data);
	}

	/**
	 * Reads bytes received from AMManager and returns them.
	 */
	@Override
	public byte[] readFrame() {
		return manager.readBytes();
	}

	/**
	 * Starts the AngelMaker, to be called after the empty constructor.
	 * @param stack The current stack, PhysicalLayer is extracted from this.
	 */
	@Override
	public Thread start(Stack stack) {
		standardSetup(stack.physLayer,null,null,null);
		return null;
	}
	
	/**
	 * The standardSetup, this will replace all the possible null modules with default settings.
	 */
	public void standardSetup(PhysicalLayer phys,Node topNode, AMManager manager, BitExchanger exchanger){
		Node topNodeUsed = topNode;
		AMManager managerUsed = manager;
		BitExchanger exchangerUsed = exchanger;
		PhysicalLayer physUsed = phys;
		if(physUsed==null){
			physUsed = AngelMakerConfig.getPhys();
		}
		if(topNodeUsed==null){
			topNodeUsed = AngelMakerConfig.getNode();
		}
		if(managerUsed==null){
			managerUsed = AngelMakerConfig.getAMManager();
		}
		if(exchangerUsed==null){
			exchangerUsed =AngelMakerConfig.getBitExchanger();
		}
		setup(physUsed,topNodeUsed,managerUsed,exchangerUsed);
	}
	
	/**
	 * Connects the different Modules together.
	 */
	public void setup(PhysicalLayer phys,Node topNode,AMManager manager, BitExchanger exchanger){
		logger.info("Building ANGEL_MAKER with: "+phys.getClass().getSimpleName()+" | "+topNode.getClass().getSimpleName()+" | "+manager.getClass().getSimpleName()+" | "+exchanger.getClass().getSimpleName());
		try{
			
			this.manager = manager;
			this.bitExchanger = exchanger;
			this.manager.setExchanger(bitExchanger);
			exchanger.givePhysicalLayer(phys);
			exchanger.giveAMManager(manager);
			logger.debug("Connected Modules.");
			exchanger.enable();
			manager.enable();
			logger.debug("Enabled Modules.");
			
			logger.info("ANGEL_MAKER setup done.");
		}catch(IncompatibleModulesException e){
			logger.bbq("Incompatible modules. ANGEL_MAKER could not start.");
		}		
	}
	
	@Override
	public String toString(){
		String s = "ANGEL_MAKER\n"
				+ "Consisting of:\n"
				+ "\tAMManager:\t"+manager.toString()
				+ "\n\tBitExchanger:\t"+bitExchanger.toString()
				+ "\n\tNode:\t\t"+AngelMakerConfig.getNode().toString()+"\n";
		
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

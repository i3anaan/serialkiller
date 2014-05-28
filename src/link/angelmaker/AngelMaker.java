package link.angelmaker;

import java.io.File;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

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
	
	public static final String ASCII_ART_ANGEL_MAKER_1 = "\n"
			+ " █████╗ ███╗   ██╗ ██████╗ ███████╗██╗             ███╗   ███╗ █████╗ ██╗  ██╗███████╗██████╗ \n"
			+ "██╔══██╗████╗  ██║██╔════╝ ██╔════╝██║             ████╗ ████║██╔══██╗██║ ██╔╝██╔════╝██╔══██╗\n"
			+ "███████║██╔██╗ ██║██║  ███╗█████╗  ██║             ██╔████╔██║███████║█████╔╝ █████╗  ██████╔╝\n"
			+ "██╔══██║██║╚██╗██║██║   ██║██╔══╝  ██║             ██║╚██╔╝██║██╔══██║██╔═██╗ ██╔══╝  ██╔══██╗\n"
			+ "██║  ██║██║ ╚████║╚██████╔╝███████╗███████╗███████╗██║ ╚═╝ ██║██║  ██║██║  ██╗███████╗██║  ██║\n"
			+ "╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝╚══════╝╚══════╝╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝\n\n";
	public static final String ASCII_ART_ANGEL_MAKER_2 = "\n"
			+ "  _          _   ___                _        ___  __  \n"
			+ " /_)  )\\ )  / _  )_   )      )\\/)  /_)  )_/  )_   )_) \n"
			+ "/ /  (  (  (__/ (__  (__    (  (  / /  /  ) (__  / \\  \n"
			+ "                         __                           \n\n";
	
	public static Node TOP_NODE_IN_USE;
	public static final Logger logger =  new Logger(Subsystem.LINK);
	public AMManager manager;
	public BitExchanger bitExchanger;
	private Stack stack;
	private static int graphID;
	
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
		this.stack = stack;
		setup();
		return null;
	}
	
	
	public void setup(){
		
		//TODO severities set correct?
		//TODO thread name on AMManger is TPPHandler, why is this?
		logger.info("Setting up ANGEL_MAKER");
		try{
		//TOP_NODE_IN_USE = new FrameNode<Node>(null, 10);
			TOP_NODE_IN_USE = new FrameCeptionNode<Node>(null, 1);
		logger.debug("Top Node build.");
		manager = new BlockingAMManagerServer();
		logger.debug("Manager constructed.");
		bitExchanger = new ConsistentDuplexBitExchanger(stack.physLayer, manager);
		logger.debug("BitExchanger constructed.");
		manager.setExchanger(bitExchanger);
		logger.debug("Handed exchanger to manager.");
		manager.enable();
		logger.debug("Manager enabled.");
		System.out.println("\n\n"+toGraph(TOP_NODE_IN_USE)+"\n\n");
		AngelMaker.play("src/link/angelmaker/not_a_sound.wav");
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
		
		return ASCII_ART_ANGEL_MAKER_2;
	}

	
	public static String toGraph(Node node){
		graphID=0;
		String graph = "digraph g{"+addGraphVertice(node,graphID)+"}";
		return graph;
	}
	
	public static String addGraphVertice(Node base, int parentID){
		String graph = "";
		String color = "AliceBlue";
		if(base.getParent()!=null){
			graphID++;
			graph = graph + (graphID+"->"+parentID+";");
		}
		String label = base.toString().substring(0, Math.min(20,base.toString().length()));
		graph = graph + graphID+"[label=\""+label+"\",shape=ellipse,fillcolor=\""+ color+ "\",style=\"filled\"];";
		
		if(base instanceof Node.Internal){
			int thisID = graphID;
			for(Node n : ((Node.Internal)base).getChildNodes()){
				graph = graph+addGraphVertice(n, thisID);
			}
		}
		return graph;
	}
	
	
	//Much needed
	public static void play(String filename)
	{
	    try
	    {
	        Clip clip = AudioSystem.getClip();
	        clip.open(AudioSystem.getAudioInputStream(new File(filename)));
	        clip.start();
	    }
	    catch (Exception exc)
	    {
	        exc.printStackTrace(System.out);
	    }
	}
}

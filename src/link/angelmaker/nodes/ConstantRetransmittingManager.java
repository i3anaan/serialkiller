package link.angelmaker.nodes;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.primitives.Bytes;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.manager.AMManager;

public class ConstantRetransmittingManager extends Thread implements AMManager, AMManager.Server {
	//TODO better name
	//TODO more like a manager, or combination of manager / node
	
	private BitExchanger exchanger;
	public final int bitsUsed = 3;
	private ArrayBlockingQueue<Byte> queueIn;
	private ArrayBlockingQueue<Byte> queueOut;
	
	public static final int MESSAGE_FINE = 7;
	private int messageReceived;
	private int messageToSend;
	
	private Node[] memory;
	
	private Sender sender;
	private Receiver receiver;
	
	public ConstantRetransmittingManager(){
		this.queueIn = new ArrayBlockingQueue<Byte>(1024);
		this.queueOut = new ArrayBlockingQueue<Byte>(1024);
		this.memory = new Node[(int)Math.pow(2,bitsUsed)-1]; //bitsUsed - amount of special messages.
	}
	
	
	@Override
	public void setExchanger(BitExchanger exchanger) {
		this.exchanger = exchanger;
	}

	@Override
	public void enable() {
		
	}

	/**
	 * Blocking if queue gets full.
	 */
	@Override
	public void sendBytes(byte[] bytes) {
		for(int i=0;i<bytes.length;i++){
			try {
				queueOut.put(bytes[i]);
			} catch (InterruptedException e) {
				//Should never happen
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public byte[] readBytes() {
		ArrayList<Byte> arr = new ArrayList<Byte>();
		Byte b = queueIn.poll();
		while(b!=null){
			arr.add(b);
			b = queueIn.poll();
		}
		return Bytes.toArray(arr);
	}

	@Override
	public Node getCurrentSendingNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getCurrentReceivingNode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Node getNextNode() {
		Node node = sender.nodeToSendNext;
		sender.nodeToSendNext = null;
		//TODO make this a method, remove Sender Thread.
		if(node!=null){
			return node;
		}else{
			if(AngelMaker.TOP_NODE_IN_USE instanceof Node.Fillable){
				Node.Fillable filler = (Node.Fillable)AngelMaker.TOP_NODE_IN_USE;
				return filler.getFiller();
			}else{
				throw new IncompatibleModulesException();
			}
		}
	}
	
	public void loadNewNodeInMemory(int index,int messageToSend){
		//TODO
	}
	
	private class Sender extends Thread{
		private int lastSent;
		private int loadNew;
		private Node nodeToSendNext;
		public void run(){
			while(true){
				//TODO possibly very inefficient busy-wait
				if(nodeToSendNext==null){ //If the previous Node is done sending.
					while(messageReceived==MESSAGE_FINE){
						int indexToSend = (lastSent+1)%(memory.length);
						if(lastSent==loadNew){
							loadNewNodeInMemory(indexToSend,messageToSend);
						}
						nodeToSendNext = memory[indexToSend];
						lastSent = indexToSend;
						messageToSend = MESSAGE_FINE; //Do not send same message multiple times.
						
						if(lastSent == (loadNew+1)%memory.length){
							loadNew = lastSent;
						}
					}
					lastSent = messageReceived; //Other side requested retransmitting after this index.
				}
			}
		}		
	}
	
	private class Receiver extends Thread{
		private int lastReceivedCorrect;
		
		public void run(){
			while(true){
				Node received = fillNewNode();
				Node errorDetection = received.getChildNodes()[0];
				if(errorDetection.isCorrect()){
					Node packetNode = received.getChildNodes()[0];
					if(packetNode instanceof SequencedNode){
						SequencedNode seqNode = ((SequencedNode) packetNode);
						if(seqNode.getSeq()==(lastReceivedCorrect+1)%memory.length){
							//Fully correct.
							Node data = received.getChildNodes()[0];
							byte[] dataBytes = data.getOriginal().toByteArray();
							for(byte b : dataBytes){
							queueIn.put(b);
							}
							lastReceivedCorrect = (lastReceivedCorrect+1)%memory.length;
							messageReceived = seq.getMessage();
							messageToSend = MESSAGE_FINE;
						}else{
							//Only sequence number is wrong, packet is correct.
							messageReceived = seq.getMessage();
							messageToSend = lastReceivedCorrect;
						}
					}else{
						throw new IncompatibleModulesException();
					}
				}else{
					//Packet has errors.
					messageToSend = lastReceivedCorrect;
				}
			}
		}
		
		private Node fillNewNode(){
			//TODO Read bit stream, give to node, make it full, return.
			return null;
		}
		
		
		/*RECEIVING:
			 * Vanuitgaand normale situatie, lastReceivedCorrect = x.
			 * while(true){
				 * Vraag nieuwe node aan (vul een node met de stream).
				 * unflag de node, splits
				 * Check of verwachte lengte.
				 * Check of correct.
				 * Check of seq== (lastReceivedCorrect+1)%X.
				 * if(fullyCorrect){
					 * decode data, stop data in queueOut.
					 * lastReceivedCorrect = (lastReceivedCorrect+1)%X;
					 * messageReceived = haalMessageUitNode(node);
					 * messageToSent = prima;
				 * }else if(alleenSeqIsFout){
					 *  messageReceived = haalMessageUitNode(node);
					 *  messageToSent = lastReceivedCorrect;
				 *  }else{
					 *  messageToSent = lastReceivedCorrect;
					 *  //Ignore, dont even update messageReceived, assume sending went ok.
					 *  
					 *  
					 *  //TODO is dit een fatsoenlijke assumption? Je zou ook kunnen zeggen,
					 *  //	fout ontvangen hier is waarschijnlijk fout ontvangen daar.
					 *  //	Dit zou betekenen messageReceived = lastSent--; Punt is dat frame/node lengtes kunnen verschillen.
					 *  //	Dit zou optimalisatie zijn.	 *  
				 *  }
			 *  }
			 *  */
		
		
	}

	
	

	/*
	 * Heeft 2 queues. in/out bytes.
	 * Heeft ook X Nodes 'in geheugen'
	 * Stuurt Y data bits per Node.
	 * 
	 * Als er X frames achter elkaar raar doen, en de retransmits gaan mis zal het geheel mis blijven lopen zolang de queue gevuld is.
	 * Deze kans kan erg klein gemaakt worden en zou moeten resulteren in een packet drop op network.
	 * 
	 * 
	 * SENDING:
	 * while(true){
		 * Vanuitgaande normale situatie, lastSent = x
		 * while(allesprima){
			 * if(lastSent==loadNew){
				 * Haal max Y (node lengte) data uit queue. (kan dus ook niks zijn, dan wordt enkel de flags verstuurd, dus filler)
				 * stuff de data.
				 * Voeg seq en messageToSent toe.
				 * Voeg start en end flag toe.
				 * Zet deze gehele node op plek (lastSent+1)%X in geheugen.
			 * }
			 * Stuur (lastSent+1)%X. Deze heeft seq nummer en message, zoals RetransmittingNode dat ook heeft.
			 * lastSent = (lastSent+1)%X;
			 * if(lastSent = (loadNew+1)%X){
				 * loadNew = lastSent.
			 * }
		 * }
		 * Als laatste message geen 'ack' is.
		 * lastSent = message;
	 * }
	 * 
	 * 
	 * RECEIVING:
	 * Vanuitgaand normale situatie, lastReceivedCorrect = x.
	 * while(true){
		 * Vraag nieuwe node aan (vul een node met de stream).
		 * unflag de node, splits
		 * Check of verwachte lengte.
		 * Check of correct.
		 * Check of seq== (lastReceivedCorrect+1)%X.
		 * if(fullyCorrect){
			 * decode data, stop data in queueOut.
			 * lastReceivedCorrect = (lastReceivedCorrect+1)%X;
			 * messageReceived = haalMessageUitNode(node);
			 * messageToSent = prima;
		 * }else if(alleenSeqIsFout){
			 *  messageReceived = haalMessageUitNode(node);
			 *  messageToSent = lastReceivedCorrect;
		 *  }else{
			 *  messageToSent = lastReceivedCorrect;
			 *  //Ignore, dont even update messageReceived, assume sending went ok.
			 *  
			 *  
			 *  //TODO is dit een fatsoenlijke assumption? Je zou ook kunnen zeggen,
			 *  //	fout ontvangen hier is waarschijnlijk fout ontvangen daar.
			 *  //	Dit zou betekenen messageReceived = lastSent--; Punt is dat frame/node lengtes kunnen verschillen.
			 *  //	Dit zou optimalisatie zijn.	 *  
		 *  }
	 *  }
	 * 
	 */
	
	
}

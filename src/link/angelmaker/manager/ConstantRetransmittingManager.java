package link.angelmaker.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.Node.Fillable;
import link.angelmaker.nodes.SequencedNode;

public class ConstantRetransmittingManager extends Thread implements AMManager, AMManager.Server {
	//TODO better name
	//TODO more like a manager, or combination of manager / node
	public static final FlaggingNode NODE_FILLER = (FlaggingNode) new FlaggingNode(null);
	
	
	private BitExchanger exchanger;
	private ArrayBlockingQueue<Byte> queueIn;
	private ArrayBlockingQueue<Byte> queueOut;
	
	public static final int MESSAGE_FINE = (int)Math.pow(2,SequencedNode.MESSAGE_BIT_COUNT)-1;
	private static BitSet2[] possibleMessages;
	private Node receivingNode = NODE_FILLER.getClone();
	private volatile int messageReceived;
	private volatile int messageToSend;
	
	private Node[] memory;
	private BitSet2 spilledBitsIn;
	
	private Receiver receiver;
	private int lastSent=0;
	private int loadNew=0;
	
	private static final byte[] emptyArray = new byte[]{};
	
	public ConstantRetransmittingManager(){
		this.queueIn = new ArrayBlockingQueue<Byte>(2048);
		this.queueOut = new ArrayBlockingQueue<Byte>(2048);
		this.memory = new Node[MESSAGE_FINE]; //bitsUsed - amount of special messages.
		possibleMessages = new BitSet2[MESSAGE_FINE+1];
		for(int i=0;i<memory.length;i++){
			memory[i] = NODE_FILLER.getClone();
			possibleMessages[i] = intMessageToBitSet(i);
		}
		possibleMessages[MESSAGE_FINE+1] = intMessageToBitSet(MESSAGE_FINE+1);
		spilledBitsIn = new BitSet2();
		messageReceived = MESSAGE_FINE;
		messageToSend = MESSAGE_FINE;
	}
	
	
	@Override
	public void setExchanger(BitExchanger exchanger) {
		this.exchanger = exchanger;
	}

	@Override
	public void enable() {
		receiver = new Receiver();
		receiver.start();
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
		if (queueIn.isEmpty()) {
			return emptyArray;
		} else {
			ArrayList<Byte> arr = new ArrayList<Byte>();
			queueIn.drainTo(arr);
			return Bytes.toArray(arr);
		}
	}

	@Override
	public Node getCurrentSendingNode() {
		return memory[lastSent]; 
		//TODO not really the currently send node.
		//Kind of want to get the memory.
	}

	@Override
	public Node getCurrentReceivingNode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Node getNextNode() {
		Node nodeToSendNext;
		if(messageReceived!=MESSAGE_FINE){
			lastSent = messageReceived; //Other side requested retransmitting after this index
			messageReceived = MESSAGE_FINE; //Moved sending index down, now continue assuming fine.
		}
		
		int indexToSend = (lastSent+1)%(memory.length);
		if(lastSent==loadNew){
			loadNewNodeInMemory(indexToSend);
			//AngelMaker.logger.debug("Loaded new Node in Memory");
		}
		nodeToSendNext = memory[indexToSend];
		//AngelMaker.logger.debug("NodeToSendNext [" +indexToSend+"] = "+nodeToSendNext);
		
		lastSent = indexToSend;
		if(nodeToSendNext.getChildNodes()[0].getChildNodes()[0] instanceof SequencedNode){
			SequencedNode seqNode = ((SequencedNode)nodeToSendNext.getChildNodes()[0].getChildNodes()[0]);			
			seqNode.setMessage(possibleMessages[messageToSend]);		
			seqNode.setSeq(possibleMessages[indexToSend]);
		}else{
			throw new IncompatibleModulesException();
		}
		messageToSend = MESSAGE_FINE; //Do not send same message multiple times.
		
		if(lastSent == (loadNew+1)%memory.length){
			loadNew = lastSent;
		}
		
		//AngelMaker.logger.debug("Sending packet,\tseq="+((SequencedNode)nodeToSendNext.getChildNodes()[0].getChildNodes()[0]).getSeq().getUnsignedValue()+"\t\tmsg="+((SequencedNode)nodeToSendNext.getChildNodes()[0].getChildNodes()[0]).getMessage().getUnsignedValue()+"\tdata="+nodeToSendNext.getOriginal());
		
		return nodeToSendNext;
	}
	
	
	
	public void loadNewNodeInMemory(int index){
		memory[index].reset();
		BitSet2 bs = new BitSet2();
		int byteCount = 0;
		Byte b = queueOut.poll();
		if(b==null){
			//Filler
			memory[index] = NODE_FILLER;
		}else{
			while(b!=null && byteCount<SequencedNode.PACKET_BIT_COUNT/8){
				bs.addAtEnd(new BitSet2(b));
				byteCount++;
				if (byteCount<SequencedNode.PACKET_BIT_COUNT/8) {
					b = queueOut.poll();
				}
			}
			memory[index].giveOriginal(bs);
		}		
	}
	
	public BitSet2 intMessageToBitSet(int message){
		BitSet2 bs = new BitSet2(Ints.toByteArray(message));
		return bs.get(bs.length()-SequencedNode.MESSAGE_BIT_COUNT, bs.length());
	}
	
	@Override
	public String toString(){
		return "ConstantRetransmittingManager";
	}
		
	private class Receiver extends Thread{
		private int lastReceivedCorrect;
		
		public void run(){
			while(true){
				refillReceivingNode();
				Node errorDetection = receivingNode.getChildNodes()[0];
				if(errorDetection.isCorrect()){
					Node packetNode = errorDetection.getChildNodes()[0];
					if(packetNode instanceof SequencedNode){
						SequencedNode seqNode = ((SequencedNode) packetNode);
						//AngelMaker.logger.debug("Received packet\tseq="+seqNode.getSeq().getUnsignedValue()+" ("+((lastReceivedCorrect+1)%memory.length)+")"+"\tmsg="+seqNode.getMessage().getUnsignedValue()+"\tdata="+seqNode.getOriginal());
						if(seqNode.getSeq().getUnsignedValue()==(lastReceivedCorrect+1)%memory.length){
							//Fully correct.
							//AngelMaker.logger.debug("Received correct packet\tseq="+seqNode.getSeq().getUnsignedValue()+"\tOK");

							byte[] dataBytes = seqNode.getOriginal().toByteArray();
							for(byte b : dataBytes){
							try {
								queueIn.put(b);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							}
							lastReceivedCorrect = (lastReceivedCorrect+1)%memory.length;
							int currentMessageReceived = seqNode.getMessage().getUnsignedValue();
							if(currentMessageReceived!=MESSAGE_FINE){
								messageReceived = currentMessageReceived;
							}
							messageToSend = MESSAGE_FINE;
						}else{
							//Only sequence number is wrong, packet is correct.
							int currentMessageReceived = seqNode.getMessage().getUnsignedValue();
							if(currentMessageReceived!=MESSAGE_FINE){
								messageReceived = currentMessageReceived;
							}
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
		
		private void refillReceivingNode(){
			receivingNode.reset();
			while(!receivingNode.isFull()){
				spilledBitsIn = receivingNode.giveConverted(BitSet2.concatenate(spilledBitsIn,exchanger.readBits()));
			}
		}
	}
}

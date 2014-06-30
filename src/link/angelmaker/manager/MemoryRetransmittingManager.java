package link.angelmaker.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.SequencedNode;

public class MemoryRetransmittingManager extends Thread implements Node ,AMManager, AMManager.Server {
	public static final FlaggingNode NODE_FILLER = new FlaggingNode(null);
	
	
	private BitExchanger exchanger;
	private ArrayBlockingQueue<Byte> queueIn;
	private ArrayBlockingQueue<Byte> queueOut;
	
	public static final int MESSAGE_FINE = (int)Math.pow(2,SequencedNode.MESSAGE_BIT_COUNT)-1;
	public BitSet2[] possibleMessages;
	private Node.Resetable receivingNode = (Node.Resetable)NODE_FILLER.getClone();
	private Node.Resetable sendingNode = (Node.Resetable)NODE_FILLER.getClone();
	private volatile int messageReceived;
	private volatile int messageToSend;
	private int lastReceivedCorrect = 254;
	private Byte[] memory;
	private Byte[] backupMemoryRealNodes;
	private Byte[] nodeBuildingBytes;
	private BitSet2 spilledBitsIn;
	
	private Receiver receiver;
	private int lastSent=254;
	private int loadNew=0;
	
	private static final byte[] emptyArray = new byte[]{};
	
	public MemoryRetransmittingManager(){
		this.queueIn = new ArrayBlockingQueue<Byte>(2048);
		this.queueOut = new ArrayBlockingQueue<Byte>(2048);
		this.memory = new Byte[MESSAGE_FINE]; //bitsUsed - amount of special messages.
		this.backupMemoryRealNodes = new Byte[MESSAGE_FINE];
		nodeBuildingBytes = new Byte[SequencedNode.PACKET_BIT_COUNT/8];
		NODE_FILLER.setParent(this);
		
		possibleMessages = new BitSet2[MESSAGE_FINE+1];
		possibleMessages[MESSAGE_FINE] = intMessageToBitSet(MESSAGE_FINE);
		for(int i=0;i<memory.length;i++){
			Node.Resetable newNode = ((Node.Resetable)NODE_FILLER.getClone());
			memory[i] = null;
			backupMemoryRealNodes[i] = null;
			possibleMessages[i] = intMessageToBitSet(i);
		}
		NODE_FILLER.giveOriginal(new BitSet2()); //Make sure it is full (so isFiller() returns true);
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
				AngelMaker.logger.warning("Dropped Byte to send while trying to put it in MemoryRetransmittingManager.queueOut.");
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
		AngelMaker.logger.info("Requested Current Sending Node");
		System.out.println(this);
		return this;
	}

	@Override
	public Node getCurrentReceivingNode() {
		AngelMaker.logger.info("Requested Current Receving Node");
		return receivingNode;
	}
	
	@Override
	public Node getNextNode() {
		if(messageReceived!=MESSAGE_FINE){
			lastSent = messageReceived; //Other side requested retransmitting after this index
			messageReceived = MESSAGE_FINE; //Moved sending index down, now continue assuming fine.
		}
		
		int indexToSend = (lastSent+1)%(memory.length);
		for(int i=0;i<SequencedNode.PACKET_BYTE_COUNT;i++){
			nodeBuildingBytes[i] = null;
		}
		for(int i=0;i<SequencedNode.PACKET_BYTE_COUNT;i++){
			if((indexToSend+i)%(memory.length)==loadNew){
				//If added all the requested retransmissions.
				Byte newByte = queueOut.poll();
				if(newByte!=null){
					//If extra bytes to send, add to memory and prepare for sending.
					memory[(indexToSend+i)%(memory.length)] = newByte;
					nodeBuildingBytes[i] = newByte;
					loadNew = (indexToSend+i+1)%(memory.length);
				}else{
					i = SequencedNode.PACKET_BYTE_COUNT;
					//Stop for loop if nothing else to send
				}
			}else{
				//Retransmissions first.
				nodeBuildingBytes[i] = memory[(indexToSend+i)%(memory.length)];
			}
			lastSent = (indexToSend+i)%memory.length;
		}
		//AngelMaker.logger.debug("Building node ["+indexToSend+"] from: "+Arrays.toString(nodeBuildingBytes));
		//Put created array in node.
		sendingNode.reset();
		sendingNode.giveOriginal(new BitSet2(nodeBuildingBytes));
		//AngelMaker.logger.debug("Node Original: "+sendingNode.getOriginal()+"\tConverted: "+sendingNode.getConverted());
		
		if(sendingNode.getChildNodes()[0].getChildNodes()[0] instanceof SequencedNode){
			SequencedNode seqNode = ((SequencedNode)sendingNode.getChildNodes()[0].getChildNodes()[0]);			
			seqNode.setMessage(possibleMessages[messageToSend]);		
			seqNode.setSeq(possibleMessages[(indexToSend)%memory.length]);
		}else{
			throw new IncompatibleModulesException();
		}
		
		messageToSend = MESSAGE_FINE; //Do not send same message multiple times.
		//AngelMaker.logger.debug("Sending packet,\tseq="+((SequencedNode)sendingNode.getChildNodes()[0].getChildNodes()[0]).getSeq().getUnsignedValue()+"\t\tmsg="+((SequencedNode)sendingNode.getChildNodes()[0].getChildNodes()[0]).getMessage().getUnsignedValue()+"\tdata="+sendingNode.getOriginal()+"\tConverted: "+sendingNode.getConverted());
		return sendingNode;
	}
	
	public static BitSet2 intMessageToBitSet(int message){
		BitSet2 bs = new BitSet2(Ints.toByteArray(message));
		return bs.get(bs.length()-SequencedNode.MESSAGE_BIT_COUNT, bs.length());
	}
		
	private class Receiver extends Thread{
		
		
		@Override
		public void run(){
			while(true){
				refillReceivingNode();
				Node errorDetection = receivingNode.getChildNodes()[0];
				if(errorDetection.isCorrect()){
					Node packetNode = errorDetection.getChildNodes()[0];
					if(packetNode instanceof SequencedNode){
						SequencedNode seqNode = ((SequencedNode) packetNode);
						//AngelMaker.logger.debug("Received packet\tseq="+seqNode.getSeq().getUnsignedValue()+"\tmsg="+seqNode.getMessage().getUnsignedValue()+"\tExpected:"+((lastReceivedCorrect+1)%memory.length)+"\tData="+seqNode.getOriginal()+"\tConverted: "+receivingNode.getConverted());

						if(seqNode.getSeq().getUnsignedValue()==(lastReceivedCorrect+1)%memory.length){
							//Fully correct, expected sequence number
							
							byte[] dataBytes = seqNode.getOriginal().toByteArray();
							for(byte b : dataBytes){
							try {
								queueIn.put(b);
							} catch (InterruptedException e) {
								//Should not happen, but if does, just drop byte.
								e.printStackTrace();
								AngelMaker.logger.warning("Dropped received byte while trying to put it in MemoryRetransmittingManager.queueIn");
							}
							}
							lastReceivedCorrect = (lastReceivedCorrect+seqNode.getOriginal().length()/8)%memory.length;
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

	
	/*
	 * Implements Node to let Graph draw the memory.
	 */
	
	
	
	@Override
	public BitSet2 getOriginal() {
		return sendingNode.getOriginal();
	}


	@Override
	public BitSet2 getConverted() {
		return sendingNode.getConverted();
	}


	@Override
	public Node getParent() {
		return null;
	}


	@Override
	public boolean isFull() {
		return false;
	}


	@Override
	public boolean isReady() {
		return false;
	}


	@Override
	public boolean isCorrect() {
		return true;
	}

	@Override
	public Node[] getChildNodes() {
		return new Node[]{sendingNode};
	}


	@Override
	public String getStateString() {
		return "LastSent: "+lastSent+"\tNewestSend: "+loadNew+"\tLastReceived: "+lastReceivedCorrect;
	}


	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		throw new NotImplementedException();
	}


	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		throw new NotImplementedException();
	}


	@Override
	public Node getClone() {
		throw new NotImplementedException();
	}
	
	@Override
	public String toString(){
		String result =  "MemoryRetransmittingManager";
		result = result +"\n\tLastSent: "+lastSent+"\tNewestSend: "+loadNew+"\tLastReceived: "+lastReceivedCorrect;
		//result = result + "\n\tMemory"+Arrays.toString(memory);
		return result;
	}
}

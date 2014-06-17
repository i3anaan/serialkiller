package link.angelmaker.nodes;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.BitSet2;

public class RetransmittingNode implements Node, Node.Fillable, Node.SelfBuilding{

	/*
	 * SENDING Krijg alle data van boven. Stop in meerdere childNodes. Begin met
	 * het sturen van alle childNodes. Als bij receiving merkt dat er ergens
	 * iets misgegaan is, begin opnieuw bij de eerst volgende childNode.
	 * 
	 * 
	 * 
	 * RECEIVING
	 * 
	 * 
	 * Zoek start flag, noteer sequence nummer, zoek end flag.
	 * Stop dat stuk ertussen in childNode.
	 * is n0?{
	 * NEE> negeer frame, zet n-1 in eerstvolgende frame;
	 * verwacht n0
	 * JA> 
	 * vraag of n0 goed is.{
	 * JA>	zet in eerstvolgende frame prima;
	 * Verwacht n1
	 * NEE> zet in eerstvolgende frame n-1;
	 * verwacht n0
	 * }
	 */
	
	
	private static final int SEQUENCE_BIT_COUNT = 4;
	private static final int DIFFERENT_SEQUENCE_NUMBERS = 6;
	private static final int CORRECT = 7;
	private static final int DONE = 8;
	
	public static final Flag FLAG_START_OF_FRAME = new BasicFlag(new BitSet2(
			"10011001"));
	public static final Flag FLAG_END_OF_FRAME = new BasicFlag(new BitSet2(
			"00111001101"));

	private Node[] childNodes;
	private Node parent;
	
	private int correctReceivedSequence;
	private int correctReceivedChild;
	
	private int lastChildSent;
	
	private int messageToSend;
	private int messageReceived;
	
	private boolean receivedStartFlag;
	private BitSet2 lastReceivedConvertedJunk;
	private BitSet2 storedReceived;
	
	
	public RetransmittingNode(Node parent, int childCount){
		this.parent = parent;
		childNodes = new Node[childCount];
		lastReceivedConvertedJunk = new BitSet2();
		
		for(int i=0;i<childNodes.length;i++){
			//childNodes[i] = getNewNode();
		}
		correctReceivedChild = 0;
		lastChildSent = (-1+DIFFERENT_SEQUENCE_NUMBERS)%DIFFERENT_SEQUENCE_NUMBERS;
		
		messageReceived = CORRECT;
		messageToSend = CORRECT;
	}
	
	public Node getNewNode(){
		return new FrameNode<Node>(this,3);//TODO
	}
	
	@Override
	public void buildSelf(BitExchanger exchanger, Node nodeToSend) {
		if(!(nodeToSend instanceof RetransmittingNode)){
			throw new IncompatibleModulesException();
		}
		
		
		RetransmittingNode retransmittingNodeToSend = (RetransmittingNode)nodeToSend;
		while(!isFull()){
			//Check if received a frame since last time.
			BitSet2 received = getReceivedChild(exchanger);
			if(received!=null){
				actOnReceived(received);
			}
			
			//Check if still something to send.
			if(exchanger.getQueueOut().size()<10){ //TODO send next at 10?
				queueNextChild(exchanger,retransmittingNodeToSend);
			}
			
			
		}
	}
	
	private void actOnReceived(BitSet2 received){
		messageReceived = getMessageAtEnd(received);
		int sequenceNumberReceived = getSequenceNumber(received);
		BitSet2 dataReceived = getData(received);
		if(sequenceNumberReceived == correctReceivedSequence+1){
			Node testNode = getNewNode();
			testNode.giveConverted(dataReceived);
			if(testNode.isCorrect()){
				childNodes[correctReceivedChild] = testNode;
				correctReceivedChild++;
				correctReceivedSequence = correctReceivedChild%DIFFERENT_SEQUENCE_NUMBERS;
				messageToSend = CORRECT;
			}else{
				//Bit error occured.
				messageToSend = correctReceivedSequence;
			}
		}else{
			//Wrong sequence number.
			messageToSend = correctReceivedSequence;
		}
	}
	
	private BitSet2 getData(BitSet2 bs){
		return bs.get(FLAG_START_OF_FRAME.getFlag().length()+SEQUENCE_BIT_COUNT,bs.length()-SEQUENCE_BIT_COUNT-FLAG_END_OF_FRAME.getFlag().length());
	}
	
	private int getSequenceNumber(BitSet2 bs){
		BitSet2 message = bs.get(FLAG_START_OF_FRAME.getFlag().length(), FLAG_START_OF_FRAME.getFlag().length()+SEQUENCE_BIT_COUNT);
		return getUnsignedValue(message);
	}
	
	
	
	private int getMessageAtEnd(BitSet2 bs){
		BitSet2 message = bs.get(bs.length()-SEQUENCE_BIT_COUNT, bs.length());
		return getUnsignedValue(message);
	}
	
	private int getUnsignedValue(BitSet2 bs){
		int value = 0;
		for(int i=0;i<bs.length();i++){
			if(bs.get(bs.length()-1-i)){
				value = value + (int)Math.pow(2, i);
			}
		}
		return value;
	}
	
	private void queueNextChild(BitExchanger exchanger, RetransmittingNode nodeToSend){
		if(messageReceived==CORRECT){
			sendChild(exchanger,(lastChildSent+1)%DIFFERENT_SEQUENCE_NUMBERS,nodeToSend.getChildConverted(lastChildSent+1),messageToSend);
			lastChildSent++;
		}else if(messageReceived==DONE){
			//TODO
		}else{
			//Retransmit request.
			lastChildSent = lastChildSent-(DIFFERENT_SEQUENCE_NUMBERS - (lastChildSent%DIFFERENT_SEQUENCE_NUMBERS))-1;
			sendChild(exchanger,(lastChildSent+1)%DIFFERENT_SEQUENCE_NUMBERS,nodeToSend.getChildConverted(lastChildSent+1),messageToSend);
			lastChildSent++;
		}
	}
	
	
	private void sendChild(BitExchanger exchanger, int sequenceNumber, BitSet2 childConverted, int messageForOther) {
		BitSet2 rawToBeSend = new BitSet2();
		rawToBeSend = BitSet2.concatenate(rawToBeSend, getBitSetFromSequenceNumber(sequenceNumber));
		rawToBeSend = BitSet2.concatenate(rawToBeSend, childConverted);
		rawToBeSend = BitSet2.concatenate(rawToBeSend, getBitSetFromSequenceNumber(messageForOther));
		stuff(rawToBeSend);
		rawToBeSend = BitSet2.concatenate(FLAG_START_OF_FRAME.getFlag(),rawToBeSend);
		rawToBeSend = BitSet2.concatenate(rawToBeSend, FLAG_END_OF_FRAME.getFlag());
		exchanger.sendBits(rawToBeSend);		
	}
	
	public BitSet2 getBitSetFromSequenceNumber(int sequenceNumber){
		BitSet2 result = new BitSet2();
		int remaining = sequenceNumber;
		for(int i=0;i<SEQUENCE_BIT_COUNT;i++){
			int bitValue = (int) Math.pow(2,(SEQUENCE_BIT_COUNT-i-1));
			if(remaining>bitValue){
				result.addAtEnd(true);
				remaining = remaining - bitValue;
			}else{
				result.addAtEnd(false);
			}
		}
		//TODO test method;
		return result;
	}
	
	private BitSet2 stuff(BitSet2 bits) {
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[] { FLAG_START_OF_FRAME, FLAG_END_OF_FRAME };
		for (int i = 0; i < flags.length; i++) {
			flags[i].stuff(result);
		}
		return result;
	}

	private BitSet2 unStuff(BitSet2 bits) {
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[] { FLAG_START_OF_FRAME, FLAG_END_OF_FRAME };
		for (int i = flags.length - 1; i >= 0; i--) {
			flags[i].unStuff(result);
		}
		return result;
	}

	private BitSet2 getChildConverted(int i) {
		return childNodes[i].getConverted();
	}

	/**
	 * @return A newly receivedFrame or null if no new Frame yet.
	 */
	private BitSet2 getReceivedChild(BitExchanger exchanger) {
		BitSet2 bits = exchanger.readBits();
		if (!receivedStartFlag) {
			BitSet2 tempConcat = BitSet2.concatenate(lastReceivedConvertedJunk,
					bits); // Add just received to already received.
			// Keep only just received + max flag length (so it cannot
			// infinetely grow.
			lastReceivedConvertedJunk = tempConcat.get(
					Math.max(0,
							tempConcat.length()
									- FLAG_START_OF_FRAME.getFlag().length()
									- bits.length()), tempConcat.length());
			BitSet2 afterStart = getDataAfterStartFlag(lastReceivedConvertedJunk);
			if (afterStart.length() >= 0) {
				storedReceived = afterStart;
			}
		} else {
			storedReceived = BitSet2.concatenate(storedReceived, bits);
		}
		int contains = storedReceived.contains(FLAG_END_OF_FRAME.getFlag());
		if (receivedStartFlag && contains >= 0) {
			// Received start and end flag.
			BitSet2 receivedChildConverted = (BitSet2) (unStuff(getDataBeforeEndFlag(storedReceived)).clone());
			storedReceived =  storedReceived.get(contains+ FLAG_END_OF_FRAME.getFlag().length(),storedReceived.length());
			return receivedChildConverted;
		}
		return null;
	}
	
	/**
	 * @bits BitSet2 to look in.
	 * @return the data after the start flag, empty bitset2 if bits does not
	 *         contain the start flag
	 */
	private BitSet2 getDataAfterStartFlag(BitSet2 bits) {
		int contains = bits.contains(FLAG_START_OF_FRAME.getFlag());
		if (contains >= 0) {
			receivedStartFlag = true;
			return bits.get(contains + FLAG_START_OF_FRAME.getFlag().length(),
					bits.length());
		} else {
			return new BitSet2();
		}
	}
	
	private BitSet2 getDataBeforeEndFlag(BitSet2 bits) {
		int contains = bits.contains(FLAG_END_OF_FRAME.getFlag());
		if (contains >= 0) {
			return bits.get(0, contains);
		} else {
			return (BitSet2) bits.clone();
		}
	}
	
	/*

	public void layout(){
		//Require filler size to be same as data size. (frame)
		
		//Receiver
		while(!childNodesIncomplete){
			data = getFrame()
			expectedSeq //Up to (but excluding) this seq was received correct.
			msg = data.getmsgAtEnd()
			data = getBetweenFlags()
			seq = data.removeSeqAtStart()
			//data now only contains the data.
			if(seq==expectedSeq){
				if(data.isCorrect()){
					expectedSeq = expectedSeq+1;
					senderMessage = prima;
				}else{
					//Do nothing, ignore
					senderMessage = expectedSeq-1; //expectedSeq-1 was received correct.
				}			
			}else{
				//Do nothing, ignore
				senderMessage = expectedSeq-1; //expectedSeq-1 was received correct.
			}
		}
		while(msg!=done){
			data = getFrame()
			msg = data.getmsgAtEnd()			
		}
		
		//Sender
		while(msg!=done){
			if(msg==prima){
				send(dataFrames[nextSeq],senderMessage);
				senderMessage = prima;
			}else{
				nextSeq = msg+1
				msg = prima;	//Handled message, for now assumes everything is well.
				send(dataFrames[nextSeq],senderMessage);
				senderMessage = prima;
			}
		}
		while(!childNodesIncomplete){
			send(filler,senderMessage);
		}		
	}

	*/

	@Override
	public boolean isFiller() {
		return false;
	}

	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		for(int i=0;i<childNodes.length && !isFull() && bits.length()>0;i++){
			bits = childNodes[i].giveOriginal(bits);
		}
		return bits;
	}

	@Override
	public BitSet2 getOriginal() {
		BitSet2 result = new BitSet2();
		for(int i=0;i<childNodes.length;i++){
			result = BitSet2.concatenate(result, childNodes[i].getOriginal());
		}
		return result;
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		throw new NotImplementedException();
	}

	@Override
	public BitSet2 getConverted() {
		throw new NotImplementedException();
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public boolean isFull() {
		for(int i=0;i<childNodes.length;i++){
			if(!childNodes[i].isFull()){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isReady() {
		for(int i=0;i<childNodes.length;i++){
			if(!childNodes[i].isReady()){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isCorrect() {
		for(int i=0;i<childNodes.length;i++){
			if(!childNodes[i].isReady()){
				return false;
			}
		}
		return isReady();
	}
	
	@Override
	public Node getFiller(){
		//TODO test, rethink;
		return AngelMaker.TOP_NODE_IN_USE.getClone();
	}
	

	@Override
	public Node getClone() {
		RetransmittingNode clone = new RetransmittingNode(parent, childNodes.length);
		for(int i=0;i<childNodes.length;i++){
			clone.childNodes[i] = childNodes[i].getClone(); 
		}
		return clone;
	}

	@Override
	public String getStateString() {
		return "NOT IMPLEMENTED";
	}

	
}

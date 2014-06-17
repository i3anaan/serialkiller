package link.angelmaker.manager;

import java.util.concurrent.ArrayBlockingQueue;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.Node;

public class ThreadedAMManagerServer extends Thread implements AMManager,
		AMManager.Server {

	/*
	 * TODO Make DoubleThreadedAMManagerServer. This AMManager will give the
	 * data you want to send to the used Node and then immediately asks for the
	 * converted data. If you do not give the exact amount of data the Node can
	 * use (makes it ready) this AMManager will fail. The
	 * DoubleThreadedAMManagerServer would have an extra thread dedicated to
	 * collecting data to send and putting it in a Node until it is ready, this
	 * would let it accept parts of data.
	 */

	public ArrayBlockingQueue<Byte> queueIn;
	private BitExchanger exchanger;
	private Node currentSendingNode;
	private Node currentReceivingNode;

	public ThreadedAMManagerServer() {
		queueIn = new ArrayBlockingQueue<Byte>(1024);
	}

	@Override
	public Node getNextNode() {
		if (AngelMaker.TOP_NODE_IN_USE instanceof Node.Fillable) {
			return ((Node.Fillable) AngelMaker.TOP_NODE_IN_USE).getFiller();
		} else {
			throw new IncompatibleModulesException();
		}

	}

	@Override
	public void setExchanger(BitExchanger exchanger) {
		this.exchanger = exchanger;
	}

	@Override
	public void enable() {
		this.start();
	}

	@Override
	public void sendBytes(byte[] bytes) {
		currentSendingNode = AngelMaker.TOP_NODE_IN_USE.getClone();
		currentSendingNode.giveOriginal(new BitSet2(bytes));
		if(!currentSendingNode.isReady()){
			AngelMaker.logger.error("Node trying to send is not ready");
		}
		exchanger.sendBits(currentSendingNode.getConverted());
	}

	@Override
	public byte[] readBytes() {
		int size = queueIn.size();
		byte[] arr = new byte[size];
		for (int i = 0; i < size; i++) {
			try {
				arr[i] = queueIn.take();
			} catch (InterruptedException e) {
				// Should never happen.
				e.printStackTrace();
			}
		}
		return arr;
	}

	@Override
	public Node getCurrentSendingNode() {
		return currentSendingNode;
	}

	@Override
	public Node getCurrentReceivingNode() {
		return currentReceivingNode;
	}

	@Override
	public void run() {
		AngelMaker.logger.info("Started ThreadedAMManagerServer Thread");
		BitSet2 received = new BitSet2();
		while (true) {
			currentReceivingNode = AngelMaker.TOP_NODE_IN_USE.getClone();
			do{
				received = BitSet2.concatenate(received,exchanger.readBits());
				if(received.length()>0){
					received = currentReceivingNode.giveConverted(received);
				}
			}while(!(currentReceivingNode.isFull()));
			
			if(currentReceivingNode.isFull() && !currentReceivingNode.isReady()){
				AngelMaker.logger.error("Node full, but not ready to be read.");
				//TODO;
			}
			byte[] arr = currentReceivingNode.getOriginal().toByteArray();
			for(int i=0;i<arr.length;i++){
				if(!queueIn.offer(arr[i])){
					AngelMaker.logger.warning("QueueIn is full!, dropping bytes.");
				}
			}
		}
	}
}

package link.angelmaker.bitexchanger;

import java.util.concurrent.ArrayBlockingQueue;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.BitSet2;

/**
 * Dummym, does not use a link.
 * Sends to itself.
 * @author I3anaan
 *
 */
public class DummyBitExchanger implements BitExchanger {

	private ArrayBlockingQueue<Boolean> queue;
	
	public DummyBitExchanger(){
		queue = new ArrayBlockingQueue<Boolean>(1024);
	}
	@Override
	public void sendBits(BitSet2 bits) {
		//System.out.println("Send:\t"+bits);
		for(int i = 0;i<bits.length();i++){
			//System.out.println("S\t"+bits.get(i));
			queue.add(bits.get(i));
		}
	}

	@Override
	public void emptyQueue() {
		queue.clear();
	}

	@Override
	public BitSet2 readBits() {
		BitSet2 bs = new BitSet2();
		while(queue.peek()!=null){
			//System.out.println("R\t"+queue.peek());
			bs.addAtEnd(queue.poll());
		}
		if(bs.length()>0){
			//System.out.println("Read:\t"+bs);
		}
		return bs;
	}
	@Override
	public ArrayBlockingQueue<Boolean> getQueueOut() {
		throw new UnsupportedOperationException();
	}

}

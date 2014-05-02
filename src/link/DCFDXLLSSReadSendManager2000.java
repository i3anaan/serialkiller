package link;

import java.util.concurrent.ArrayBlockingQueue;

import util.ByteArrays;

/**
 * DelayCorrectedFullDuplexLinkLayerSectionSegmentReadWriteSendManager2000
 * readByte() and sendByte() are NOT blocking;
 * @author I3anaan
 *
 */
public class DCFDXLLSSReadSendManager2000 extends LinkLayer implements Runnable{
	DelayCorrectedFDXLinkLayerSectionSegment down;
	
	private ArrayBlockingQueue<Byte> inbox; //TODO frames van maken;
	private ArrayBlockingQueue<Byte> outbox; //TODO frames van maken;
		
	
	public DCFDXLLSSReadSendManager2000(DelayCorrectedFDXLinkLayerSectionSegment down){
		this.down = down;
		this.inbox = new ArrayBlockingQueue<Byte>(1024); //TODO capacity goed zo?
		this.outbox = new ArrayBlockingQueue<Byte>(1024); //TODO capacity goed zo?
		
		Thread exchanger = new Thread(this,"Exchanger");
		exchanger.start();
	}
	
	@Override
	public void sendByte(byte data) {
		outbox.add(data);
	}

	@Override
	public byte readByte() {
		try {
			return inbox.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0; //TODO iets van error flag?
		}
	}

	@Override
	public void run() {
		while(true){
			for(byte b : ByteArrays.fromBitSet(down.readFrame().getBitSet())){
				inbox.add(b);
			}
			down.sendFrame(new FlaggedFrame(outbox));
			down.exchangeFrame();
		}
	}

}

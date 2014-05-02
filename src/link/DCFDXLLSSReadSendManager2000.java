package link;

import java.util.concurrent.ArrayBlockingQueue;

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
			byte currentRead = down.readByte();
			if(currentRead!=FILLER_DATA){
				inbox.add(currentRead);
			}
			
			if(outbox.isEmpty()){
				down.sendByte(FlaggedFrame.FILLER_DATA);
			}else{
				try {
					down.sendByte(outbox.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
					down.sendByte(FILLER_DATA);
				}
			}
			down.exchangeFrame();
		}
	}

}

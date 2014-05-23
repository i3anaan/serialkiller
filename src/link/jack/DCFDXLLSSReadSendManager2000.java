package link.jack;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * DelayCorrectedFullDuplexLinkLayerSectionSegmentReadWriteSendManager2000
 * readByte() and sendByte() are NOT blocking;
 * 
 * @author I3anaan
 * 
 */

public class DCFDXLLSSReadSendManager2000 implements Runnable {
	DelayCorrectedFDXLinkLayerSectionSegment down;

	private ArrayBlockingQueue<Unit> inbox; // TODO frames van maken;
	private ArrayBlockingQueue<Unit> outbox; // TODO frames van maken;
	private Thread exchanger;

	public DCFDXLLSSReadSendManager2000(
			DelayCorrectedFDXLinkLayerSectionSegment down) {
		this.down = down;
		this.inbox = new ArrayBlockingQueue<Unit>(1024); // TODO capacity goed
															// zo?
		this.outbox = new ArrayBlockingQueue<Unit>(1024); // TODO capacity goed
															// zo?

		exchanger = new Thread(this, "Jack - Exchanger");
		exchanger.start();
	}

	public void sendUnit(Unit unit) {
		// down.log("Sending: "+unit);
		try {
			outbox.put(unit);
		} catch (InterruptedException e) {
			// TODO hier iets doen?
			e.printStackTrace();
		}
	}

	public Unit readUnit() {
		try {
			Unit read = inbox.take();
			return read;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null; // TODO iets van error flag?
		}
	}

	@Override
	public void run() {
		down.readFrame();
		while (true) {
			SimpleFrame frameToSend = new SimpleFrame(outbox);
			down.sendFrame(frameToSend);
			down.exchangeFrame();
			SimpleFrame f = down.readFrame();
			// System.out.println(Arrays.toString(f.getUnits()));
			for (Unit u : f.getUnits()) {
				try {
					if (!u.isFiller()) {
						inbox.put(u);
					} else {

					}
				} catch (InterruptedException e) {
					// TODO hier iets doen?
					e.printStackTrace();
				}
			}
		}
	}
}

package link.jack;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import util.ByteArrays;
import util.Bytes;

/**
 * DelayCorrectedFullDuplexLinkLayerSectionSegmentReadWriteSendManager2000
 * readByte() and sendByte() are NOT blocking;
 * 
 * @author I3anaan
 * 
 */

public class DCFDXLLSSReadSendManager2000 implements Runnable {
	DelayCorrectedFDXLinkLayerSectionSegment down;

	private ArrayBlockingQueue<PureUnit> inbox; // TODO frames van maken;
	private ArrayBlockingQueue<PureUnit> outbox; // TODO frames van maken;
	private Thread exchanger;
	private boolean keepRunning = true;

	public DCFDXLLSSReadSendManager2000(
			DelayCorrectedFDXLinkLayerSectionSegment down) {
		this.down = down;
		this.inbox = new ArrayBlockingQueue<PureUnit>(1024); // TODO capacity goed
															// zo?
		this.outbox = new ArrayBlockingQueue<PureUnit>(1024); // TODO capacity goed
															// zo?

		exchanger = new Thread(this, "Exchanger");
		exchanger.start();
	}

	public void sendByte(byte data) {
		try {
			outbox.put(new PureUnit(data));
		} catch (InterruptedException e) {
			// TODO hier iets doen?
			e.printStackTrace();
		}
	}

	public void sendUnit(PureUnit unit) {
		try {
			outbox.put(unit);
		} catch (InterruptedException e) {
			// TODO hier iets doen?
			e.printStackTrace();
		}
	}

	public PureUnit readUnit() {
		try {
			PureUnit read = inbox.take();
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
			if (keepRunning) {
				FlaggedFrame frameToSend = new FlaggedFrame(outbox);
				down.sendFrame(frameToSend);
				down.exchangeFrame();
				for (PureUnit u : down.readFrame().getUnits()) {
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

	public void setRun(boolean keepRunning) {
		System.out.println("Setting run to: " + keepRunning);
		this.keepRunning = keepRunning;

	}
}

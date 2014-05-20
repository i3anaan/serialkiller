package link.jack;
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
	private boolean keepRunning = true;

	public DCFDXLLSSReadSendManager2000(
			DelayCorrectedFDXLinkLayerSectionSegment down) {
		this.down = down;
		this.inbox = new ArrayBlockingQueue<Unit>(1024); // TODO capacity goed
															// zo?
		this.outbox = new ArrayBlockingQueue<Unit>(1024); // TODO capacity goed
															// zo?

		exchanger = new Thread(this, "Exchanger");
		exchanger.start();
	}
	public void sendUnit(Unit unit) {
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
			if (keepRunning) {
				FlaggedFrame frameToSend = new FlaggedFrame(outbox);
				down.sendFrame(frameToSend);
				down.exchangeFrame();
				for (Unit u : down.readFrame().getUnits()) {
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

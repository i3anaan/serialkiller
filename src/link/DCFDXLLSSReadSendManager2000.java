package link;

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

	public void sendByte(byte data) {
		try {
			outbox.put(new Unit(data));
		} catch (InterruptedException e) {
			// TODO hier iets doen?
			e.printStackTrace();
		}
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

	/**
	 * 
	 * Verkregen op normale Lpt:
	 * 
	 * Helo 39"&DXLS! This is my test mesage that is sent to confirm usability
	 * for the DCFDXLS link-layer serial line protocol. Mesage ends.Helo from
	 * DCFDXLS! This is my test mesage that is sent to confirm usability for the
	 * DCFDXLS link-layer serial line protocol. Mesage ends.Helo from DCFDXLS!
	 * This is my test mesage that is sent to confirm usability for the DCFDXLS
	 * link-layer serial line protocol. Mesage ends.Helo from DCFDXLS! This is
	 * my test mesage that is sent to confirm usability for the DCFDXLS
	 * link-layer serial line protocol.
	 * 
	 * 
	 * Opvallend: Merendeel goed, in het begin een raar teken. Geen dubbele
	 * tekens achterelkaar doorgegeven.
	 * 
	 * 
	 * Verkregen op Error-Lpt: üÿþÿüþÒ is sent to
	 * confirm usäÈÁaÿþÿ Lijkt gebitshift te zijn in zijn geheel.
	 * 
	 * 
	 */

}

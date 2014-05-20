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
public class DCFDXLLSSReadSendManager2000 extends LinkLayer implements Runnable {
	DelayCorrectedFDXLinkLayerSectionSegment down;

	private ArrayBlockingQueue<Byte> inbox; // TODO frames van maken;
	private ArrayBlockingQueue<Byte> outbox; // TODO frames van maken;
	private Thread exchanger;
	private boolean keepRunning = true;

	public DCFDXLLSSReadSendManager2000(
			DelayCorrectedFDXLinkLayerSectionSegment down) {
		this.down = down;
		this.inbox = new ArrayBlockingQueue<Byte>(1024); // TODO capacity goed
															// zo?
		this.outbox = new ArrayBlockingQueue<Byte>(1024); // TODO capacity goed
															// zo?

		exchanger = new Thread(this, "Exchanger");
		exchanger.start();
	}

	@Override
	public void sendByte(byte data) {
		try {
			// System.out.println("adding:  "+data +
			// " queue size: "+outbox.size());
			outbox.put(data);
			// System.out.println(data+"  added");

		} catch (InterruptedException e) {
			// TODO hier iets doen?
			e.printStackTrace();
		}
	}

	@Override
	public byte readByte() {
		try {
			byte read = inbox.take();
			// System.out.println("Reading byte:  "+Bytes.format(read)+" :  "+read);
			return read;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0; // TODO iets van error flag?
		}
	}

	@Override
	public void run() {
		down.readFrame();
		while (true) {
			if (keepRunning) {
				// System.out.println("Outbox: "+Arrays.toString(outbox.toArray()));
				FlaggedFrame frameToSend = new FlaggedFrame(outbox);
				// System.out.println("Pushing out flagged frame to send: "+frameToSend.payload);
				down.sendFrame(frameToSend);

				// System.out.println("sentFrame!");
				down.exchangeFrame();
				// System.out.println(Arrays.toString(down.readFrame().units));
				for (byte b : down.readFrame().getBitSet().toByteArray()) {
					// System.out.println("Putting in inbox:  "+Arrays.toString(down.readFrame().units));
					try {
						inbox.put(b);
						//System.out.print((char) (b & 0xFF));
					} catch (InterruptedException e) {
						// TODO hier iets doen?
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void setRun(boolean keepRunning) {
		System.out.println("Setting run to: "+keepRunning);
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

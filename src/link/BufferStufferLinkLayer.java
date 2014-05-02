package link;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import util.Bytes;

import common.Layer;

/**
 * The BufferStuffer link layer exchanges frames over unreliable links. 
 * 
 * The actual exchange of data is done in a seperate thread.
 */
public class BufferStufferLinkLayer implements Runnable {
	private Layer down;
	private ArrayBlockingQueue<byte[]> outbox;
	private ArrayBlockingQueue<byte[]> inbox;
	
	public static final int MAX_WAITING_FRAMES = 1024;
	public static final long MAX_WAIT = 1000000000; // One second
	
	private static final byte IDLE  = 0x00; // Line is idle.
	private static final byte RTS   = 0x01; // Request to send.
	private static final byte CTS   = 0x03; // Clear to send.
	private static final byte BYTE  = 0x01; // Byte done.
	private static final byte LEFT  = 0x02; // Left byte set.
	private static final byte RIGHT = 0x01; // Right byte set.
	private static final byte PANIC = 0x03; // Sheer and utter panic.
	
	private static final byte FLAG = 126; // 01111110
	
	private Thread t;
	
	private byte lastrecv = 0x00;
	
	public BufferStufferLinkLayer(Layer down) {
		this.down = down;
		this.outbox = new ArrayBlockingQueue<byte[]>(MAX_WAITING_FRAMES);
		this.inbox = new ArrayBlockingQueue<byte[]>(MAX_WAITING_FRAMES);
		
		t = new Thread(this);
		t.setName("BufferStuffer " + this.hashCode());
	}
	
	public void sendFrame(byte[] frame) throws InterruptedException {
		outbox.put(frame);
	}

	public byte[] readFrame() throws InterruptedException {
		return inbox.take();
	}
	
	public void start() {
		t.start();
	}
	
	private void setIdle()  { set(IDLE);  }
	private void setByte()  { set(BYTE); }
	private void setPanic() { set(PANIC); }
	
	private byte waitIdle() { return wait(IDLE); }
	private byte waitByte() { return wait(BYTE); }
	private byte waitPanic(){ return wait(PANIC); }

	@Override
	public void run() {
		// Startup: Panic, wait for panic, idle, wait for idle
		setPanic(); waitPanic();
		setIdle(); waitIdle();
		
		while (true) {
			try {
				byte in = get();
				
				if (in == RTS) {
					/* Got a request to send! */
					log("Got a RTS! Setting CTS.");
					set(CTS);
					
					// Would rather use ByteBuffer, but ByteBuffer is Java 7
					ArrayList<Byte> bytes = new ArrayList<Byte>();
					
					boolean eof = false;
					
					while (true) {
						
						byte b = 0;
						for (int i = 0; i < 8; i++) {
							log("Read waiting for idle.");
							waitIdle();
							log("Read wait for idle complete.");
							setIdle();
							
							switch(waitnot(IDLE)) {
							case LEFT:
								set(LEFT);
								break;
							case RIGHT:
								b |= (1 << i);
								set(RIGHT);
								break;
							case PANIC:
								log("Panic! Aargh!");
								set(PANIC);
								wait(PANIC);
								eof = true;
								break;
							}
							
							if (eof) break;
							
							log("Got bit " + Bytes.format(b));
						}
						
						if (!eof) {
							log("Got eight bits and no panic! Wait for end-of-byte signal");
							waitByte(); setByte();
						}
						
						if (b == FLAG) {
							log("Byte was flag byte, marks end of frame");
							break;
						} else {
							log("Byte was not flag byte, need more bytes for frame");
							bytes.add(b);
						}
					}
					
					byte[] out = Bytes.toArray(bytes);
					inbox.put(out);
					
					log("Received a whole frame.");
					log("Looks like we're done receiving. Set idle.");
					waitIdle(); setIdle();
				} else if (in == IDLE) {
					/* Check if we've got something to send. */
					if (!outbox.isEmpty()) {
						byte[] frame;
						
						frame = outbox.take();
						
						waitIdle();
						set(RTS);
						log("We want to send something!");
						
						
						byte response = waitnot(IDLE);
						
						if (response == RTS) {
							log("Collided! Time for sheer panic!");
							setPanic();
							waitPanic();
							log("Panic complete");
							continue;
						} else if (response == CTS) {
							log("Got cleared!");
							for (int x = 0; x < frame.length; x++) {
								byte out = frame[x];
								sendDataByte(out);
							}
							
							log("Pre-flag idle.");
							setIdle();
							waitIdle();
							
							log("Pre-flag.");
							sendDataByte(FLAG);
							log("Sent flag, returning to idle.");
							setIdle();
							timedWaitIdle();
							
							log("Both returned to idle.");
						} else {
							log("Received something other than CTS or RTS while waiting for response for RTS - panicing.");
							setPanic();
						}
					} else {
						setIdle();
					}
				} else if (in == PANIC) {
					log("Received PANIC, going idle");
					setIdle();
				} else {
					log("Received other than RTS while idle - ignoring - value was " + in);
					waitnot(in);
					log("Got unstuck!");
				}
			} catch (InterruptedException e) {
				log("InterruptedException?!");
				setPanic();
			}
		}
	}

	private void timedWaitIdle() {
		long t = System.nanoTime();
		long dt = MAX_WAIT;
		while (get() != IDLE && System.nanoTime() < t + dt);
	}

	private void sendDataByte(byte out) {
		for (int i = 0; i < 8; i++) {
			setIdle(); waitIdle();
			byte encoded = ((out & 1) == 0) ? LEFT : RIGHT;
			set(encoded); 
			log("Set to " + encoded);
			
			byte bitresponse = waitnot(IDLE);
			if (bitresponse != encoded) {
				set(PANIC);
				continue;
			}
			log("Received ack for " + encoded);
			
			out >>= 1;
		}
		
		log("Done sending byte data, signaling end of byte");
		setByte(); waitByte();
	}
	
	private void set(byte newstate) {
		down.sendByte(newstate);
	}
	
	private byte get() {
		lastrecv = down.readByte();
		return lastrecv;
	}
	
	private byte wait(byte newstate, boolean invert) {
		while (true) {
			byte b = get();
			if ((b == newstate) == !invert) return b;
		}
	}
	
	private byte wait(byte newstate) {
		return wait(newstate, false);
	}
	
	private byte waitnot(byte newstate) {
		return wait(newstate, true);
	}
	
	private void log(String msg, Object... arguments) {
		//System.out.printf("[%8x] " + msg + "%n", this.hashCode(), arguments);
	}
}

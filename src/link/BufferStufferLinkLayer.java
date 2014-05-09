package link;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import phys.PhysicalLayer;

import util.Bytes;

/**
 * The BufferStuffer link layer exchanges frames over unreliable links. 
 * 
 * The actual exchange of data is done in a seperate thread.
 */
public class BufferStufferLinkLayer extends FrameLinkLayer implements Runnable {
	private PhysicalLayer down;
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
	private byte lastsent = 0x00;
	
	public BufferStufferLinkLayer(PhysicalLayer down) {
		this.down = down;
		this.outbox = new ArrayBlockingQueue<byte[]>(MAX_WAITING_FRAMES);
		this.inbox = new ArrayBlockingQueue<byte[]>(MAX_WAITING_FRAMES);
		
		t = new Thread(this);
		t.setName("BufferStuffer " + this.hashCode());
	}
	
	public void sendFrame(byte[] frame) {
		try {
			outbox.put(frame);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] readFrame() {
		try {
			return inbox.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		t.start();
	}
	
	@Override
	public void run() {

		// Startup: Panic, wait for panic, idle, wait for idle
		while (true) {
			try {
			set(PANIC); wait(PANIC);
			set(IDLE); wait(IDLE);
			break;
			} catch (PanicException e) {
			}
		}
		
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
							wait(IDLE);
							log("Read wait for idle complete.");
							set(IDLE);
							
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
								throw new PanicException();
							}
							
							if (eof) break;
							
							log("Got bit " + Bytes.format(b));
						}
						
						if (!eof) {
							log("Got eight bits and no panic! Wait for end-of-byte signal");
							wait(BYTE); set(BYTE);
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
					wait(IDLE); set(IDLE);
				} else if (in == IDLE) {
					/* Check if we've got something to send. */
					if (!outbox.isEmpty()) {
						byte[] frame;
						
						frame = outbox.take();
						
						wait(IDLE);
						set(RTS);
						log("We want to send something!");
						
						
						byte response = waitnot(IDLE);
						
						if (response == RTS) {
							log("Collided! Time for sheer panic!");
							set(PANIC);
							wait(PANIC);
							log("Panic complete");
							continue;
						} else if (response == CTS) {
							log("Got cleared!");
							for (int x = 0; x < frame.length; x++) {
								byte out = frame[x];
								sendDataByte(out);
							}
							
							log("Pre-flag idle.");
							set(IDLE);
							wait(IDLE);
							
							log("Pre-flag.");
							sendDataByte(FLAG);
							log("Sent flag, returning to idle.");
							set(IDLE);
							wait(IDLE);
							
							log("Both returned to idle.");
						} else {
							log("Received something other than CTS or RTS while waiting for response for RTS - panicing.");
							set(PANIC);
						}
					} else {
						set(IDLE);
						set(IDLE);
					}
				} else if (in == PANIC) {
					throw new PanicException();
				} else {
					log("Received other than RTS while idle - ignoring - value was " + in);
					waitnot(in);
					log("Got unstuck!");
				}
			} catch (PanicException e) {
				log("Panic! Set-then-wait panic, then set-then-wait idle");
				boolean recovered = false;
				while (!recovered) {
					try {
					set(PANIC);
					wait(PANIC);
					set(IDLE);
					wait(IDLE);
					recovered = true;
					} catch (PanicException f) {
						/* Try again. */
					}
				}
				log("It seems we've recovered.");
			} catch (InterruptedException e) {
				log("InterruptedException?!");
				set(PANIC);
			}
		}
	}

	private void sendDataByte(byte out) {
		for (int i = 0; i < 8; i++) {
			set(IDLE); wait(IDLE);
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
		set(BYTE); wait(BYTE);
	}
	
	private void set(byte newstate) {
		lastsent = newstate;
		down.sendByte(newstate);
	}
	
	private byte get() {
		while (true) {
			set(lastsent);
			lastrecv = down.readByte();
			
			if (lastrecv == down.readByte() && lastrecv == down.readByte() && lastrecv == down.readByte()) {
				return lastrecv;
			}
		}
	}
	
	private byte wait(byte newstate, boolean invert) {
		long t = System.nanoTime();
		long dt = MAX_WAIT;
		
		while (true) {
			set(lastsent);
			byte b = get();
			if ((b == newstate) == !invert) return b;
			if ((System.nanoTime() >= t + dt)) throw new PanicException();
		}
	}
	
	private byte wait(byte newstate) {
		return wait(newstate, false);
	}
	
	private byte waitnot(byte newstate) {
		return wait(newstate, true);
	}
	
	private void log(String msg, Object... arguments) {
		System.out.printf("[%8x] " + msg + "%n", this.hashCode(), arguments);
	}
}

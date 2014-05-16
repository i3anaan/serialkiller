package link;

import java.util.Random;

import com.google.common.base.Charsets;

import log.LogMessage;
import log.Logger;
import phys.PhysicalLayer;
import stats.MonitoredQueue;
import util.ByteArrays;
import util.Bytes;

public class BittasticLinkLayer extends FrameLinkLayer implements Runnable {
	private PhysicalLayer down;
	private MonitoredQueue<byte[]> inbox;
	private MonitoredQueue<byte[]> outbox;
	private Logger log;
	
	private boolean ourClk = false;
	private boolean ourDat = false;
	
	private boolean primary = false;
	private boolean secondary = false;
	
	private static final long ELECTION_TIME = 1000; // 1s
	private static final long PACKET_SIZE = 1024; // bytes
	private static final long OVERHEAD = 4; // bytes
	
	private Random r;
	
	/** The physical layer implementation under this link layer. */
	public BittasticLinkLayer(PhysicalLayer phys) {
		super();
		down = phys;
		
		inbox = new MonitoredQueue<byte[]>("bittastic_in", 1024);
		outbox = new MonitoredQueue<byte[]>("bittastic_out", 1024);
		
		log = new Logger(LogMessage.Subsystem.LINK);
		
		r = new Random();
	}
	
	@Override
	public void sendFrame(byte[] data) {
		try {
			outbox.put(data);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return;
	}

	@Override
	public byte[] readFrame() {
		try {
			return inbox.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void set(int newstate) {
		set((byte)newstate);
	}
	
	private void set(byte newstate) {
		down.sendByte(newstate);
	}
	
	private byte get() {
		byte b = down.readByte();
		return b;
	}
	
	private boolean getTheirClock() {
		return (get() & 2) == 2;
		/*
		byte dampbuf = 85;
		
		for (int i = 0; i < 8; i++) {
			dampbuf <<= 1;
			dampbuf |= ((get() & 2) >> 1);
		}
		
		int onebits = Math.min(Integer.bitCount(dampbuf), 8);
		int nulbits = 8 - onebits;
		
		//log.debug("gtc ob " + onebits + " nb " + nulbits + " db " + dampbuf);
		
		return onebits >= nulbits;
		*/
	}
	
	private boolean getTheirData() {
		return (get() & 1) == 1;
	}
	
	private boolean getOurClock() {
		return ourClk;
	}
	
	private boolean getOurData() {
		return ourDat;
	}
	
	private void setClock(boolean state) {
		ourClk = state;
		set((ourClk ? 2 : 0) | (ourDat ? 1 : 0));
	}
	
	private void setData(boolean state) {
		ourDat = state;
		set((ourClk ? 2 : 0) | (ourDat ? 1 : 0));
	}
	
	private void toggleClk() {
		boolean state = getOurClock();
		setClock(!state);
	}
	
	private void waitClk() {
		waitClock(!getTheirClock());
	}
	
	private void waitClock(boolean state) {
		while (getTheirClock() != state);
	}

	@Override
	public void run() {
		log.debug("Bittastic starting (" + Thread.currentThread().getId() + ")");
		
		waitConnection();
		electPrimary();
		
		if (primary) {
			runPrimary();
		} else if (secondary) {
			runSecondary();
		} else {
			log.error("Neither primary nor secondary - election problems?" + primary + secondary);
		}
	}
	
	private void runPrimary() {
		char last16bits = 0;
		int x = 0;
		int y = 0;
		
		byte[] databytes = "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ ".getBytes(Charsets.UTF_8);
		
		
		while (true) {
			char out = packPair(databytes[x]);
			
			for (int i = 15; i >= 0; i--) {
				setData(((out >> i) & 1) == 1);
				setClock(true);
				waitClock(true);
	
				last16bits |= (getTheirData()?1:0);
				
				setClock(false);
				waitClock(false);
				
				if (validPair(last16bits)) {
					//log.debug("PRI " + Bytes.format(unpackPair(last16bits)) + "/" + (char)unpackPair(last16bits));
					//System.out.print((char)unpackPair(last16bits));
					last16bits = 0; // XXX Check if this is needed
					
					y++;
					if (y != 0 && y % 60 == 0) {
						y = 0;
						//System.out.println();
					}
				}
				
				last16bits <<= 1;
			}
			
			x++;
			x %= databytes.length;
		}
	}
	
	private void runSecondary() {
		char last16bits = 0;
		int x = 0;
		int y = 0;
		
		byte[] databytes = "Hello, world!".getBytes(Charsets.UTF_8);

		while (true) {
			char out = packPair(databytes[x]);

			for (int i = 15; i >= 0; i--) {
				waitClock(true);
				last16bits |= (getTheirData()?1:0);
	
				setClock(true);
				
				waitClock(false);
				setData((out & 1) == 1);
				setClock(false);
				
				if (validPair(last16bits)) {
					//log.debug("SEC " + Bytes.format(unpackPair(last16bits)) + "/" + (char)unpackPair(last16bits));
					
					System.out.print(new String(new byte[]{unpackPair(last16bits)}, Charsets.UTF_8));

					last16bits = 0; // XXX Check if this is needed
					
					y++;
					if (y != 0 && y % 80 == 0) {
						y = 0;
						System.out.println();
					}
				}
				
				last16bits <<= 1;
			}
			
			x++;
			x %= databytes.length;
		}
	}
	
	/**
	 * Checks if a char contains 16 bits that form a valid byte. A valid
	 * pair starts with a 0101 nibble, then 8 data bits, then a bit that
	 * that signifies the special status of the pair and finally three
	 * bits of parity, which is the popcount of the data modulo 8.
	 * 
	 * In other words, valid bytes have the following form:
	 * 1010 DDDD DDDD FPPP
	 * 
	 * D: Data bits
	 * F: Special (flag) bit
	 * P: Parity bit
	 */
	public static boolean validPair(char bits) {
		byte highnibble = (byte) (bits >> 12 & 15);
		byte outbyte = (byte) ((bits >> 4) & 0xFF);
		byte parity = (byte) (bits & 7);
		int bitcount = Integer.bitCount(outbyte) % 8;
		
		if (highnibble == 10) {
			return highnibble == 10 && bitcount == parity;
		} else {
			return false;
		}
	}
	
	public static byte unpackPair(char bits) {
		return (byte) ((bits >> 4) & 0xFF);
	}
	
	public static char packPair(byte b) {
		return (char) (40960 | (b << 4) | (Integer.bitCount(b) % 8));
	}

	private void electPrimary() {
		log.info("Primary election starts.");
		
		long start = System.currentTimeMillis();
		long timer = (long) (r.nextDouble() * ELECTION_TIME);
		
		while (!primary && !secondary) {
			if (getTheirClock() == false) {
				secondary = true;
				setData(true);
				setClock(false);
				break;
			} else if (getTheirData() == true) {
				primary = true;
				break;
			} else if (System.currentTimeMillis() > start + timer) {
				setClock(false);
			}
		}
		
		log.info("Primary election ends. We are " + (primary ? "PRIMARY" : "SECONDARY") + ".");
	}

	private void waitConnection() {
		log.debug("Startup procedure: Set our clock to 1 and wait for other host to come online.");
		setClock(true);
		waitClock(true);
	}
}
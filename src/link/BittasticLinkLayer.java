package link;

import java.util.Arrays;
import java.util.Random;

import common.Stack;
import common.Startable;

import log.LogMessage;
import log.Logger;
import phys.PhysicalLayer;
import stats.MonitoredQueue;

public class BittasticLinkLayer extends FrameLinkLayer implements Runnable, Startable {
	private PhysicalLayer down;
	private MonitoredQueue<byte[]> inbox;
	private MonitoredQueue<byte[]> outbox;
	private Logger log;
	
	private byte[] inbound = null;
	private byte[] outbound = null;
	private int inptr = 0;
	private int outptr = 0;
	
	private boolean ourClk = false;
	private boolean ourDat = false;
	
	private boolean primary = false;
	private boolean secondary = false;
	
	private static final byte ACK_FLAG = 'A';
	private static final byte EOF_FLAG = 'E';
	private static final byte ERR_FLAG = '!';
	
	private static final long ELECTION_TIME = 1000; // 1s
	
	private Random r;
	
	/** The physical layer implementation under this link layer. */
	public BittasticLinkLayer() {
		super();
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
	}
	
	private boolean getTheirData() {
		return (get() & 1) == 1;
	}
	
	private void setClock(boolean state) {
		ourClk = state;
		set((ourClk ? 2 : 0) | (ourDat ? 1 : 0));
	}
	
	private void setData(boolean state) {
		ourDat = state;
		set((ourClk ? 2 : 0) | (ourDat ? 1 : 0));
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
		
		while (true) {
			char out = buildOutboundPair();
			
			for (int i = 15; i >= 0; i--) {
				setData(((out >> i) & 1) == 1);
				setClock(true);
				waitClock(true);
	
				last16bits |= (getTheirData()?1:0);
				
				setClock(false);
				waitClock(false);
				
				if (isValidPair(last16bits)) {
					handleInboundPair(last16bits);
					last16bits = 0;
				}
				
				last16bits <<= 1;
			}
		}
	}
	
	/** Called by the main loop to ask for the next pair to send. */
	private char buildOutboundPair() {
		if (outbound == null) {
			try {
				outbound = outbox.poll();
				outptr = 0;
			} catch (InterruptedException e) {
				/* Do nothing. */
			}
		}
		
		if (outbound != null) {
			if (outptr == outbound.length) {
				outbound = null;
				return packPair(EOF_FLAG, true);
			} else {
			byte b = outbound[outptr];
			outptr++;
			return packPair(b, false);
			}
		}

		// If we don't have anything interesting to send, just send line idle.
		return 0;
	}
	
	/** Called by the main loop when it sees a new pair. */
	private void handleInboundPair(char pair) {
		if (inbound == null) {
			// Make room for an incoming message.
			inbound = new byte[1024];
			inptr = 0;
		}
		
		if (isSpecialPair(pair)) {
			switch (unpackPair(pair)) {
			case ACK_FLAG:
				log.debug("Ack!");
				break;
			case EOF_FLAG:
				try {
					inbox.put(Arrays.copyOf(inbound, inptr));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Arrays.fill(inbound, (byte)0);
				inptr = 0;
				break;
			case ERR_FLAG:
				log.error("Err!");
				break;
			}
		} else {
			inbound[inptr] = unpackPair(pair);
			inptr++;
		}
	}

	private boolean isSpecialPair(char pair) {
		return (pair >> 3 & 1) == 1;
	}

	private void runSecondary() {
		char last16bits = 0;
		
		while (true) {
			char out = buildOutboundPair();

			for (int i = 15; i >= 0; i--) {
				waitClock(true);
				last16bits |= (getTheirData() ? 1 : 0);
	
				setClock(true);
				waitClock(false);
				setData((out & 1) == 1);
				setClock(false);
				
				if (isValidPair(last16bits)) {
					handleInboundPair(last16bits);
					last16bits = 0;
				}
				
				last16bits <<= 1;
			}
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
	public static boolean isValidPair(char bits) {
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
	
	/** Unpacks a pair into a data byte. */
	public static byte unpackPair(char bits) {
		return (byte) ((bits >> 4) & 0xFF);
	}
	
	/** Packs a data byte into a pair. */
	public static char packPair(byte b, boolean special) {
		return (char) (40960 | (b << 4) | (Integer.bitCount(b) % 8) | (special ? 8 : 0));
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



	@Override
	public Thread start(Stack stack) {
		down = stack.physLayer;
		Thread t = new Thread(this);
		t.start();
		return t;
	}
}

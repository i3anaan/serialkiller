package bench;

import phys.DelayPhysicalLayer;
import phys.VirtualPhysicalLayer;
import link.LinkLayer;
import link.SimpleLinkLayer;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class SimpleTestbench {
	private class SeqThread extends Thread {
		private LinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThread(LinkLayer down) {
			this.down = down;
		}

		public void run() {
			while (true) {
				for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
					down.sendByte(i);
					byte in = down.readByte();
					
					if (in == i) {
						System.out.print(".");
						good++;
					} else {
						System.out.print("X");
						bad++;
					}
					
					if ((good+bad) % 64 == 0) System.out.printf(" %d/%d bytes good\n", good, good+bad);
					
					System.out.flush();
				}
			}
		}
	}

	private class EchoThread extends Thread {
		private LinkLayer down;

		public EchoThread(LinkLayer down) {
			this.down = down;
		}

		public void run() {
			while (true) {
				byte i = down.readByte();
				down.sendByte(i);
			}
		}
	}

	public static void main(String[] args) {
		new SimpleTestbench().run();
	}

	public void run() {
		System.out.println("SimpleTestbench");
		System.out.println("===============");
		System.out.println();
		
		VirtualPhysicalLayer vpla, vplb;
		
		vpla = new VirtualPhysicalLayer();
		vplb = new VirtualPhysicalLayer();

		vpla.connect(vplb);
		vplb.connect(vpla);

		LinkLayer a = new SimpleLinkLayer(new DelayPhysicalLayer(vpla));
		LinkLayer b = new SimpleLinkLayer(new DelayPhysicalLayer(vplb));
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
		
		Thread et = new EchoThread(a);
		Thread st = new SeqThread(b);
		
		et.start();
		st.start();
	}
}

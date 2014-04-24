package bench;

import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.PerfectVirtualPhysicalLayer;
import phys.VirtualPhysicalLayer;
import phys.VirtualPhysicalLayer;
import link.LinkLayer;
import link.SimpleLinkLayer;
import link.StatefulHDXLinkLayer;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class SimpleTestbench {
	private class SeqThread extends Thread {
		private LinkLayer down;
		int good = 0;
		int bad = 0;
		int oldgood = 0;
		int oldbad = 0;

		public SeqThread(LinkLayer down) {
			this.down = down;
		}

		public void run() {
			while (true) {
				for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
					down.sendByte(i);
					byte in = down.readByte();
					
					if (in == i) {
						good++;
					} else {
						bad++;
					}
					
					if ((good - oldgood) + (oldbad - bad) == 16) {
						StringBuilder sb = new StringBuilder();
						for (int x = 0; x < bad - oldbad; x++) sb.append("X");
						for (int x = 0; x < good - oldgood; x++) sb.append(".");
						sb.append(" ");
						sb.append(good);
						sb.append("/");
						sb.append(good+bad);
						System.out.println(sb);
						
						oldgood = good;
						oldbad = bad;
					}
					
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
		
		PerfectVirtualPhysicalLayer vpla, vplb;
		
		vpla = new PerfectVirtualPhysicalLayer();
		vplb = new PerfectVirtualPhysicalLayer();

		vpla.connect(vplb);
		vplb.connect(vpla);

		LinkLayer a = new StatefulHDXLinkLayer((vpla));
		LinkLayer b = new StatefulHDXLinkLayer((vplb));
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
		
		Thread et = new EchoThread(a);
		Thread st = new SeqThread(b);
		
		et.start();
		st.start();
	}
}

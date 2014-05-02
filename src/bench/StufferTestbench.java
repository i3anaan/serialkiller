package bench;

import java.util.Arrays;

import phys.VirtualPhysicalLayer;
import link.BufferStufferLinkLayer;

/**
 * A simple test bench application for testing the BufferStuffer link layer.
 */
public class StufferTestbench {
	private class SeqThread extends Thread {
		private BufferStufferLinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThread(BufferStufferLinkLayer b) {
			this.down = b;
		}

		public void run() {
			while (true) {
				try {
					for (byte i = 0; i < Byte.MAX_VALUE; i++) {
						byte[] out = {22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33};
						down.sendFrame(out);
						
						byte[] in = down.readFrame();
						
						if (Arrays.equals(in,  out)) {
							good++;
						} else {
							bad++;
						}
						
						if ((good+bad) % 64 == 0) System.out.printf(" %d/%d frames good\n", good, good+bad);
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private class EchoThread extends Thread {
		private BufferStufferLinkLayer down;

		public EchoThread(BufferStufferLinkLayer a) {
			this.down = a;
		}

		public void run() {
			while (true) {
				try {
					down.sendFrame(down.readFrame());
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void main(String[] args) {
		new StufferTestbench().run();
	}

	public void run() {
		System.out.println("BufferStufferTestbench");
		System.out.println("======================");
		System.out.println();
		
		VirtualPhysicalLayer vpla, vplb;
		
		vpla = new VirtualPhysicalLayer();
		vplb = new VirtualPhysicalLayer();

		vpla.connect(vplb);
		vplb.connect(vpla);

		BufferStufferLinkLayer a = new BufferStufferLinkLayer(vpla);
		a.start();
		BufferStufferLinkLayer b = new BufferStufferLinkLayer(vplb);
		b.start();
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
		
		Thread et = new EchoThread(a);
		Thread st = new SeqThread(b);
		
		et.start();
		st.start();
	}
}

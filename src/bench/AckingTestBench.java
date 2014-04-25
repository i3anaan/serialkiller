package bench;

import link.AckingLinkLayer;
import link.LinkLayer;
import link.SimpleLinkLayer;
import phys.DelayPhysicalLayer;
import phys.LptHardwareLayer;
import phys.VirtualPhysicalLayer;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class AckingTestBench {
	private class SeqThread extends Thread {
		private LinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThread(LinkLayer down) {
			this.down = down;
		}

		public void run() {
            int n = 0;
			while (true) {
				for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
					down.sendByte(i);
                    n++;
                    if (n % 128 == 0) System.out.printf("%d sent\n", n);
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
			}
		}
	}

	public static void main(String[] args) {
        if (args.length > 0) {
            new AckingTestBench().run(true);
        } else {
            new AckingTestBench().run();
        }
	}

	public void run(boolean hardware) {
		System.out.println("AckingTestbench");
		System.out.println("===============");
		System.out.println();

        LinkLayer a, b;

        if (hardware) {
            LptHardwareLayer hwla, hwlb;

            hwla = new LptHardwareLayer();
            hwlb = new LptHardwareLayer();

            a = new AckingLinkLayer(hwla);
            b = new AckingLinkLayer(hwlb);
        } else {
            VirtualPhysicalLayer vpla, vplb;

            vpla = new VirtualPhysicalLayer();
            vplb = new VirtualPhysicalLayer();

            vpla.connect(vplb);
            vplb.connect(vpla);

            a = new AckingLinkLayer(new DelayPhysicalLayer(vpla, 1));
            b = new AckingLinkLayer(new DelayPhysicalLayer(vplb, 1));
        }

        System.out.println("STACK A: " + a);
        System.out.println("STACK B: " + a);
        System.out.println();

        Thread et = new EchoThread(a);
        Thread st = new SeqThread(b);

        et.start();
        st.start();
	}

    public void run() {
        run(false);
    }
}

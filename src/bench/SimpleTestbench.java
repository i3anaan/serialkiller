package bench;

import link.*;
import phys.diag.BitErrorPhysicalLayer;
import phys.diag.CheckingPhysicalLayer;
import phys.diag.DebouncePhysicalLayer;
import phys.diag.DelayPhysicalLayer;
import phys.diag.DumpingPhysicalLayer;
import phys.diag.PerfectVirtualPhysicalLayer;
import phys.diag.VirtualPhysicalLayer;
import util.Bytes;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class SimpleTestbench {
	private class SeqThread extends Thread {
		private BytewiseLinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThread(BytewiseLinkLayer down) {
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
	
	private class SeqThreadSmall extends Thread {
		private BytewiseLinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThreadSmall(BytewiseLinkLayer down) {
			this.down = down;
		}

		public void run() {
			for(int i=0;i<3;i++) {
					down.sendByte((byte)(i+22));
					System.out.println("Sending byte");
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

	private class EchoThread extends Thread {
		private BytewiseLinkLayer down;

		public EchoThread(BytewiseLinkLayer down) {
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
		//vpla.connect(vplb);
		//vplb.connect(vpla);
		
		vpla.connect(vplb);
		vplb.connect(vpla);

		BytewiseLinkLayer a = null;// = new DCFDXLLSSRSM2000FrameReadSendManager3000(new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new BitErrorPhysicalLayer(new DelayPhysicalLayer(vpla)))));
		BytewiseLinkLayer b = null;// = new DCFDXLLSSRSM2000FrameReadSendManager3000(new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new BitErrorPhysicalLayer(new DelayPhysicalLayer(vplb)))));
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
		
		//System.out.println(a.hashCode()+"\tReadFirstByte:\t"+Bytes.format(a.readByte()));
		//System.out.println(b.hashCode()+"\tReadFirstByte:\t"+Bytes.format(b.readByte()));
		
		Thread et = new EchoThread(a);
		Thread st = new SeqThread(b);
		
		
		
		et.start();
		st.start();
	}
}

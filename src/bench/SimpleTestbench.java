package bench;

import phys.CheckingPhysicalLayer;
import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DelayPhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.VirtualPhysicalLayer;
import util.Bytes;
import link.DCFDXLLSSReadSendManager2000;
import link.DelayCorrectedFDXLinkLayerSectionSegment;
import link.DumpingLinkLayer;
import link.HighSpeedHDXLinkLayer;
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
	
	private class SeqThreadSmall extends Thread {
		private LinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThreadSmall(LinkLayer down) {
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

		LinkLayer a = new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new CleanStartPhysicalLayer(new DelayPhysicalLayer(vpla))));
		LinkLayer b = new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new CleanStartPhysicalLayer(new DelayPhysicalLayer(vplb))));
		
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

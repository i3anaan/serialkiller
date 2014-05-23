package bench;

import link.*;
import link.diag.BytewiseLinkLayer;
import link.jack.DCFDXLLSSReadSendManager2000;
import link.jack.DelayCorrectedFDXLinkLayerSectionSegment;
import link.jack.JackTheRipper;
import phys.diag.DelayPhysicalLayer;
import phys.diag.VirtualPhysicalLayer;
import util.Bytes;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class SimpleTestbench {
	private class SeqThread extends Thread {
		private FrameLinkLayer down;
		int good = 0;
		int bad = 0;

		public SeqThread(FrameLinkLayer down) {
			this.down = down;
		}

		public void run() {
			while (true) {
				for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
					//System.out.println("Sending Byte: "+Bytes.format((byte)(i)));
					down.sendFrame(new byte[]{(byte)(i)});
					byte in = down.readFrame()[0];
					//System.out.println("ReceivedByte: "+Bytes.format(in));
					
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
		private FrameLinkLayer down;

		public EchoThread(FrameLinkLayer down) {
			this.down = down;
		}

		public void run() {
			while (true) {
				byte i = down.readFrame()[0];
				down.sendFrame(new byte[]{i});
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

		FrameLinkLayer a = new JackTheRipper(new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new DelayPhysicalLayer(vpla))));
		FrameLinkLayer b = new JackTheRipper(new DCFDXLLSSReadSendManager2000(new DelayCorrectedFDXLinkLayerSectionSegment(new DelayPhysicalLayer(vplb))));
		
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

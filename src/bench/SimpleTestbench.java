package bench;

import common.Graph;

import link.*;
import link.angelmaker.AngelMaker;
import link.diag.BytewiseLinkLayer;
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
					down.sendFrame(new byte[]{(byte)(i)});
					byte[] bytes = down.readFrame();
					if(bytes.length>0){
					byte in = bytes[0];
					if(down instanceof AngelMaker){
						//Graph.makeImage(Graph.getFullGraphForNode(((AngelMaker) down).getCurrentSendingNode(), true));
					}
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
	}
	private class EchoThread extends Thread {
		private FrameLinkLayer down;

		public EchoThread(FrameLinkLayer down) {
			this.down = down;
		}

		public void run() {
			while (true) {
				byte[] bytes = down.readFrame();
				if(bytes.length>0){
				byte i = bytes[0];
				down.sendFrame(new byte[]{i});
				}
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

		FrameLinkLayer a = new AngelMaker(new DelayPhysicalLayer(vpla));
		FrameLinkLayer b = new AngelMaker(new DelayPhysicalLayer(vplb));
		
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

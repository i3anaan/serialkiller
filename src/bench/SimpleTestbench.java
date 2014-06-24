package bench;

import common.Graph;
import link.*;
import link.angelmaker.AngelMaker;
import link.angelmaker.manager.ThreadedAMManagerServer;
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
			for (byte i = 0; i < Byte.MAX_VALUE; i++) {
				down.sendFrame(new byte[]{(byte)(i)});
			}
		}
	}
	private class CheckThread extends Thread {
		private FrameLinkLayer down;

		public CheckThread(FrameLinkLayer down) {
			this.down = down;
		}

		public void run() {
			for (byte i = 0; i < Byte.MAX_VALUE; i += 0) {
				byte[] bytes = down.readFrame();
			
				if(bytes.length>0){
					int j;
					
					for (j = 0; j < bytes.length; j++) {
						if (bytes[j] == i) {
							System.out.printf("[OK] expected %d got %d\n", i, bytes[j]);
						} else {
							System.out.printf("[!!] expected %d got %d\n", i, bytes[j]);
						}
						
						i++;
					}
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

		FrameLinkLayer a = new AngelMaker(new DelayPhysicalLayer(vpla),null,null,null);
		FrameLinkLayer b = new AngelMaker(new DelayPhysicalLayer(vplb),null,null,null);
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
		
		//System.out.println(a.hashCode()+"\tReadFirstByte:\t"+Bytes.format(a.readByte()));
		//System.out.println(b.hashCode()+"\tReadFirstByte:\t"+Bytes.format(b.readByte()));
		
		Thread et = new CheckThread(a);
		Thread st = new SeqThread(b);
		
		et.start();
		st.start();
	}
}

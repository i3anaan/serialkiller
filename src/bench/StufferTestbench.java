package bench;

import com.google.common.base.Charsets;

import phys.diag.VirtualCable;
import phys.diag.VirtualCablePhysicalLayer;
import link.BufferStufferLinkLayer;

/**
 * A simple test bench application for testing the BufferStuffer link layer.
 */
public class StufferTestbench {
	private class SendThread extends Thread {
		private BufferStufferLinkLayer down;

		public SendThread(BufferStufferLinkLayer a) {
			this.down = a;
		}

		public void run() {
			while (true) {
				down.sendFrame("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".getBytes(Charsets.UTF_8));
			}
		}
	}
	
	private class RecvThread extends Thread {
		private BufferStufferLinkLayer down;

		public RecvThread(BufferStufferLinkLayer a) {
			this.down = a;
		}

		public void run() {
			int i = 0;
			
			while (true) {
				System.out.printf("%-20d %s\n", i, new String(down.readFrame(), Charsets.UTF_8));
				i++;
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
		
		VirtualCable cable = new VirtualCable();
		
		VirtualCablePhysicalLayer vpla = new VirtualCablePhysicalLayer(cable, 0);
		VirtualCablePhysicalLayer vplb = new VirtualCablePhysicalLayer(cable, 1);

		BufferStufferLinkLayer a = new BufferStufferLinkLayer(vpla);
		a.start();
		BufferStufferLinkLayer b = new BufferStufferLinkLayer(vplb);
		b.start();
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
		
		Thread et = new SendThread(a);
		Thread st = new RecvThread(b);
		
		et.start();
		st.start();
	}
}

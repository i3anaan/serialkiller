package bench;

import link.BufferStufferLinkLayer;
import phys.VirtualPhysicalLayer;

public class BufferStufferReceiver {
	public static void main(String[] args) throws Exception {
		BufferStufferLinkLayer bsll = new BufferStufferLinkLayer(new VirtualPhysicalLayer());
		
		while (true) {
			String frame = new String(bsll.readFrame(), "UTF-8");
			System.out.println(frame);
		}
	}

}

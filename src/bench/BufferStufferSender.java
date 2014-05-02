package bench;

import phys.VirtualPhysicalLayer;
import link.BufferStufferLinkLayer;

public class BufferStufferSender {
	public static void main(String[] args) throws Exception {
		BufferStufferLinkLayer bsll = new BufferStufferLinkLayer(new VirtualPhysicalLayer());
		bsll.sendFrame("Hello from BufferStufferLinkLayer!".getBytes("UTF-8"));
	}
}

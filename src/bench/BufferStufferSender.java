package bench;

import phys.LptErrorHardwareLayer;
import phys.VirtualPhysicalLayer;
import link.BufferStufferLinkLayer;

public class BufferStufferSender {
	public static void main(String[] args) throws Exception {
		BufferStufferLinkLayer bsll = new BufferStufferLinkLayer(new LptErrorHardwareLayer());
		bsll.start();
		while (true) bsll.sendFrame("Hello from BufferStufferLinkLayer!".getBytes("UTF-8"));
	}
}

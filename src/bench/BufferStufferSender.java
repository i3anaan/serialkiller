package bench;

import common.Stack;
import link.BufferStufferLinkLayer;
import phys.LptHardwareLayer;

public class BufferStufferSender {
	public static void main(String[] args) throws Exception {
		BufferStufferLinkLayer bsll = new BufferStufferLinkLayer();
        Stack stack = new Stack();
        stack.physLayer = new LptHardwareLayer();
		bsll.start(stack);
		while (true) bsll.sendFrame("Hello from BufferStufferLinkLayer!".getBytes("UTF-8"));
	}
}

package bench;

import common.Stack;
import link.BufferStufferLinkLayer;
import phys.diag.VirtualPhysicalLayer;

public class BufferStufferReceiver {
	public static void main(String[] args) throws Exception {
        BufferStufferLinkLayer bsll = new BufferStufferLinkLayer();
        Stack stack = new Stack();
        stack.physLayer = new VirtualPhysicalLayer();
        bsll.start(stack);
		
		while (true) {
			String frame = new String(bsll.readFrame(), "UTF-8");
			System.out.println(frame);
		}
	}

}

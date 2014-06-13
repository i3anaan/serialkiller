package link.diag;

import common.Stack;
import common.Startable;

import link.FrameLinkLayer;

public class MockFrameLinkLayer extends FrameLinkLayer implements Startable {

	@Override
	public void sendFrame(byte[] data) {
		/* Do nothing. */
	}

	@Override
	public byte[] readFrame() {
		while (true) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public Thread start(Stack stack) {
		return null;
	}
}

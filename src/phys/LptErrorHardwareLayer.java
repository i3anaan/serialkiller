package phys;

import common.Stack;
import common.Startable;

public class LptErrorHardwareLayer extends HardwareLayer implements Startable {
	private lpt.ErrorLpt lpt;

	public LptErrorHardwareLayer() {
		lpt = new lpt.ErrorLpt();
	}

	@Override
	public void sendByte(byte data) {
		lpt.writeLPT(data);
	}

	@Override
	public byte readByte() {
		return shuftRight(lpt.readLPT());
	}

	@Override
	public Thread start(Stack stack) {
		/* Does nothing. */
		return null;
	}
}

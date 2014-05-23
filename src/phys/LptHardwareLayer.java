package phys;

import common.Stack;
import common.Startable;

import lpt.Lpt;

/**
 * An implementation of HardwareLayer that uses the 'official', provided, Lpt
 * class.
 */
public class LptHardwareLayer extends HardwareLayer implements Startable {
	private Lpt lpt;

	public LptHardwareLayer() {
		lpt = new Lpt();
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

package phys.diag;

import common.Stack;
import common.Startable;

import phys.PhysicalLayer;

public class NullPhysicalLayer extends PhysicalLayer implements Startable {
	@Override
	public void sendByte(byte data) {
		/* Do nothing. */
	}

	@Override
	public byte readByte() {
		/* Return idle line. */
		return 0;
	}

	@Override
	public Thread start(Stack stack) {
		/* Do nothing. */
		return null;
	}
}

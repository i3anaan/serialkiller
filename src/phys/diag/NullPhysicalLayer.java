package phys.diag;

import phys.PhysicalLayer;

public class NullPhysicalLayer extends PhysicalLayer {
	@Override
	public void sendByte(byte data) {
		/* Do nothing. */
	}

	@Override
	public byte readByte() {
		/* Return idle line. */
		return 0;
	}
}

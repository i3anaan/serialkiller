package phys;

import lpt.Lpt;

/**
 * An implementation of HardwareLayer that uses the 'official', provided, Lpt class.
 */
public class LptHardwareLayer extends HardwareLayer {
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

}

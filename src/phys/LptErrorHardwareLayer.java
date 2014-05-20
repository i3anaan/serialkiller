package phys;

public class LptErrorHardwareLayer extends HardwareLayer {
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

}

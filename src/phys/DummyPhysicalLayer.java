package phys;

public class DummyPhysicalLayer extends PhysicalLayer {
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

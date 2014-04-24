package phys;

/**
 * The DelayPhysicalLayer adds an artificial, configurable delay to sending
 * bytes. The default is a delay of at least 5 ms per bit. The idea is to offer
 * the receiving side some time to pick up our signal, without setting a fixed
 * bit rate/baud rate.
 * 
 * Interruptions cause the delay to be canceled. Reading bytes is not slowed
 * down.
 */
public class DelayPhysicalLayer extends PhysicalLayer {
	private int delay;

	public DelayPhysicalLayer(PhysicalLayer down) {
		this(down, 1);
	}

	public DelayPhysicalLayer(PhysicalLayer down, int delay) {
		super();
		this.down = down;
		this.delay = delay;
	}

	@Override
	public void sendByte(byte data) {
		delay();
		down.sendByte(data);

	}

	@Override
	public byte readByte() {
		return down.readByte();
	}

	private void delay() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}
	}

}

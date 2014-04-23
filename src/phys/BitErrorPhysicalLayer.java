package phys;

import java.util.Random;

/**
 *
 */
public class BitErrorPhysicalLayer extends PhysicalLayer {
	private Random rand;
	private double chance = 0.0005;

	public BitErrorPhysicalLayer(PhysicalLayer down) {
		super();
		this.down = down;
		this.rand = new Random();
	}

	@Override
	public void sendByte(byte data) {
		if (rand.nextDouble() < chance) {
			down.sendByte((byte) (data ^ 3));
		} else {
			down.sendByte(data);
		}
	}

	@Override
	public byte readByte() {
		if (rand.nextDouble() < chance) {
			return (byte) (down.readByte() ^ 3);
		} else {
			return down.readByte();
		}
	}

}

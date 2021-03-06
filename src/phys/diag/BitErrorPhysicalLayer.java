package phys.diag;

import java.util.Random;

import phys.PhysicalLayer;

/**
 *
 */
public class BitErrorPhysicalLayer extends PhysicalLayer {
	protected PhysicalLayer down;
	private Random rand;
	private double chance = 0.003; //ErrorLPT chance is 0.3%

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

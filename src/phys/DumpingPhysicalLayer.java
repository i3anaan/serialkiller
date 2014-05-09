package phys;

/** A physical layer that dumps all bytes that pass through. */
public class DumpingPhysicalLayer extends PhysicalLayer {
	protected PhysicalLayer down;
	
	public DumpingPhysicalLayer(PhysicalLayer down) {
		super();
		this.down = down;
	}

	@Override
	public void sendByte(byte data) {
		System.out.printf("%8x send %d%d\n", this.hashCode(), (data & 2) >> 1, data & 1);
		down.sendByte(data);
		System.out.printf("%8x sent %d%d\n", this.hashCode(), (data & 2) >> 1, data & 1);
	}

	@Override
	public byte readByte() {
		System.out.printf("%8x recv\n", this.hashCode());
		byte data = down.readByte();
		System.out.printf("%8x rcvd %d%d\n", this.hashCode(), (data & 2) >> 1, data & 1);
		return data;
	}

}

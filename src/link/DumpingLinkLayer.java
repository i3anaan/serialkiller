package link;

import util.Bytes;

public class DumpingLinkLayer extends LinkLayer {
	public DumpingLinkLayer(LinkLayer down) {
		super();
		this.down = down;
	}
	
	@Override
	public void sendByte(byte data) {
		System.out.printf("%8x send byte %s\n", this.hashCode(), Bytes.format(data));
		down.sendByte(data);
		System.out.printf("%8x sent byte %s\n", this.hashCode(), Bytes.format(data));
	}

	@Override
	public byte readByte() {
		System.out.printf("%8x recv byte\n", this.hashCode());
		byte data = down.readByte();
		System.out.printf("%8x rcvd byte %s\n", this.hashCode(), Bytes.format(data));
		return data;
	}

}

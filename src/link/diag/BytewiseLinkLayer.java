package link.diag;

import link.LinkLayer;

public abstract class BytewiseLinkLayer extends LinkLayer {
	public abstract void sendByte(byte data);
	public abstract byte readByte();
}

package link.diag;

import link.BytewiseLinkLayer;

/**
 * A null implementation of a link layer. Reading from this link layer will
 * immediately return null (0) bytes, and writing to it will do precisely
 * nothing.
 */
public class NullLinkLayer extends BytewiseLinkLayer {
	@Override
	public void sendByte(byte data) {
		/* Do nothing. */
	}

	@Override
	public byte readByte() {
		/* Pretend we've read a byte. */
		return 0;
	}

}

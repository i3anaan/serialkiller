package link;

/** A FrameLinkLayer that does nothing. */
public class NullFrameLinkLayer extends FrameLinkLayer {
	@Override
	public void sendFrame(byte[] data) {
		/* Do nothing. */
	}

	@Override
	public byte[] readFrame() {
		return new byte[]{};
	}
}

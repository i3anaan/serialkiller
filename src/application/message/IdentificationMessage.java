package application.message;

public class IdentificationMessage extends ApplicationLayerMessage {

	public IdentificationMessage(byte address, byte[] data) {
		super(address,data);
	}
}

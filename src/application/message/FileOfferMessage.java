package application.message;

import network.Payload;

public class FileOfferMessage extends FileMessage {
	public FileOfferMessage(byte destination, int size, String name) {
		super(destination, (byte) 'F', size, name);
	}

	public FileOfferMessage(Payload p) {
		super(p);
	}
	
	public String getKey() {
		return String.format("%s-%s-%s", getAddress(), getFileSize(), getFileName());
	}
	
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
}

package application.message;

import network.Payload;

public class FileAcceptMessage extends FileMessage {
	public FileAcceptMessage(byte destination, int size, String name) {
		super(destination, (byte) 'A', size, name);
	}
	
	public FileAcceptMessage(FileOfferMessage offer) {
		this(offer.getAddress(), offer.getFileSize(), offer.getFileName());
	}

	public FileAcceptMessage(Payload p) {
		super(p);
	}

}

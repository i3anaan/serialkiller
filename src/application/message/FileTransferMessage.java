package application.message;

import network.Payload;

public class FileTransferMessage extends FileMessage {
	public FileTransferMessage(byte destination, int size, String name, byte[] data) {
		super(destination, (byte) 'S', size, name, data);
	}

	public FileTransferMessage(Payload p) {
		super(p);
	}
}



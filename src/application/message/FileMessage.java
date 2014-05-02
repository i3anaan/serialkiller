package application.message;

import java.util.Arrays;

public abstract class FileMessage extends ApplicationLayerMessage {

	// Private variables
	private final int fileSize;
	private final String fileName;
	
	public FileMessage(byte[] data) {
		super(data);
		byte [] size = Arrays.copyOfRange(data, 0, 31);
		byte [] name = Arrays.copyOfRange(data, 32, data.length);
	}
	
	

}

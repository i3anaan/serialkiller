package application;

import java.util.Arrays;

import application.message.FileMessage;

public class FileTransferMessage extends FileMessage {

	// Private variables
	private final byte[] fileBytes;
	
	public FileTransferMessage(byte[] data) {
		super(data);
		fileBytes = Arrays.copyOfRange(data, 1, data.length);
		
	}
	
	/**
	 * Returns the byte array containing all of the bytes
	 * of the transfered file
	 * @return Bytes from file
	 */
	public byte[] getFileBytes(){
		return fileBytes;
	}

}

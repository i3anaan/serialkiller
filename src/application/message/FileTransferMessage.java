package application.message;

import java.util.Arrays;

public class FileTransferMessage extends FileMessage {

	// Private variables
	private byte[] fileBytes;

	public FileTransferMessage(byte address, byte[] data) {
		super(address, data);

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

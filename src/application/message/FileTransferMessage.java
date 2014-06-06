package application.message;

import java.util.Arrays;

public class FileTransferMessage extends FileMessage {

	// Private variables
	private final byte[] fileBytes;
	
	public FileTransferMessage(byte address, byte[] data) {
		super(address, data);
		int i = 0;
		for(i = 0; i < data.length; i++){
			if(data[i] == (byte)0){
				break;
			}
		}
		fileBytes = Arrays.copyOfRange(data, i, data.length);
		
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

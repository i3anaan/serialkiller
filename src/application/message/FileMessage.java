package application.message;

import java.util.Arrays;

public abstract class FileMessage extends ApplicationLayerMessage {

	// Private variables
	private final int fileSize;
	private final String fileName;
	
	public FileMessage(byte address, byte[] data) {
		super(address, data);
		byte [] size = Arrays.copyOfRange(data, 1, 5);
		byte [] name = Arrays.copyOfRange(data, 5, data.length);
		fileSize = Integer.parseInt(new String(size));
		fileName = new String(name);
	}
	
	/**
	 * Returns the size of the file
	 * @return size of file
	 */
	public int getFileSize(){
		return fileSize;
	}
	
	/**
	 * Returns the name of the file
	 * @return filename
	 */
	public String getFileName(){
		return fileName;
	}
	
	

}

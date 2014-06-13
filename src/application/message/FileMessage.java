package application.message;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class FileMessage extends ApplicationLayerMessage {

	// Private variables
	private final int fileSize;
	private final String fileName;
	
	public FileMessage(byte address, byte[] data) {
		super(address, data);
		byte [] size = Arrays.copyOfRange(data, 1, 5);
		byte [] name = Arrays.copyOfRange(data, 5, data.length);
		int derp = ByteBuffer.allocate(4).wrap(size).getInt();
	
		//TODO DEBUG LINE
		//System.out.println("DE FILESIZE: " +derp);
		fileSize = 	derp;
		fileName = new String(name).trim();
		//System.out.println("THE NAME : " +fileName);
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

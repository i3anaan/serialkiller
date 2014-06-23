package application.message;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.google.common.primitives.Bytes;

public abstract class FileMessage extends ApplicationLayerMessage {

	// Private variables
	private int fileSize;
	private String fileName;
	
	public FileMessage(byte address, byte[] data) {
		super(address, data);
		setFileSize(data);
		setFileName(data);
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
	
	/**
	 * sets the fileSize, should only be used once
	 * @param data
	 */
	private void setFileSize(byte[] data){
		byte [] size = Arrays.copyOfRange(data, 1, 5);
		int derp = ByteBuffer.allocate(4).wrap(size).getInt();
		fileSize = 	derp;
	}
	
	/**
	 * sets the fileName, should only be used once
	 * @param data
	 */
	private void setFileName(byte[] data){
		if (this instanceof FileTransferMessage){
			
			byte [] namedata = Arrays.copyOfRange(data, 5, data.length);
			byte [] name = Arrays.copyOfRange(namedata, 0, Bytes.indexOf(namedata, (byte)'\0'));
			fileName = new String(name).trim();
		}else{
			
			byte [] name = Arrays.copyOfRange(data, 5, data.length);
			fileName = new String(name).trim();
		}
	}

}

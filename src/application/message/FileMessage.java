package application.message;

import java.nio.charset.Charset;
import java.util.Arrays;

import network.Payload;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class FileMessage extends ApplicationLayerMessage {
	private int size;
	private String name;
	private byte[] data;
	
	private static final Charset UTF = Charsets.UTF_8;
	
	public FileMessage(Payload p) {
		super(p);
		
		int i;
		for (i = 5; i < p.data.length && p.data[i] != 0; i++);
		
		size = Ints.fromByteArray(Arrays.copyOfRange(p.data, 1, 5));
		name = new String(p.data, 5, i - 5, Charsets.UTF_8);
		data = Arrays.copyOfRange(p.data, i, p.data.length);
	}
	
	public FileMessage(byte destination, byte type, int size, String name) {
		this(destination, type, size, name, new byte[]{});
	}
	
	public FileMessage(byte destination, byte type, int size, String name, byte[] data) {
		super(destination);
		
		byte[] msg = Bytes.concat(new byte[]{type}, Ints.toByteArray(size), name.getBytes(UTF));
		
		if (data.length > 0) {
			msg = Bytes.concat(msg, new byte[]{(byte)0}, data);
		}
		
		setData(msg);
	}
	
	/** Returns the size of the file. */
	public int getFileSize(){
		return size;
	}
	
	/** Returns the name of the file. */
	public String getFileName(){
		return name;
	}
	
	/** Returns the file data. */
	public byte[] getData() {
		return data;
	}
}

package phys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of HardwareLayer that uses a custom OutputStream to a named
 * file.
 */
public class FileHardwareLayer extends HardwareLayer {
	private OutputStream os;
	private InputStream is;

	public FileHardwareLayer() {
		this("/telpparport");
	}

	public FileHardwareLayer(String filename) {
		super();
		try {
			File f = new File(filename);
			is = new FileInputStream(f);
			os = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendByte(byte data) {
		try {
			os.write(data);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte readByte() {
		try {
			return (byte) is.read();
		} catch (IOException e) {
			e.printStackTrace();
			return (byte) 0;
		}
	}

}

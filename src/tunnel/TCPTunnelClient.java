package tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import common.Layer;

class TCPTunnelClient extends Layer {
	private Socket s;
	private String host;
	private int port;
	
	private InputStream is;
	private OutputStream os;
	
	public TCPTunnelClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void connect() throws IOException {
		s = new Socket(host, port);
		is = s.getInputStream();
		os = s.getOutputStream();
	}
	
	@Override
	public void sendByte(byte data) {
		try {
			os.write(data);
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte readByte() {
		try {
			byte[] b = {0};
			is.read(b);
			return b[0];
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
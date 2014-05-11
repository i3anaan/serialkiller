package web;

import java.io.*;
import java.net.Socket;

class WebWorker extends Thread {
	private Socket sock;
	private InputStream is;
	private BufferedReader br;
	private OutputStream os;
	private PrintWriter pw;
	
	private Logger log;
	private WebService service;
	
	public WebWorker(Socket sock, WebService service) {
		this.sock = sock;
		this.log = new Logger(LogMessage.Subsystem.WEB);
		this.service = service;
	}
	
	public void run() {
		try {
			is = sock.getInputStream();
			os = sock.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            String headerLine = br.readLine();
            if (headerLine == null) return;

			String[] headerBits = headerLine.split(" ");
			String method = headerBits[0];
			String path = headerBits[1];
            String proto = headerBits[2];

            log.info(String.format("%s %s %s %s", sock.getInetAddress(), method, path, proto));
			
			Request request = new Request(method, path);
			Router router = service.getRouter();
			ServiceHandler handler = router.route(request);
			Response response = handler.handleRequest(request);
			pw.print(response.asString());
			
			pw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

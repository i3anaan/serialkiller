package web;

import stats.Stats;

import java.io.IOException;
import java.net.ServerSocket;

public class WebService {
	private ServerSocket ssock;
	private Logger log;
	private Router router;
	
	private static final int PORT = 8080;
	
	public WebService() {
		log = new Logger(LogMessage.Subsystem.WEB);
	}
	
	public void run() throws IOException {
		ssock = new ServerSocket(PORT);
		log.info("WebService starting");
		
		router = new Router();
        router.register(ChatHandler.class);
		router.register(IndexHandler.class);
        router.register(FilesHandler.class);
        router.register(LogDisplayHandler.class);
        router.register(StatsHandler.class);
		
		while (true) {
			new WebWorker(ssock.accept(), this).start();
            Stats.hit("web.connectionsAccepted");
        }
	}
	
	public Router getRouter() {
		return router;
	}
	
	public static void main(String[] args) {
		try {
			WebService ws = new WebService();
			ws.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
package web;

import stats.Stats;

import java.io.IOException;
import java.net.ServerSocket;

/** Main class for the WebService subsystem. */
public class WebService {
    private static final int PORT = 8080;
    private Logger log;
    private Router router;

    /** Make a new WebService. */
    public WebService() {
        log = new Logger(LogMessage.Subsystem.WEB);
        router = new Router();
    }

    /** Web service command-line entry point. */
    public static void main(String... args) {
        try {
            WebService ws = new WebService();
            ws.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Runs the WebService. */
    public void run() throws IOException {
        log.info("WebService starting");

        router.register(ChatHandler.class,
                        IndexHandler.class,
                        FilesHandler.class,
                        LogDisplayHandler.class,
                        StatsHandler.class);

        ServerSocket ssock = new ServerSocket(PORT);

        try {
            while (true) {
                new WebWorker(ssock.accept(), this).start();
                Stats.hit("web.connectionsAccepted");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            ssock.close();
        }
    }

    Router getRouter() {
        return router;
    }
}
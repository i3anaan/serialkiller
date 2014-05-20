package web;

import stats.Stats;

import java.io.IOException;
import java.net.ServerSocket;

import log.LogMessage;
import log.Logger;

/** Main class for the WebService subsystem. */
public class WebService implements Runnable {
    private Logger log;
    private Router router;
    private int port;

    private final Object ready = new Object(); // Notified when service is ready

    /** Make a new WebService that listens on the given port. */
    public WebService(int serverPort) {
        log = new Logger(LogMessage.Subsystem.WEB);
        router = new Router();
        port = serverPort;
    }

    /** Web service command-line entry point. */
    public static void main(String... args) {
        WebService ws = new WebService(8080);
        ws.run();
    }

    /** Runs the WebService. */
    @Override
    public void run() {
        ServerSocket ssock = null;

        try {
            log.info("WebService starting");

            router.register(ChatHandler.class);
            router.register(IndexHandler.class);
            router.register(FilesHandler.class);
            router.register(LogDisplayHandler.class);
            router.register(StatusHandler.class);
            
            ssock = new ServerSocket(port);
            markReady();

            while (true) {
                new WebWorker(ssock.accept(), this).start();
                Stats.hit("web.connectionsAccepted");
            }
        } catch (IOException e) {
            e.printStackTrace();
		} finally {
            if (ssock != null) {
                try {
                    ssock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Mark this instance as being ready. */
    private void markReady() {
        synchronized(ready) { ready.notifyAll(); }
    }

    /** Wait for this instance to start serving. */
    public void waitReady() throws InterruptedException {
        synchronized (ready) { ready.wait(); }
    }

    Router getRouter() {
        return router;
    }
}
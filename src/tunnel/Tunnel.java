package tunnel;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Represents a tunnel with another host.
 */
public class Tunnel implements Runnable {
    /** The IP address of the host this tunnel connects to. */
    private String ip;

    /** Whether this tunnel should automatically (re)connect. */
    private boolean autoconnect;

    /** The socket for the tunnel. */
    private Socket socket;

    /** The thread for this tunnel. */
    private Thread t;

    /** Whether to continue running. */
    private boolean run;

    private Tunnel(boolean autoconnect) {
        this.autoconnect = autoconnect;
        this.t = new Thread(this);
    }

    public Tunnel(Socket socket, boolean autoconnect) {
        this(autoconnect);
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
    }

    public Tunnel(String ip, boolean autoconnect) {
        this(autoconnect);
        this.ip = ip;
        this.autoconnect = false;
    }

    public String ip() {
        return ip;
    }

    public boolean autoconnect() {
        return autoconnect;
    }

    public boolean connected() {
        return socket.isConnected();
    }

    public boolean connect() {
        boolean success = true;

        if ((socket == null || !socket.isConnected()) && autoconnect) {
            try {
                socket = new Socket(ip, Tunneling.PORT);
            } catch (UnknownHostException e) {
                Tunneling.getLogger().error("Unable to connect " + toString() + " (host " + ip + " unknown).");
                success = false;
            } catch (IOException e) {
                Tunneling.getLogger().error("Unable to connect " + toString() + " (unknown error).");
                success = false;
            }
        }

        return success;
    }

    @Override
    public void run() {
        // Make sure we have a working socket.
        if (!this.connect()) {
            run = false;
        }

        while(run) {
            // TODO: Actually implement the tunnel.
        }

        Tunneling.getLogger().warning(toString() + " stopped.");
    }

    public void start() {
        run = true;
        t.start();
        Tunneling.getLogger().warning(toString() + " started.");
    }

    public void stop() {
        try {
            run = false;
            t.join();
        } catch (InterruptedException e) {
            // Do nothing.
        }
    }

    public String toString() {
        return "Tunnel<" + ip + ">";
    }
}

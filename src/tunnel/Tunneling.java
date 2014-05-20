package tunnel;

import log.LogMessage;
import log.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class that manages tunnels.
 */
public class Tunneling implements Runnable {
    public static final int PORT = 1337;

    /** The tunnels. */
    private Map<String, Tunnel> tunnels;

    /** The socket. */
    private ServerSocket socket;

    /** The thread for tunneling. */
    private Thread t;

    /** Whether to continue running. */
    private boolean run;

    /** The logger. */
    private static Logger logger;

    public Tunneling() {
        tunnels = new TreeMap<String, Tunnel>();
    }

    /**
     * Creates a new tunnel to the given IP address.
     * @param ip The IP address of the remote host.
     * @param autoconnect Whether to automatically connect this tunnel.
     * @return The tunnel.
     */
    public Tunnel create(String ip, boolean autoconnect) {
        // Create the new tunnel.
        Tunnel tunnel = new Tunnel(ip, autoconnect);

        // Perform tunnel create actions.
        _create(tunnel);

        return tunnel;
    }

    protected Tunnel create(Socket socket, boolean autoconnect) {
        // Create the new tunnel.
        Tunnel tunnel = new Tunnel(socket, autoconnect);

        // Perform tunnel create actions.
        _create(tunnel);

        return tunnel;
    }

    private void _create(Tunnel tunnel) {
        // Remove and stop the old tunnel if present.
        if (tunnels.containsKey(tunnel.ip())) {
            Tunnel old = tunnels.remove(tunnel.ip());
        }

        // Add the new tunnel to the collection.
        tunnels.put(tunnel.ip(), tunnel);
        Tunneling.getLogger().debug(tunnel.toString() + " created.");

        // Start the new tunnel if necessary.
        if (tunnel.connected()) {
        }
    }

    /**
     * Returns the known tunnels.
     * @return A collection of the known tunnels.
     */
    public Collection<Tunnel> tunnels() {
        return tunnels.values();
    }

    @Override
    public void run() {
        // Connect the socket.
        try {
            socket = new ServerSocket(PORT);
        } catch (IOException e) {
            Tunneling.getLogger().critical(String.format("Cannot listen on port %d, exiting...", PORT));
            run = false;
        }

        while(run) {
            try {
                Socket s = socket.accept();
                this.create(s, false);
            } catch (IOException e) {
                Tunneling.getLogger().error("Error with accepting a new connection.");
            }
        }

        Tunneling.getLogger().warning("Tunneling stopped.");
    }

    public void start() {
        run = true;
        t.start();
        Tunneling.getLogger().warning("Tunneling started.");
    }

    public void stop() {
        try {
            run = false;
            t.join();
        } catch (InterruptedException e) {
            // Do nothing.
        }
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger(LogMessage.Subsystem.TUNNEL);
        }
        return logger;
    }
}

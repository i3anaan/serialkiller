package tunnel;

import log.LogMessage;
import log.Logger;
import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class that manages tunnels.
 */
public class Tunneling implements Runnable {
    public static final int PORT = 1337;

    /** The tunnels. */
    private Map<String, Tunnel> tunnels;

    /** The socket. */
    private ServerSocket socket;

    /** The queue to the network layer. */
    private ArrayBlockingQueue<Packet> queue;

    /** The thread for tunneling. */
    private Thread t;

    /** Whether to continue running. */
    private boolean run;

    /** The logger. */
    private static Logger logger;

    public Tunneling(TPPNetworkLayer parent) {
        queue = parent.queue();
        tunnels = new TreeMap<String, Tunnel>();

        t = new Thread(this);
        t.setName("TPP Tunneling " + hashCode());
    }

    /**
     * Creates a new tunnel to the given IP address.
     * @param ip The IP address of the remote host.
     * @param autoconnect Whether to automatically connect this tunnel.
     * @return The tunnel.
     */
    public Tunnel create(String ip, boolean autoconnect) {
        // Create the new tunnel.
        Tunnel tunnel = new Tunnel(ip, queue, autoconnect);

        // Perform tunnel create actions.
        register(tunnel);

        return tunnel;
    }

    protected Tunnel create(Socket socket, boolean autoconnect) {
        // Create the new tunnel.
        Tunnel tunnel = new Tunnel(socket, queue, autoconnect);

        // Perform tunnel create actions.
        register(tunnel);

        return tunnel;
    }

    private void register(Tunnel tunnel) {
        // Remove and stop the old tunnel if present.
        if (tunnels.containsKey(tunnel.ip())) {
            Tunnel old = tunnels.remove(tunnel.ip());
            old.stop();
        }

        // Add the new tunnel to the collection.
        tunnels.put(tunnel.ip(), tunnel);
        Tunneling.getLogger().debug(tunnel.toString() + " created.");
    }

    /**
     * Sends a packet over the matching tunnel. May drop the packet if a queue
     * is full or no tunnel is known for the packet.
     * @param p The packet.
     * @param ip The IP address of the remote host.
     */
    public void send(Packet p, String ip) {
        Tunnel t = tunnels.get(ip);

        Tunneling.getLogger().debug(p.toString() + " received by tunneling.");

        if (t != null) {
            t.offer(p);
        } else {
            Tunneling.getLogger().warning(p.toString() + " dropped, no tunnel found (IP: " + ip + ").");
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

    /**
     * Closes and removes all tunnels.
     */
    public void clear() {
        stopTunnels();
        tunnels.clear();
    }

    private void startTunnels() {
        for (Tunnel t : tunnels.values()) {
            t.start();
        }
    }

    private void stopTunnels() {
        for (Tunnel t: tunnels.values()) {
            t.stop();
        }
    }

    public void start() {
        run = true;
        t.start();
        startTunnels();
        Tunneling.getLogger().warning("Tunneling started.");
    }

    public void stop() {
        stopTunnels();
        run = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            // Do nothing.
        }
        Tunneling.getLogger().warning("Tunneling stopped.");
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger(LogMessage.Subsystem.TUNNEL);
        }
        return logger;
    }
}

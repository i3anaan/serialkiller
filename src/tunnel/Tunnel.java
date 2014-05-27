package tunnel;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;
import network.tpp.PacketHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Represents a tunnel with another host.
 */
public class Tunnel implements Runnable {
    public static final int RECONNECT_TIMEOUT = 100;

    /** The IP address of the host this tunnel connects to. */
    protected String ip;

    /** Whether this tunnel should automatically (re)connect. */
    protected boolean autoconnect;

    /** The socket for the tunnel. */
    protected Socket socket;

    /** The packet queues for this tunnel. */
    private ArrayBlockingQueue<Packet> in;
    private ArrayBlockingQueue<Packet> out;

    /** The reader and writer. */
    private TunnelReader reader;
    private TunnelWriter writer;

    /** The thread. */
    private Thread t;

    /** Whether to continue running this tunnel. */
    private boolean run;

    /**
     * Constructs a new Tunnel instance. Sets up some basic parameters.
     * @param in The queue for packets traveling into the network layer.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     */
    private Tunnel(ArrayBlockingQueue<Packet> in, boolean autoconnect) {
        this.autoconnect = autoconnect;
        this.in = in;
        out = new ArrayBlockingQueue<Packet>(TPPNetworkLayer.QUEUE_SIZE);
    }

    /**
     * Constructs a new Tunnel instance based on an existing socket. This socket
     * will be used for communication.
     * @param socket The socket.
     * @param in The queue for packets traveling into the network layer.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     */
    public Tunnel(Socket socket, ArrayBlockingQueue<Packet> in, boolean autoconnect) {
        this(in, autoconnect);
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();

        t = new Thread(this);
        t.setName(toString());
    }

    /**
     * Constructs a new Tunnel instance. A connection will be set up with the
     * given IP address.
     * @param ip The IP address of the remote host.
     * @param in The queue for packets traveling into the network layer.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     */
    public Tunnel(String ip, ArrayBlockingQueue<Packet> in, boolean autoconnect) {
        this(in, autoconnect);
        this.ip = ip;
        this.autoconnect = false;

        t = new Thread(this);
        t.setName(toString());
    }

    /**
     * Returns the IP address of the remote host.
     * @return The IP address of the remote host.
     */
    public String ip() {
        return ip;
    }

    /**
     * Returns whether this tunnel will automatically (re)connect.
     * @return Whether this tunnel will automatically (re)connect.
     */
    public boolean autoconnect() {
        return autoconnect;
    }

    /**
     * Returns whether this tunnel is connected.
     * @return Whether this tunnel is connected.
     */
    public boolean connected() {
        return socket != null && socket.isConnected();
    }

    /**
     * Offer a packet to the tunnel. Is a proxy for ArrayBlockingQueue.offer().
     * May drop a packet if the queue is full.
     * @param p The packet.
     */
    public void offer(Packet p) {
        if (!out.offer(p)) {
            Tunneling.getLogger().warning(p.toString() + " dropped, " + toString() + " queue full.");
        }
    }

    /**
     * Connects this tunnel. Creates a new socket if this tunnel does not have
     * a socket or if the socket is not connected and autoconnect() is true.
     * @return Whether the connection is successful.
     */
    public boolean connect() {
        boolean success = true;

        Tunneling.getLogger().warning("Connecting " + toString() + ".");

        if (socket == null) {
            try {
                socket = new Socket(ip, Tunneling.PORT);
            } catch (IOException e) {
                Tunneling.getLogger().error("Unable to set up " + toString() + " (" + e.getMessage() + ").");
                success = false;
            }
        }

        if (socket != null && !socket.isConnected() && autoconnect) {
            try {
                socket.connect(new InetSocketAddress(ip, Tunneling.PORT));
            } catch (IOException e) {
                Tunneling.getLogger().error("Unable to connect " + toString() + " (" + e.getMessage() + ").");
                success = false;
            }
        }

        if (success) {
            Tunneling.getLogger().warning(toString() + " connected.");
        }

        return success;
    }

    public String toString() {
        return "Tunnel<" + ip + ">";
    }

    @Override
    public void run() {
        try {
            while (run) {
                // Try to connect the socket, or wait until it becomes connected.
                while (socket == null || !socket.isConnected()) {
                    Thread.sleep(RECONNECT_TIMEOUT);
                    connect();
                }

                Tunneling.getLogger().debug(toString() + String.format(" socket:<bound: %b; connected: %b>", socket.isBound(), socket.isConnected()));

                // Start reader and writer.
                reader = new TunnelReader(this, socket.getInputStream());
                writer = new TunnelWriter(this, socket.getOutputStream());
                reader.start();
                writer.start();

                // Wait for interrupts, join threads.
                reader.join();
                writer.join();

                socket.close();
                Tunneling.getLogger().warning(toString() + " closed.");
            }
        } catch (IOException e) {
            run = false;
            Tunneling.getLogger().error(toString() + " cannot initialize the reader and/or writer.");
        } catch (InterruptedException e) {
        }
    }

    public void start() {
        run = true;
        t.start();
        Tunneling.getLogger().debug(toString() + " started.");
    }

    public void stop() {
        run = false;
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        Tunneling.getLogger().debug(toString() + " stopped.");
    }

    public boolean isAlive() {
        return t.isAlive();
    }

    /**
     * Reader part of the tunnel.
     */
    private class TunnelReader implements Runnable {
        private Tunnel tunnel;
        private InputStream stream;
        private Thread t;
        boolean run;

        public TunnelReader(Tunnel tunnel, InputStream in) {
            this.tunnel = tunnel;
            stream = in;
            t = new Thread(this);
            t.setName(toString());
        }

        @Override
        public void run() {
            while (run) {
                try {
                    // Read the header data of the next packet.
                    byte[] rawHeader = new byte[Packet.HEADER_LENGTH];
                    ByteStreams.readFully(stream, rawHeader);

                    for (byte b : rawHeader) {
                        System.out.print(b);
                    }
                    System.out.println();

                    // Parse the header.
                    PacketHeader header = Packet.parseHeader(rawHeader);

                    // Fetch the payload.
                    byte[] rawPayload = new byte[header.getLength()];

                    // Build packet.
                    Packet p = new Packet(Bytes.concat(rawHeader, rawPayload));

                    // Verify packet.
                    if (p.verify()) {
                        // Add packet to queue.
                        tunnel.in.add(p);
                    } else {
                        // We are out of sync, stop.
                        Tunneling.getLogger().error(tunnel.toString() + " received invalid packet, tunnel may be out of sync.");
                        run = false;
                    }
                } catch (IOException e) {
                    // Connection closed, stop.
                    Tunneling.getLogger().warning(tunnel.toString() + " closed.");
                    run = false;
                }
            }
            tunnel.writer.interrupt();
        }

        public void start() {
            run = true;
            t.start();
            Tunneling.getLogger().debug(toString() + " started.");
        }

        public void join() {
            run = false;
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }

        public void interrupt() {
            t.interrupt();
        }

        public String toString() {
            return "TunnelReader<" + tunnel.hashCode() + ">";
        }
    }

    /**
     * Writer part of the tunnel.
     */
    private class TunnelWriter implements Runnable {
        private Tunnel tunnel;
        private OutputStream stream;
        private Thread t;
        boolean run;

        public TunnelWriter(Tunnel tunnel, OutputStream in) {
            this.tunnel = tunnel;
            stream = in;
            t = new Thread(this);
            t.setName(toString());
        }

        @Override
        public void run() {
            while (run) {
                try {
                    // Get packet.
                    Packet p = tunnel.out.take();

                    Tunneling.getLogger().debug(p.toString() + " received by " + toString());

                    // Send packet.
                    stream.write(p.compile());
                } catch (IOException e) {
                    // Connection closed, stop.
                    Tunneling.getLogger().warning(tunnel.toString() + " closed.");
                    run = false;
                } catch (InterruptedException e) {
                    // Some other error, stop.
                    run = false;
                }
            }
            tunnel.reader.interrupt();
        }

        public void start() {
            run = true;
            t.start();
            Tunneling.getLogger().debug(toString() + " started.");
        }

        public void join() {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }

        public void interrupt() {
            t.interrupt();
        }

        public String toString() {
            return "TunnelWriter<" + tunnel.hashCode() + ">";
        }
    }
}

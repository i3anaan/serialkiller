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
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Represents a tunnel with another host.
 */
public class Tunnel implements Runnable {
    public static final int CONNECT_TIMEOUT = 2000;
    public static final int RECONNECT_TIMEOUT = 2000;

    /** The NetworkLayer this tunnel works for. */
    private TPPNetworkLayer network;

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
     * @param network The NetworkLayer instance this tunnel works for. May be
     *                null if this tunnel works without a network layer.
     */
    private Tunnel(ArrayBlockingQueue<Packet> in, boolean autoconnect, TPPNetworkLayer network) {
        this.autoconnect = autoconnect;
        this.in = in;
        this.out = new ArrayBlockingQueue<Packet>(TPPNetworkLayer.QUEUE_SIZE);
        this.network = network;
    }

    /**
     * Constructs a new Tunnel instance based on an existing socket. This socket
     * will be used for communication.
     * @param socket The socket.
     * @param in The queue for packets traveling into the network layer.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     * @param network The NetworkLayer instance this tunnel works for. May be
     *                null if this tunnel works without a network layer.
     */
    public Tunnel(Socket socket, ArrayBlockingQueue<Packet> in, boolean autoconnect, TPPNetworkLayer network) {
        this(in, autoconnect, network);
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
     * @param network The NetworkLayer instance this tunnel works for. May be
     *                null if this tunnel works without a network layer.
     */
    public Tunnel(String ip, ArrayBlockingQueue<Packet> in, boolean autoconnect, TPPNetworkLayer network) {
        this(in, autoconnect, network);
        this.ip = ip;

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
        if (socket != null && socket.isConnected()) {
            if (!out.offer(p)) {
                Tunneling.getLogger().alert(p.toString() + " dropped, " + toString() + " queue full.");
                if (network != null) {
                    network.markAsDropped(p);
                }
            }
        } else {
            Tunneling.getLogger().alert(p.toString() + " dropped, " + toString() + " is not connected.");
            // Do not mark as dropped, retransmission should occur.
        }
    }

    /**
     * Connects this tunnel. Creates a new socket if this tunnel does not have
     * a socket or if the socket is not connected and autoconnect() is true.
     * @return Whether the connection is successful.
     */
    public boolean connect() {
        return connect(false);
    }

    public boolean connect(boolean force) {
        boolean success = true;

        Tunneling.getLogger().info("Connecting " + toString() + ".");

        if (socket == null || force) {
            try {
                resetSocket();
            } catch (IOException e) {
                Tunneling.getLogger().warning("Unable to set up " + toString() + " (" + e.getMessage() + ").");
                success = false;
            }
        }

        if (socket != null && !socket.isConnected() && autoconnect) {
            try {
                socket.connect(new InetSocketAddress(ip, Tunneling.PORT), CONNECT_TIMEOUT);
            } catch (IOException e) {
                Tunneling.getLogger().warning("Unable to connect " + toString() + " (" + e.getMessage() + ").");
                success = false;
            }
        }

        if (success) {
            Tunneling.getLogger().info(toString() + " connected.");
        }

        return success;
    }

    private void resetSocket() throws IOException {
        if (socket != null && socket.isConnected()) {
            socket.close();
        }

        socket = new Socket(ip, Tunneling.PORT);
    }

    @Override
	public String toString() {
        return "Tunnel<" + ip + "; auto:" + String.valueOf(autoconnect) + ">";
    }

    @Override
    public void run() {
        while (run) {
            try {
                // Try to connect the socket, or wait until it becomes connected.
                while (run && autoconnect && (socket == null || !socket.isConnected() || socket.isClosed())) {
                    Thread.sleep(RECONNECT_TIMEOUT);
                    connect(true);
                }

                if (run && socket.isConnected()) {
                    // Start reader and writer.
                    reader = new TunnelReader(this, socket.getInputStream());
                    writer = new TunnelWriter(this, socket.getOutputStream());
                    reader.start();
                    writer.start();

                    // Wait for interrupts, join threads.
                    reader.join();
                    writer.join();

                    socket.close();
                    Tunneling.getLogger().warning(toString() + " socket closed.");

                    run = autoconnect;
                }
            } catch (IOException e) {
                run = autoconnect;
            } catch (InterruptedException e) {
                run = false;
            }
        }

        Tunneling.getLogger().warning(toString() + " stopped.");
    }

    public void start() {
        t = new Thread(this);
        t.setName(toString());
        run = true;
        t.start();
        Tunneling.getLogger().info(toString() + " started.");
    }

    public void stop() {
        run = false;
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        Tunneling.getLogger().info(toString() + " stopped.");
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
            this.t = new Thread(this);
            this.t.setName(toString());
        }

        @Override
        public void run() {
            while (run) {
                try {
                    // Read the header data of the next packet.
                    byte[] rawHeader = new byte[Packet.HEADER_LENGTH];
                    ByteStreams.readFully(stream, rawHeader);

                    // Parse the header.
                    PacketHeader header = Packet.parseHeader(rawHeader);

                    // Fetch the payload.
                    byte[] rawPayload = new byte[header.getLength()];
                    ByteStreams.readFully(stream, rawPayload);

                    // Build packet.
                    Packet p = new Packet(Bytes.concat(rawHeader, rawPayload));

                    // Verify packet.
                    if (p.verify()) {
                        // Add packet to queue.
                        if (!tunnel.in.offer(p)) {
                            Tunneling.getLogger().error(p.toString() + " dropped, queue full.");
                            if (network != null) {
                                network.markAsDropped(p);
                            }
                        }
                    } else if (p.reason() != null) {
                        // We are out of sync, stop.
                        Tunneling.getLogger().alert(tunnel.toString() + " received invalid packet (" + p.reason().toString() + ").");

                        if (p.reason() == Packet.RejectReason.LENGTH) {
                            Tunneling.getLogger().info("Restarting " + tunnel.toString() + ", tunnel may be out of sync.");
                            run = false;
                        }
                    } else {
                        Tunneling.getLogger().alert(tunnel.toString() + " received invalid packet (unknown error).");
                    }
                } catch (IOException e) {
                    // Connection closed, stop.
                    Tunneling.getLogger().warning(tunnel.toString() + " closed (" + e.getMessage() + ").");
                    run = false;
                }
            }
            tunnel.writer.interrupt();
        }

        public void start() {
            this.t = new Thread(this);
            this.t.setName(toString());
            run = true;
            this.t.start();
            Tunneling.getLogger().info(toString() + " started.");
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

        @Override
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
            this.t = new Thread(this);
            this.t.setName(toString());
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
                    stream.flush();
                } catch (IOException e) {
                    // Connection closed, stop.
                    Tunneling.getLogger().warning(tunnel.toString() + " closed (" + e.getMessage() + ").");
                    run = false;
                } catch (InterruptedException e) {
                    // Some other error, stop.
                    run = false;
                }
            }
            tunnel.reader.interrupt();
        }

        public void start() {
            this.t = new Thread(this);
            this.t.setName(toString());
            run = true;
            this.t.start();
            Tunneling.getLogger().info(toString() + " started.");
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

        @Override
		public String toString() {
            return "TunnelWriter<" + tunnel.hashCode() + ">";
        }
    }
}

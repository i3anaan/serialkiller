package network;

import common.Layer;
import link.FrameLinkLayer;
import network.handlers.Handler;
import network.handlers.LinkLayerInHandler;
import network.handlers.LinkLayerOutHandler;
import network.handlers.RetransmissionHandler;

import javax.naming.SizeLimitExceededException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The main class for the network layer.
 *
 * This class runs a thread that monitors packets in a queue and sends each
 * packet to the relevant handler (see handlers.Handler or Router for more
 * information).
 *
 * This class also has methods for sending data and tracks sent packets.
 */
public class NetworkLayer extends Layer implements Runnable {
    public static final long TIMEOUT = 10000; // in milliseconds
    public final byte ADDRESS_SELF;
    public final byte ADDRESS_SIBLING;
    public static final String ROUTING_PATH = System.getProperty("user.home") + "/serialkiller/routes.txt"; // TODO: Move to configuration file

    /** Size of the queue. */
    public static final int QUEUE_SIZE = 64;

    /** The thread for this instance. */
    private Thread t;

    /** The collection of handlers used for connecting with underlying layers. */
    private Collection<Handler> handlers;

    /** The retransmission handler. */
    private Handler retransmissionHandler;

    /** The link layer that is used. */
    private FrameLinkLayer link;

    /** The router for this network. */
    private Router router;

    /** A lock for the router. */
    private Lock routerLock;

    /** The router queue. */
    protected ArrayBlockingQueue<Packet> queue;

    /** The sent and to be acknowledged packets. */
    private Collection<Packet> sent;

    long ackTimeout;

    /** The next available sequence number. */
    private int seqnum;

    /**
     * Constructs a new NetworkLayer instance.
     */
    public NetworkLayer(FrameLinkLayer link, byte address, byte sibling) {
        ADDRESS_SELF = address;
        ADDRESS_SIBLING = sibling;

        this.link = link;

        handlers = new ArrayList<Handler>();

        queue = new ArrayBlockingQueue<Packet>(QUEUE_SIZE, true);
        sent = new ArrayList<Packet>();

        router = new Router();
        routerLock = new ReentrantLock(true);

        ackTimeout = System.currentTimeMillis();

        // Load routes
        loadDefaultRoutes();

        // Construct and run thread
        t = new Thread(this);
        t.setName("TPP " + this.hashCode());
    }

    /**
     * Returns the incoming queue of the network layer.
     * @return The incoming queue.
     */
    public ArrayBlockingQueue<Packet> queue() {
        return queue;
    }

    /**
     * Sets the active sequence number to the next value and returns this new
     * value.
     * @return The new sequence number.
     */
    private int nextSeqnum() {
        seqnum = (seqnum + 1) % (Packet.MAX_SEQNUM + 1);
        return seqnum;
    }

    /**
     * Send data using this NetworkLayer. The data may be split into segments
     * and being reassembled at the other end.
     * @param data The data.
     */
    public void send(byte[] data, byte destination) throws SizeLimitExceededException {
        int segments = (int) Math.ceil((double) data.length / Packet.MAX_PAYLOAD_LENGTH);

        if (segments == 1) {
            // Send a single packet.
            Packet p = new Packet(nextSeqnum());
            p.header().setSender(ADDRESS_SELF);
            p.header().setDestination(destination);
            p.setPayload(data);

            sendPacket(p);
        } else if (segments <= PacketHeader.MAX_SEGNUM) {
            int seqnum = nextSeqnum();

            // Send each packet individually
            for (int i = 0; i < segments; i++) {
                Packet p = new Packet(seqnum);
                p.header().setSegnum(i);
                p.header().setSender(ADDRESS_SELF);
                p.header().setDestination(destination);
                p.setPayload(Arrays.copyOfRange(data, Packet.MAX_PAYLOAD_LENGTH * i, Math.min(Packet.MAX_PAYLOAD_LENGTH * (i+1), segments)));

                sendPacket(p);
            }
        } else {
            // TODO: Log
            throw new SizeLimitExceededException("The data is too long for the packets.");
        }
    }

    /**
     * Sends the given packet.
     * @param p The packet.
     */
    private void sendPacket(Packet p) {
        routerLock.lock();

        Host host = router.route(p);

        if (host != null && host.handler() != null) {
            host.handler().offer(p);

            p.timestamp(System.currentTimeMillis());
            sent.add(p);
        } else {
            // TODO: Log
        }

        routerLock.unlock();
    }

    /**
     * Sends retransmissions to the RetransmissionHandler.
     */
    public void checkRetransmissions() {
        for (Packet p : sent) {
            if (p.timestamp() + TIMEOUT < System.currentTimeMillis()) {
                sent.remove(p);
                retransmissionHandler.offer(p);
            }
        }
    }

    /**
     * Marks the given packet as sent.
     * @param p The packet.
     */
    public void markSent(Packet p) {
        p.timestamp(System.currentTimeMillis());
        sent.add(p);
    }

    @Override
    public void run() {
        // Run handlers
        startHandlers();

        boolean run = true;

        // Route packets in the queue to their handlers.
        while (run) {
            try {
                Packet p = queue.take();
                p.header().decreaseTTL(); // Decrease the TTL for this hop.

                // Check if this is an acknowledgement or should be acknowledged.
                if (p.header().getAck()) {
                    sent.remove(p.header().getAcknum());
                    return; // We are done.
                } else if (p.header().getDestination() == ADDRESS_SELF) {
                    // Send acknowledgement if we are the final destination.
                    queue.offer(p.createAcknowledgement(nextSeqnum()));
                }

                // Route packet (lock the router during this operation)
                routerLock.lock();
                Host linkHost = router.route(p);

                if (linkHost != null && linkHost.handler() != null) {
                    // Push packet to handler.
                    linkHost.handler().offer(p);
                } else {
                    // TODO: Log unroutable packet
                }

                routerLock.unlock();
            } catch (InterruptedException e) {
                // Exit gracefully
                run = false;
            }
        }

        // Destroy handlers.
        stopHandlers();
    }

    /**
     * Constructs handlers and connects them to the hosts based on the entries
     * in the router.
     *
     * Stops and restarts the handlers if the NetworkLayer thread is running.
     */
    private void constructHandlers() {
        assert (router != null);

        // Stop existing handlers (only if the network layer is running).
        if (t.isAlive()) {
            for (Handler h : handlers) {
                stopHandlers();
            }
        }

        // Check all hosts for a possible handler.
        for (Host host : router.hosts()) {
            if (host.address() == ADDRESS_SELF) {
                // TODO: Construct & connect application layer handler
            } else if (host.address() == ADDRESS_SIBLING) {
                // Construct LinkLayer handlers.
                Handler in = new LinkLayerInHandler(this, link);
                Handler out = new LinkLayerOutHandler(this, link);

                // Add handlers to collection.
                handlers.add(in);
                handlers.add(out);

                // Connect handler to host.
                host.handler(out);
            } else if (host.IP() != null && !host.IP().equals("")) {
                // TODO: Construct & connect tunnel handler
            }
        }

        // Add retransmission handler
        retransmissionHandler = new RetransmissionHandler(this);
        handlers.add(retransmissionHandler);

        // Start new handlers (only if the network layer is running).
        if (t.isAlive()) {
            startHandlers();
        }
    }

    /**
     * Constructs routes for a given router and sets the given router as router
     * for this NetworkLayer instance.
     * @param r The router.
     * @param filePath The file containing routes.
     */
    private void constructRoutes(Router r, String filePath) {
        router = r;

        RoutingTable routes;

        try {
            routes = new RoutingTable(filePath);
        } catch (IOException e) {
            routes = new RoutingTable();
        }

        r.parse(routes);
        r.update();
    }

    /**
     * Loads the default routes file into the router. All existing data in the
     * router will be lost.
     */
    public void loadDefaultRoutes() {
        // Lock router
        routerLock.lock();

        // Create new routes
        constructRoutes(new Router(), ROUTING_PATH);
        constructHandlers();

        // Free router
        routerLock.unlock();
    }

    /**
     * Patches the routes in the router with the routes in the specified file.
     * Duplicate entries will be overwritten.
     *
     * This method should only be used to add extra routes to the router, not
     * to (re)load the default file. This last use is not just discouraged but
     * strictly forbidden and can lead to unroutable packets and invalid routes.
     * @param filePath The routes file.
     */
    public void patchRoutes(String filePath) {
        constructRoutes(router, filePath);
        constructHandlers();
    }

    /**
     * Starts the network layer.
     */
    public void start() {
        t.start();
    }

    /**
     * Starts all handlers.
     */
    private void startHandlers() {
        for (Handler h : handlers) {
            h.start();
        }
    }

    /**
     * Stops all handlers.
     */
    private void stopHandlers() {
        for (Handler h : handlers) {
            h.stop();
        }
    }

}

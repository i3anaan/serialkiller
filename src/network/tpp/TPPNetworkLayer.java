package network.tpp;

import common.*;
import common.Stack;
import link.FrameLinkLayer;
import log.LogMessage;
import log.Logger;
import network.NetworkLayer;
import network.Payload;
import network.tpp.handlers.*;
import tunnel.Tunneling;

import javax.naming.SizeLimitExceededException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
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
public class TPPNetworkLayer extends NetworkLayer implements Runnable {
    public static final long TIMEOUT = 10000; // in milliseconds
    public static final int MAX_RETRANSMISSIONS = 3; // 0 for no maximum
    public static final int MAX_FOR_HOST = 1; // 0 for no maximum
    public static final String ROUTING_PATH = System.getProperty("user.home") + "/serialkiller/routes.txt"; // TODO: Move to configuration file

    /** Size of the queue. */
    public static final int QUEUE_SIZE = 64;

    /** The logger. */
    private static Logger logger;

    /** The thread for this instance. */
    private Thread t;

    /** The collection of handlers used for connecting with underlying layers. */
    private Collection<Handler> handlers;

    /** The retransmission handler. */
    private Handler retransmissionHandler;

    /** The reoffering handler. */
    private Handler reofferHandler;

    /** The tunneling handler. */
    private Handler tunnelingHandler;

    /** The link layer that is used. */
    private FrameLinkLayer link;

    /** The tunneling class instance. */
    private Tunneling tunnels;

    /** The router for this network. */
    private Router router;

    /** A lock for the router. */
    private Lock routerLock;

    private Lock sentLock;

    /** The router queue. */
    protected ArrayBlockingQueue<Packet> queue;

    /** The queue for the application layer. */
    private ArrayBlockingQueue<Payload> appQueue;

    /** The sent and to be acknowledged packets. */
    private Map<Integer, HashMap<Integer, Packet>> sent;

    /** The next available sequence number. */
    private int seqnum;

    private Stack stack;

    private Map<Byte, Integer> inRoute;

    /**
     * Constructs a new NetworkLayer instance.
     */
    public TPPNetworkLayer() {
        handlers = new ArrayList<Handler>();

        queue = new ArrayBlockingQueue<Packet>(QUEUE_SIZE, true);
        appQueue = new ArrayBlockingQueue<Payload>(QUEUE_SIZE, true);
        sent = new ConcurrentHashMap<Integer, HashMap<Integer, Packet>>();
        inRoute = new ConcurrentHashMap<Byte, Integer>();

        router = new Router();

        routerLock = new ReentrantLock(true);
        sentLock = new ReentrantLock(true);

        this.tunnels = new Tunneling(this);

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
        TPPNetworkLayer.getLogger().debug(String.format("Current sequence number set to %d.", seqnum));
        return seqnum;
    }

    /**
     * Returns the next available payload. This is a proxy method for
     * ArrayBlockingQueue.
     * @return The next available payload.
     */
    @Override
    public Payload read() {
        try {
            return appQueue.take();
        } catch (InterruptedException e) {
            return new Payload(new byte[0], (byte) 0);
        }
    }

    @Override
    public void send(Payload payload) throws SizeLimitExceededException {
        send(payload.data, payload.address);
    }

    /**
     * Send data using this NetworkLayer. The data may be split into segments
     * and being reassembled at the other end.
     * @param data The data.
     */
    public void send(byte[] data, byte destination) throws SizeLimitExceededException {
        int segments = (int) Math.ceil((double) data.length / Packet.MAX_PAYLOAD_LENGTH);
        TPPNetworkLayer.getLogger().debug(String.format("Data received for %d segments.", segments));

        if (segments <= PacketHeader.MAX_SEGNUM) {
            int seqnum = nextSeqnum();

            // For each segment.
            for (int i = 0; i < segments; i++) {
                // Build the packet.
                Packet p = new Packet(seqnum);
                p.header().setSegnum(i);
                p.header().setMore(i + 1 != segments);
                p.header().setSender(router.self());
                p.header().setDestination(destination);
                p.setPayload(Arrays.copyOfRange(data, Packet.MAX_PAYLOAD_LENGTH * i, Math.min(Packet.MAX_PAYLOAD_LENGTH * (i+1), data.length - (Packet.MAX_PAYLOAD_LENGTH * i))));

                // Send it.
                try {
                    queue.put(p);
                } catch (InterruptedException e) {
                    // Do nothing.
                }
            }
        } else {
            TPPNetworkLayer.getLogger().warning(String.format("Tried to send more data (%s bytes) than the protocol allows (%s bytes).", data.length, Packet.MAX_PAYLOAD_LENGTH));
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
            // Decrease TTL if needed.
            if (!(p.header().getSender() == router.self())) {
                p.header().decreaseTTL();
            }

            if (!host.handler().offer(p)) {
                TPPNetworkLayer.getLogger().error(p.toString() + " dropped, NetworkLayer queue full.");
            }

            // Mark packet as sent when we are the original sender.
            if (p.header().getSender() == router.self() && p.header().getDestination() != router.self() && !p.header().getAck()) {
                markSent(p);
            }
        } else {
            TPPNetworkLayer.getLogger().error(p.toString() + " is not routeable. Packet dropped.");
        }

        routerLock.unlock();
    }

    /**
     * Sends retransmissions to the RetransmissionHandler.
     */
    public void checkRetransmissions() {
        sentLock.lock();
//        TPPNetworkLayer.getLogger().debug(String.format("Checking for retransmissions: %s not acknowledged.", sent.size()));
        for (Map<Integer, Packet> m : sent.values()) {
            for (Packet p : m.values()) {
                if (p.timestamp() + TIMEOUT < System.currentTimeMillis()) {
                    // We handled this one.
                    sent.get(p.header().getSeqnum()).remove(p.header().getSegnum());
                    if (sent.get(p.header().getSeqnum()).size() == 0) {
                        sent.remove(p.header().getSeqnum());
                    }

                    // Only retransmit if the threshold is not exceeded.
                    if (MAX_RETRANSMISSIONS > 0 && p.retransmissions() < MAX_RETRANSMISSIONS) {
                        // Offer packet to network.
                        if (!retransmissionHandler.offer(p)) {
                            // Keep in sent list and delay retransmission, queue full.
                            if (!sent.containsKey(p.header().getSeqnum())) {
                                sent.put(p.header().getSeqnum(), new HashMap<Integer, Packet>());
                            }
                            sent.get(p.header().getSeqnum()).put(p.header().getSegnum(), p);
                            TPPNetworkLayer.getLogger().debug(p.toString() + " is delayed for retransmission, NetworkLayer queue full.");
                        } else {
                            p.retransmit(); // Mark packet as retransmitted once again.
                            TPPNetworkLayer.getLogger().debug(p.toString() + " offered for retransmission.");
                        }
                    } else {
                        TPPNetworkLayer.getLogger().debug(p.toString() + String.format(" dropped, %d retransmissions failed.", p.retransmissions()));
                    }
                }
            }
        }
//        TPPNetworkLayer.getLogger().debug(String.format("Checked for retransmissions: %s not acknowledged.", sent.size()));
        sentLock.unlock();
    }

    /**
     * Marks the given packet as sent.
     * @param p The packet.
     */
    public void markSent(Packet p) {
        sentLock.lock();
        p.timestamp(System.currentTimeMillis());
        if (!sent.containsKey(p.header().getSeqnum())) {
            sent.put(p.header().getSeqnum(), new HashMap<Integer, Packet>());
        }
        sent.get(p.header().getSeqnum()).put(p.header().getSegnum(), p);
        if (!inRoute.containsKey(p.header().getDestination())) {
            inRoute.put(p.header().getDestination(), 0);
        }
        inRoute.put(p.header().getDestination(), inRoute.get(p.header().getDestination()) + 1);
        TPPNetworkLayer.getLogger().debug(p.toString() + " marked as sent.");
        sentLock.unlock();
    }

    /**
     * Returns a collection of all addresses known to the router.
     * @return The collection of all known addresses.
     */
    public Collection<Byte> hosts() {
        Collection<Byte> hosts = new ArrayList<Byte>();

        for (Host h : router.hosts()) {
            hosts.add(h.address());
        }

        return hosts;
    }

    /**
     * Returns the address of this host.
     * @return The address of this host.
     */
    public Byte host() {
        return router.self();
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

                // Check if this is an acknowledgement or should be acknowledged.
                if (p.header().getAck() && p.header().getDestination() == router.self()) {
                    sentLock.lock();
                    sent.get(p.header().getAcknum()).remove(p.header().getSegnum());

                    if (!inRoute.containsKey(p.header().getSender())) {
                        inRoute.put(p.header().getSender(), 0);
                    }
                    inRoute.put(p.header().getSender(), Math.max(inRoute.get(p.header().getSender()) - 1, 0));

                    sentLock.unlock();
                    TPPNetworkLayer.getLogger().debug("Received acknowledgement: " + p.toString() + ".");
                } else if (p.header().getDestination() == router.self()) {
                    // Send acknowledgement if we are the final destination.
                    Packet ack = p.createAcknowledgement(nextSeqnum());

                    if (!queue.offer(ack)) {
                        TPPNetworkLayer.getLogger().warning(p.toString() + " dropped, network layer queue full.");
                    } else {
                        TPPNetworkLayer.getLogger().debug("Sent acknowledgement for " + p.toString() + ": " + ack.toString() + ".");
                    }

                    // Only send the packet when the payload is not empty.
                    if (p.payload() != null && p.payload().length > 0) {
                        sendPacket(p);
                    }
                } else {
                    if (!inRoute.containsKey(p.header().getDestination())) {
                        inRoute.put(p.header().getDestination(), 0);
                    }

                    if (inRoute.get(p.header().getDestination()) < MAX_FOR_HOST) {
                        sendPacket(p);
                    } else {
                        if (reofferHandler.offer(p)) {
                            TPPNetworkLayer.getLogger().debug(p.toString() + String.format(" re-added to queue, limit of %d packets exceeded.", MAX_FOR_HOST));
                        } else {
                            TPPNetworkLayer.getLogger().warning(p.toString() + String.format(" dropped, reoffer queue full."));
                        }
                    }
                }
            } catch (InterruptedException e) {
                // Exit gracefully.
                run = false;
            }
        }

        TPPNetworkLayer.getLogger().warning("NetworkLayer stopped.");

        // Destroy handlers.
        stopHandlers();

        if (stack != null) {
            // Force stack rebuild.
            stack.smash();
        }
    }

    /**
     * Constructs handlers and connects them to the hosts based on the entries
     * in the router.
     *
     * Stops and restarts the handlers if the NetworkLayer thread is running.
     */
    private void constructHandlers() {
        assert (router != null);
        assert (tunnels != null);

        // Stop existing handlers (only if the network layer is running).
        if (t.isAlive()) {
            stopHandlers();
        }

        // Add retransmission handler
        handlers.remove(retransmissionHandler);
        retransmissionHandler = new RetransmissionHandler(this);
        handlers.add(retransmissionHandler);

        // Add reoffering handler
        handlers.remove(reofferHandler);
        reofferHandler = new ReofferHandler(this);
        handlers.add(reofferHandler);

        // Add tunneling handler
        handlers.remove(tunnelingHandler);
        tunnelingHandler = new TunnelingHandler(this, tunnels, router);
        handlers.add(tunnelingHandler);

        // Check all hosts for a possible handler.
        for (Host host : router.hosts()) {
            if (host.address() == router.self()) {
                // Construct ApplicationLayer handler.
                Handler h = new ApplicationLayerHandler(this, appQueue);

                // Add handler to collection.
                handlers.add(h);

                // Connect handler to host.
                host.handler(h);
            } else if (host.address() == router.sibling()) {
                // Construct LinkLayer handlers.
                Handler in = new LinkLayerInHandler(this, link);
                Handler out = new LinkLayerOutHandler(this, link);

                // Add handlers to collection.
                handlers.add(in);
                handlers.add(out);

                // Connect handler to host.
                host.handler(out);
            } else if (host.IP() != null && !host.IP().equals("")) {
                // Connect tunnelingHandler to host.
                host.handler(tunnelingHandler);
            }

            if (host.handler() != null) {
                TPPNetworkLayer.getLogger().debug(host.toString() + " connected with " + host.handler().toString() + ".");
            } else {
                TPPNetworkLayer.getLogger().debug(host.toString() + " not connected with any handler.");
            }
        }

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

        routes = new RoutingTable(filePath);

        r.parse(routes);
        r.update();

        // Update tunnels
        tunnels.clear();

        for (Byte addr : routes.getTunnels().keySet()) {
            tunnels.create(routes.getTunnels().get(addr), addr < r.self());
        }

        TPPNetworkLayer.getLogger().warning("Routes updated.");
    }

    /**
     * Loads the default routes file into the router. All existing data in the
     * router will be lost.
     */
    public void loadDefaultRoutes() {
        TPPNetworkLayer.getLogger().info("Reloading routes from file (" + ROUTING_PATH + ").");

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
     *
     * The results of this method are not guaranteed, this method is seen as an
     * expirimental feature.
     * @param filePath The routes file.
     */
    public void patchRoutes(String filePath) {
        TPPNetworkLayer.getLogger().info("Patching routes from file (" + filePath + ").");

        // Lock router
        routerLock.lock();

        // Add new routes
        constructRoutes(router, filePath);
        constructHandlers();

        // Free router
        routerLock.unlock();
    }

    /**
     * Starts the network layer.
     */
    public void start() {
        assert (link != null);
        t.start();
        TPPNetworkLayer.getLogger().info("NetworkLayer started.");
    }

    /**
     * Starts all handlers.
     */
    private void startHandlers() {
        for (Handler h : handlers) {
            h.start();
        }
        tunnels.start();
    }

    /**
     * Stops all handlers.
     */
    private void stopHandlers() {
        tunnels.stop();
        for (Handler h : handlers) {
            h.stop();
        }
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger(LogMessage.Subsystem.NETWORK);
        }
        return logger;
    }

    @Override
    public Thread start(Stack stack) {
        // Check if the link layer of the stack is compatible with this network layer implementation.
        assert (stack.linkLayer instanceof FrameLinkLayer);
        this.stack = stack;

        // Assign the link layer.
        this.link = (FrameLinkLayer) stack.linkLayer;

        // Load routes
        loadDefaultRoutes();

        // Run the thread(s).
        this.start();

        // Return the thread.
        return this.t;
    }
}

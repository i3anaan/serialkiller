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
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Semaphore;
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
    /** Retransmission timeout in milliseconds. */
    public static final long TIMEOUT = 5000; // Protocol: no spec

    /** Maximum number of retransmissions until a packet is dropped. */
    public static final int MAX_RETRANSMISSIONS = 3; // Protocol: 3

    /** The maximum amount of packets that should be in the network for a certain host. Used as a congestion control mechanism. */
    public static final int MAX_FOR_HOST = 1; // Protocol: 1

    /** The path where the routes file should be located. */
    public static final String ROUTING_PATH = System.getProperty("user.home") + "/serialkiller/routes.txt";

    /** Size of the queue. */
    public static final int QUEUE_SIZE = 64;

    /** The logger for the network layer. */
    private static Logger logger;

    /** The thread for this instance. */
    private Thread t;

    /** The stack. */
    private Stack stack;

    /** The (framed) link layer that is used. */
    private FrameLinkLayer link;

    /** The router for this network. */
    private Router router;

    /** The tunneling instance. */
    private Tunneling tunnels;

    /** The collection of handlers used for connecting with underlying layers. */
    private Collection<Handler> handlers;

    /** Pointers to individual handlers that are very specific. */
    private RetransmissionHandler retransmissionHandler; // Handler for retransmissions
    private ReofferHandler reofferHandler; // Handler for reoffering
    private TunnelingHandler tunnelingHandler; // Handler for tunneling

    /** The network layer queue: contains packets that have to be handled by the network layer. */
    protected ArrayBlockingQueue<Packet> queue;

    /** The application layer queue: contains packets that are to be sent to the application layer. */
    private ArrayBlockingQueue<Payload> appQueue;

    /** The retransmission queue: contains packets that are to be retransmitted. Releases packets after a specified amount of time. */
    private DelayQueue<Packet> retransmissionQueue;

    /** Retransmission list, maps the identifier of a packet to packets. */
    private ConcurrentHashMap<String, Packet> sentPackets;

    /** Congestion control administration: contains the number of packets in the network for a certain TPP address. */
    private ConcurrentHashMap<Byte, Integer> congestion;

    /** The next available sequence number. */
    private int seqnum;

    /**
     * Marks the specified packet as sent. Adds the packet to the retransmission
     * queue with the default delay.
     * @param p The packet.
     */
    private void markAsSent(Packet p) {
        p.delay(TIMEOUT);
        retransmissionQueue.offer(p);
        sentPackets.put(p.id(), p);
        increaseCongestion(p.header().getDestination());
    }

    /**
     * Marks the specified packet as acknowledged. Removes the packet from the
     * retransmission queue and sets the delay to zero.
     * @param p The packet.
     */
    private void markAsAcknowledged(Packet p) {
        // Remove packet from retransmission queue.
        if (retransmissionQueue.remove(p)) {
            // If removed, set the delay to 0. If not removed, keep delay.
            p.delay(0);
        }
        sentPackets.remove(p.id());
        decreaseCongestion(p.header().getDestination());
    }

    /**
     * Marks the packet with the specified id as acknowledged. Removes the
     * packet from the retransmission queue and sets the delay to zero.
     * @param id The id of the packet.
     */
    private void markAsAcknowledged(String id) {
        if (sentPackets.containsKey(id)) {
            markAsAcknowledged(sentPackets.get(id));
        }
    }

    /**
     * Marks a packet as dropped.
     * @param p The packet.
     */
    public void markAsDropped(Packet p) {
        // Remove packet from retransmission queue.
        if (retransmissionQueue.remove(p)) {
            // If removed, set the delay to 0. If not removed, keep delay.
            p.delay(0);
        }
        sentPackets.remove(p.id());
        decreaseCongestion(p.header().getDestination());
    }

    /**
     * Increases the network congestion for a host.
     * @param address The address of the host.
     */
    private void increaseCongestion(Byte address) {
        // Create entry if not exists.
        if (!congestion.containsKey(address)) {
            congestion.put(address, 1);
        } else {
            congestion.put(address, congestion.get(address) + 1);
        }
        TPPNetworkLayer.getLogger().debug(String.format("Congestion for %d increased to %d", address, congestion.get(address)));
    }

    /**
     * Decreases the network congestion for a host to a minimum of 0.
     * @param address The address of the host.
     */
    private void decreaseCongestion(Byte address) {
        // Create entry if not exists.
        if (!congestion.containsKey(address)) {
            congestion.put(address, 0);
        } else {
            congestion.put(address, Math.max(congestion.get(address) - 1, 0));
        }
        TPPNetworkLayer.getLogger().debug(String.format("Congestion for %d decreased to %d", address, congestion.get(address)));
    }

    /**
     * Returns the congestion (number of packets in the network) for the
     * specified host.
     * @param address The address of the host.
     * @return The congestion for the specified host.
     */
    public int getCongestion(Byte address) {
        return congestion.get(address) != null ? congestion.get(address) : 0;
    }

    /**
     * Constructs a new NetworkLayer instance.
     */
    public TPPNetworkLayer() {
        handlers = new ArrayList<Handler>();

        queue = new ArrayBlockingQueue<Packet>(QUEUE_SIZE, true);
        appQueue = new ArrayBlockingQueue<Payload>(QUEUE_SIZE, true);
        retransmissionQueue = new DelayQueue<Packet>();

        sentPackets = new ConcurrentHashMap<String, Packet>();

        congestion = new ConcurrentHashMap<Byte, Integer>();

        router = new Router();
        tunnels = new Tunneling(this);

        // Construct and run thread
        t = new Thread(this);
        t.setName("TPP " + this.hashCode());
    }

    /**
     * Returns the queue of the network layer. Elements in this queue will be
     * handled by the network layer.
     * @return The network layer queue.
     */
    public ArrayBlockingQueue<Packet> queue() {
        return queue;
    }

    /**
     * Returns the queue for retransmissions. Only the retransmission handler
     * should be removing elements from this queue.
     * @return The retransmission queue.
     */
    public DelayQueue<Packet> retransmissionQueue() {
        return retransmissionQueue;
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
            return new Payload(new byte[0], router.self());
        }
    }

    /**
     * Send data using this NetworkLayer. The data may be split into segments
     * and being reassembled at the other end.
     * @param payload The payload.
     * @throws SizeLimitExceededException The data is too long.
     */
    @Override
    public void send(Payload payload) throws SizeLimitExceededException {
        send(payload.data, payload.address);
    }

    /**
     * Send data using this NetworkLayer. The data may be split into segments
     * and being reassembled at the other end.
     * @param data The data.
     * @throws SizeLimitExceededException The data is too long.
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
                int end = Packet.MAX_PAYLOAD_LENGTH * (i+1) > data.length ? data.length : Packet.MAX_PAYLOAD_LENGTH * (i+1);
                p.setPayload(Arrays.copyOfRange(data, Packet.MAX_PAYLOAD_LENGTH * i, end));

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

    /**
     * Sends the given packet.
     * @param p The packet.
     */
    public void sendPacket(Packet p) {
        Host host = router.route(p);

        if (host != null && host.handler() != null) {
            if (p.header().getDestination() == router.self() // Packets to ourselves do not get congestion control.
                    || (p.header().getSender() == router.self() && p.header().getAck()) // Packets that are acknowledgements do not get congestion control.
                    || (p.header().getSender() == router.self() && getCongestion(p.header().getDestination()) < MAX_FOR_HOST)) { // Packets for hosts that are congested by us can be sent.
                // Decrease TTL if needed.
                if (p.header().getSender() != router.self()) {
                    p.header().decreaseTTL();
                }

                // Offer packet to handler.
                if (!host.handler().offer(p)) {
                    TPPNetworkLayer.getLogger().error(p.toString() + " dropped, handler queue full.");
                    return; // We are done.
                } else {
                    TPPNetworkLayer.getLogger().debug(p.toString() + " offered to " + host.handler().toString() + ".");
                }

                // Mark packet as sent when we are the original sender and the packet is not an acknowledgement.
                if (p.header().getSender() == router.self() && p.header().getDestination() != router.self() && !p.header().getAck()) {
                    markAsSent(p);
                }
            } else {
                // Offer again.
                reofferHandler.offer(p);
                TPPNetworkLayer.getLogger().debug(String.format("%d congested (%d/%d in route), ", host.address(), getCongestion(host.address()), MAX_FOR_HOST) + p.toString() + " will be delayed.");
            }
        } else {
            TPPNetworkLayer.getLogger().error(p.toString() + " dropped, packet is not routable.");
        }
    }

    private void handleAcknowledgement(Packet p) {
        // Mark packet as acknowledged.
        markAsAcknowledged(Packet.id(p.header().getSender(), p.header().getAcknum(), p.header().getSegnum()));
    }

    private void handlePassthrough(Packet p) {
        // Send the packet further in the network.
        sendPacket(p);
    }

    private void handleForApplication(Packet p) {
        // Create and send acknowledgement if the packet is not originating from us.
        if (p.header().getSender() != router.self()) {
            Packet ack = p.createAcknowledgement(nextSeqnum());
            sendPacket(ack);
        } else {
            markAsAcknowledged(p);
        }

        // Send the packet further in the network.
        sendPacket(p);
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
                    // Acknowledgement for us.
                    TPPNetworkLayer.getLogger().debug("Received acknowledgement: " + p.toString() + ".");
                    handleAcknowledgement(p);
                } else if (p.header().getDestination() == router.self()) {
                    // We are the final destination.
                    TPPNetworkLayer.getLogger().debug("Received packet for application: " + p.toString() + ".");
                    handleForApplication(p);
                } else {
                    // We are merely a simple workman, bossed around and without any initiative.
                    // In other words, send the packet to another host.
                    TPPNetworkLayer.getLogger().debug("Received packet for remote host: " + p.toString() + ".");
                    handlePassthrough(p);
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

        TPPNetworkLayer.getLogger().alert("Routes updated.");
    }

    /**
     * Loads the default routes file into the router. All existing data in the
     * router will be lost.
     */
    public void loadDefaultRoutes() {
        TPPNetworkLayer.getLogger().warning("Reloading routes from file (" + ROUTING_PATH + ").");

        // Create new routes
        constructRoutes(new Router(), ROUTING_PATH);
        constructHandlers();
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

        // Add new routes
        constructRoutes(router, filePath);
        constructHandlers();
    }

    /**
     * Starts the network layer.
     */
    public void start() {
        assert (link != null);
        assert (router != null);

        t.start();
        TPPNetworkLayer.getLogger().info("NetworkLayer started.");
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
}

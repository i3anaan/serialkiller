package network;

import common.Layer;
import link.FrameLinkLayer;
import network.handlers.Handler;
import network.handlers.LinkLayerInHandler;
import network.handlers.LinkLayerOutHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The network layer.
 */
public class NetworkLayer extends Layer implements Runnable {
    public static final byte ADDRESS_SELF = 0; // TODO: Move to configuration file
    public static final byte ADDRESS_SIBLING = 0; // TODO: Move to configuration file
    public static final String ROUTING_PATH = System.getProperty("user.home") + "/serialkiller/routes.txt"; // TODO: Move to configuration file

    /** Size of the queue. */
    public static final int QUEUE_SIZE = 64;

    /** The thread for this instance. */
    private Thread t;

    /** The collection of handlers used for connecting with underlying layers. */
    private Collection<Handler> handlers;

    /** The link layer that is used. */
    private FrameLinkLayer link;

    /** The router for this network. */
    private Router router;

    /** A lock for the router. */
    private Lock routerLock;

    /** The router queue. */
    protected ArrayBlockingQueue<Packet> queue;

    private int seqnum;

    /**
     * Constructs a new NetworkLayer instance.
     */
    public NetworkLayer(FrameLinkLayer link) {
        this.link = link;

        handlers = new ArrayList<Handler>();

        queue = new ArrayBlockingQueue<Packet>(QUEUE_SIZE, true);

        router = new Router();
        routerLock = new ReentrantLock(true);

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

                // Route packet (lock the router during this operation)
                routerLock.lock();
                Host linkHost = router.route(p);

                if (linkHost != null && linkHost.handler() != null) {
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

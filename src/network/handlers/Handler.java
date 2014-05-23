package network.handlers;

import network.NetworkLayer;
import network.Packet;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Base handler class for network layer handlers.
 *
 * A handler is a thread which handles packets for the network layer. A handler
 * has two queues: one queue that is shared with the network layer, this queue
 * is used for packets that are travelling within or into the network layer, and
 * its own queue that is used for packets going out of the network layer (which
 * packets the handler needs to process). Based on this you could categorize the
 * handlers into incoming, outgoing and processing handlers. The incoming
 * handlers talk to another part of the application and put packets into the
 * 'in' queue, the outgoing handlers take packets out of the 'out' queue and
 * transmits data to other parts of the application and processing handlers
 * transfer packets from the out queue to the in queue (usually with
 * modifications).
 */
public abstract class Handler implements Runnable {
    private static final int QUEUE_SIZE = 64;
    private String name = "Handler";
    private Thread t;
    private boolean run;

    /** Queue for data travelling to the network layer. Is the same queue for
     * every handler. No data should be taken out of this queue. */
    protected ArrayBlockingQueue<Packet> in;

    /** Queue for data travelling out of the network layer. Every Handler has
     * its own queue. Writing to this queue is not recommended, but all data
     * should be taken out if a handler has a responsibility of handling
     * outgoing data. */
    protected ArrayBlockingQueue<Packet> out;

    /**
     * Handles setup for new Handler subclass instances.
     * @param parent The parent NetworkLayer instance.
     */
    public Handler(NetworkLayer parent) {
        this.in = parent.queue();
        this.out = new ArrayBlockingQueue<Packet>(QUEUE_SIZE, true);

        t = new Thread(this);
        t.setName("TPP Handler " + this.hashCode());
    }

    /**
     * Puts a packet in the outgoing queue.
     * @param p The packet.
     */
    public boolean offer(Packet p) {
        return out.offer(p);
    }

    /**
     * Method that actually handles the packets in the queues. This method gets
     * called in a loop as long as the thread runs.
     */
    public abstract void handle() throws InterruptedException;

    /**
     * Starts the handler.
     */
    public void start() {
        run = true;
        t.start();
        NetworkLayer.getLogger().alert(toString() + " started.");
    }

    /**
     * Stops the handler. Running operations will be fully executed before the
     * thread stops.
     */
    public void stop() {
        run = false;
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        NetworkLayer.getLogger().alert(toString() + " stopped.");
    }

    @Override
    public void run() {
        while (run) {
            try {
                handle();
            } catch (InterruptedException e) {
                // Exit gracefully.
                NetworkLayer.getLogger().error(toString() + " interrupted.");
                stop();
            }
        }
    }

    public String toString() {
        return String.format(name + " <" + hashCode() + ">");
    }
}

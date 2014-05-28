package network.tpp.handlers;

import network.tpp.Host;
import network.tpp.Packet;
import network.tpp.Router;
import network.tpp.TPPNetworkLayer;
import tunnel.Tunneling;

/**
 * Handler that sends packets to the tunnels.
 */
public class TunnelingHandler extends Handler {
    private Tunneling tunneling;
    private Router router;

    /**
     * Handles setup for new Handler subclass instances.
     *
     * @param parent The parent NetworkLayer instance.
     */
    public TunnelingHandler(TPPNetworkLayer parent, Tunneling tunneling, Router router) {
        super(parent);
        this.tunneling = tunneling;
        this.router = router;
    }

    @Override
    public void handle() throws InterruptedException {
        // Get packet.
        Packet p = out.take();

        // Look up route.
        Host h = router.route(p);

        if (h != null) {
            // Send to tunnel.
            tunneling.send(p, h.IP());
        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    public String toString() {
        return "Tunneling" + super.toString();
    }
}

package tunnel;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Represents a tunnel with another host.
 */
public class Tunnel {
    /** The IP address of the host this tunnel connects to. */
    private String ip;

    /** Whether this tunnel should automatically (re)connect. */
    private boolean autoconnect;

    /** The socket for the tunnel. */
    private Socket socket;

    /** Whether to continue running. */
    private boolean run;

    /**
     * Constructs a new Tunnel instance. Sets up some basic parameters.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     */
    private Tunnel(boolean autoconnect) {
        this.autoconnect = autoconnect;
    }

    /**
     * Constructs a new Tunnel instance based on an existing socket. This socket
     * will be used for communication.
     * @param socket The socket.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     */
    public Tunnel(Socket socket, boolean autoconnect) {
        this(autoconnect);
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
    }

    /**
     * Constructs a new Tunnel instance. A connection will be set up with the
     * given IP address.
     * @param ip The IP address of the remote host.
     * @param autoconnect Whether the tunnel should automatically (re)connect.
     */
    public Tunnel(String ip, boolean autoconnect) {
        this(autoconnect);
        this.ip = ip;
        this.autoconnect = false;
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
        return socket.isConnected();
    }

    /**
     * Connects this tunnel. Creates a new socket if this tunnel does not have
     * a socket or if the socket is not connected and autoconnect() is true.
     * @return Whether the connection is successful.
     */
    public boolean connect() {
        boolean success = true;

        if (socket == null || (!socket.isConnected()) && autoconnect) {
            try {
                socket = new Socket(ip, Tunneling.PORT);
            } catch (UnknownHostException e) {
                Tunneling.getLogger().error("Unable to connect " + toString() + " (host " + ip + " unknown).");
                success = false;
            } catch (IOException e) {
                Tunneling.getLogger().error("Unable to connect " + toString() + " (unknown error).");
                success = false;
            }
        }

        return success;
    }

    public String toString() {
        return "Tunnel<" + ip + ">";
    }
}

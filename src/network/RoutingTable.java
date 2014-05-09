package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * This class represents a simple routing table that manages a collection of
 * machine addresses. For every address, it stores the next host address and
 * tunnel IP addresses of hosts.
 * 
 * A routing table file looks like this:
 * 1>2
 * 3>2
 * 5>8
 * 2=192.168.0.1
 */
public class RoutingTable {
    /** The map with routes. The first element is the destination host, the
     * second element is the directly connected host. */
	private Map<Byte, Byte> routes;
    private Map<Byte, String> tunnels;

    /**
     * Creates an empty routing table.
     */
	public RoutingTable() {
		routes = new HashMap<Byte, Byte>();
		tunnels = new HashMap<Byte, String>();
	}

    /**
     * Creates a routing table and parses the file on the given path.
     * @param path The path to the file with the routing table.
     * @throws IOException There is an error with the file.
     */
	public RoutingTable(String path) throws IOException {
		this(new File(path));
	}

    /**
     * Creates a routing table and parses the given file.
     * @param file The file with the routing table.
     * @throws IOException There is an error with the file.
     */
	public RoutingTable(File file) throws IOException {
		this();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		this.fromReader(reader);
		reader.close();
	}

    /**
     * Create routes based on the data from a reader.
     * @param reader The reader with the data.
     * @throws IOException There is an error with the reader.
     */
	public void fromReader(BufferedReader reader) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] parts;

            parts = line.split(">");
            if (parts.length == 2) {
                routes.put((byte) Integer.parseInt(parts[0]), (byte) Integer.parseInt(parts[1]));
                // TODO: Validate addresses
            }

            parts = line.split("=");
            if (parts.length == 2) {
                tunnels.put((byte) Integer.parseInt(parts[0]), parts[1]);
                // TODO: Validate address, IP address
            }
		}
	}

    /**
     * Create routes based on the contents of a string.
     * @param str The string with the data.
     * @throws IOException There is something wrong with the reader.
     */
	public void fromString(String str) throws IOException {
		fromReader(new BufferedReader(new StringReader(str)));
	}

    /**
     * Clears all routes.
     */
    public void clear() {
        routes.clear();
    }

    /**
     * Returns the table of known routes.
     * @return The routes.
     */
    public Map<Byte, Byte> getRoutes() {
        return routes;
    }

    /**
     * Returns the table of known tunnels.
     * @return The tunnels.
     */
    public Map<Byte, String> getTunnels() {
        return tunnels;
    }
	
}

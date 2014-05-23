package network.tpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class represents a simple routing table that manages a collection of
 * machine addresses. For every address, it stores the next host address and
 * tunnel IP addresses of hosts.
 * 
 * A routing table file looks like this:
 * self=6
 * sibling=7
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
    private Byte self;
    private Byte sibling;

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
                try {
                    routes.put((byte) Integer.parseInt(parts[0]), (byte) Integer.parseInt(parts[1]));
                    // TODO: Validate addresses
                } catch (NumberFormatException e) {
                    // Wrong route, ignore.
                }
            }

            parts = line.split("=");
            if (parts.length == 2) {
                try {
                    // Tunnel, if the first argument is an integer.
                    tunnels.put((byte) Integer.parseInt(parts[0]), parts[1]);
                    // TODO: Validate address, IP address
                } catch (NumberFormatException e) {
                    // First argument is not numeric, check for text types.
                    try {
                        if (parts[0].toLowerCase().equals("self")) {
                            self = (byte) Integer.parseInt(parts[1]);
                        } else if (parts[0].toLowerCase().equals("sibling")) {
                            sibling = (byte) Integer.parseInt(parts[1]);
                        }
                    } catch (NumberFormatException f) {
                        // Wrong entry in file, ignore.
                    }
                }
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
	 * Create a GraphViz-format description of the routes.
	 */
	public String toGraph() {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph{");
		
		for (Entry<Byte, Byte> e : routes.entrySet()) {
			sb.append(String.format("%s->%s;", e.getValue(), e.getKey()));
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * Generate a link to 
	 */
	public String toGraphUri() {
		return "https://chart.googleapis.com/chart?cht=gv&chl=" + toGraph();
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

    /**
     * Returns the address of this host.
     * @return The address of this host.
     */
    public byte getSelf() {
        return self;
    }

    /**
     * Returns the address of the sibling host.
     * @return The address of the sibling host.
     */
    public byte getSibling() {
        return sibling;
    }
	
}

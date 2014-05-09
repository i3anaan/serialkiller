package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a simple routing table that manages a collection of
 * machine addresses. For every address, it stores the interface number, i.e.
 * the number of the tunnel or serial cable that needs to be used to contact
 * the address.
 * 
 * A routing table file looks like this:
 * 10 2
 * 20 3
 * 
 * Every line is of the format "machine interface". A method is provided to 
 * look up the correct interface for a given address.
 */
public class RoutingTable {
	private Map <Integer, Integer> hm;
	
	public RoutingTable() {
		hm = new HashMap<Integer, Integer>();
	}

	public RoutingTable(String path) throws IOException {
		this(new File(path));
	}
	
	public RoutingTable(File file) throws IOException {
		this();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		this.fromReader(reader);
		reader.close();
	}
	
	public void fromReader(BufferedReader br) throws IOException {
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\\s");
			hm.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
	}
	
	public void fromString(String str) throws IOException {
		fromReader(new BufferedReader(new StringReader(str)));
	}
	
	/*
	 * Returns a given address from the routing table
	 * @param key the name of the host
	 * @returns the interface associated with the given host
	 */
	public int getInterface(int key) throws NoSuchAddressException {
		if (hm.containsKey(key)) {
			return hm.get(key);
		} else {
			throw new NoSuchAddressException();
		}
	}
	
}

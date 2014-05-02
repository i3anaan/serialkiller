package network;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
 * The main routing table responsible for
 * keeping track of the next hop
 */

public class RoutingTable {
	
	// Private variables
	private Map <Integer, Integer> hm;

	public RoutingTable() throws IllegalArgumentException, IOException{
		
		hm = new HashMap<Integer, Integer>();
		
		BufferedReader reader = new BufferedReader(new FileReader("/path/to/file.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("\\s");
			int key = 1;
			for (String part : parts) {
			    hm.put(key, Integer.parseInt(part));
			    key++;
			}
		}
		reader.close();
		
	}
	
	/*
	 * Returns a given address from the routing table
	 * @param key the name of the host
	 * @returns the address associated with the given host
	 */
	public int getAddress(int key){
		
		return hm.get(key);
	}
	
}

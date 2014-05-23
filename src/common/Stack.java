package common;

import link.LinkLayer;
import network.NetworkLayer;
import phys.PhysicalLayer;
import web.WebService;
import application.ApplicationLayer;

/**
 * The Stack class keeps track of implementations of different layers as
 * public properties. 
 */
public class Stack {
	public ApplicationLayer applicationLayer;
	public NetworkLayer networkLayer;
	public LinkLayer linkLayer;
	public PhysicalLayer physLayer;
	public WebService webService;
}

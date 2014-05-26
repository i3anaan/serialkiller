package common;

import link.LinkLayer;
import network.NetworkLayer;
import phys.PhysicalLayer;
import starter.Starter;
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
	
	private Starter starter;
	
	public Stack() {
		this(null);
	}

	public Stack(Starter starter) {
		super();
		this.starter = starter;
	}
	
	public Stack(ApplicationLayer applicationLayer, NetworkLayer networkLayer,
			LinkLayer linkLayer, PhysicalLayer physLayer) {
		super();
		this.applicationLayer = applicationLayer;
		this.networkLayer = networkLayer;
		this.linkLayer = linkLayer;
		this.physLayer = physLayer;
	}

	/** Request a restart of the entire stack. */
	public void smash() {
		if (starter != null) starter.restart();
	}
}

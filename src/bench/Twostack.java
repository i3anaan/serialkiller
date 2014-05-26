package bench;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import phys.diag.NullPhysicalLayer;
import starter.Starter;
import link.diag.MockLinkLayer;
import network.tpp.TPPNetworkLayer;
import application.ApplicationLayer;
import common.Stack;
import common.Startable;

/** Test that builds two complete stacks based on MockLinkLayer. */
public class Twostack {

	public static void main(String[] args) throws IOException {
		Stack a = new Stack(
				new ApplicationLayer(),
				new TPPNetworkLayer(),
				new MockLinkLayer(),
				new NullPhysicalLayer()
				);
		
		Stack b = new Stack(
				new ApplicationLayer(),
				new TPPNetworkLayer(),
				new MockLinkLayer(),
				new NullPhysicalLayer()
				);
		
		((TPPNetworkLayer)a.networkLayer).loadRoutes("/tmp/a.txt");
		((TPPNetworkLayer)b.networkLayer).loadRoutes("/tmp/b.txt");
		
		((Startable) a.physLayer).start(a);
		((Startable) a.linkLayer).start(b); // not a typo
		((Startable) a.networkLayer).start(a);
		((Startable) a.applicationLayer).start(a);
		
		((Startable) b.physLayer).start(b);
		((Startable) b.linkLayer).start(a); // not a typo
		((Startable) b.networkLayer).start(b);
		((Startable) b.applicationLayer).start(b);
		
		Files.write("self=1\nsibling=2", new File("/tmp/a.txt"), Charsets.UTF_8);
		Files.write("self=2\nsibling=1", new File("/tmp/b.txt"), Charsets.UTF_8);
		

		
		Starter.startGUI(a);
		Starter.startGUI(b);
	}

	
}

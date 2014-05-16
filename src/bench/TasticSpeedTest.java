package bench;

import phys.LptHardwareLayer;
import stats.Stats;
import link.BittasticLinkLayer;

public class TasticSpeedTest {
	public static void main(String[] args) throws Exception {
		BittasticLinkLayer btll = new BittasticLinkLayer(new LptHardwareLayer());
		Thread t = new Thread(btll);
		t.start();
		Thread.sleep(10000);
		System.out.println("Sent: " + Stats.getValue("link.tastic.sent") + " Recvd: " + Stats.getValue("link.tastic.rcvd"));
		System.exit(0);
	}
}

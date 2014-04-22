package link;

import lpt.Lpt;

public class LinkLayerReceiver {
	public static void main(String[] args) {
        Lpt lpt = new Lpt();
        boolean loop_continue = true;
        int oldIn = 0;
        LinkLayer linkLayer = new LinkLayer(new Lpt());

        while(loop_continue) {
        	linkLayer.readByte();
        }
    }
}

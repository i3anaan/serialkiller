package link;

import lpt.Lpt;

public class LinkLayerReceiver {
	public static void main(String[] args) {
        boolean loop_continue = true;
        LinkLayer linkLayer = new AckLinkLayer(new Lpt());
        
        while(loop_continue) {
        	byte newByte = linkLayer.readByte();
            System.out.println((int)newByte);
        }
    }
}

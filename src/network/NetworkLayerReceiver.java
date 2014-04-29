package network;

import link.LinkLayer;
import link.SingleDirectionLinkLayer;
import lpt.Lpt;

public class NetworkLayerReceiver {

	public static void main(String[] args) {
        boolean loop_continue = true;
        LinkLayer linkLayer = new SingleDirectionLinkLayer(new Lpt());
        
        while(loop_continue) {
        	byte newByte = linkLayer.readByte();
            System.out.println((int)newByte);
        }
    }
}

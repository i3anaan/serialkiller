package link;

import lpt.Lpt;

public class LinkLayerReceiver {
	public static void main(String[] args) {
        Lpt lpt = new Lpt();
        boolean loop_continue = true;
        int oldIn = 0;
        LinkLayer linkLayer = new LinkLayer(new Lpt());
        byte oldByte = 1;
        
        
        while(loop_continue) {
        	byte newByte = linkLayer.readByte();
        	if(newByte!=oldByte){
        		System.out.println((int)newByte);
        		oldByte = newByte;
        	}
        }
    }
}

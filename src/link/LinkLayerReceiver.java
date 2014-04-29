package link;

import phys.LptHardwareLayer;

public class LinkLayerReceiver {

	public static void main(String[] args) {
        LinkLayer linkLayer = new AckingLinkLayer(new LptHardwareLayer());

        while (true) {
        	byte newByte = linkLayer.readByte();
            System.out.println((int)newByte);
        }
    }
}

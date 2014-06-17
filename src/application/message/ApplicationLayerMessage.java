package application.message;

import network.Payload;

/**
 * Abstract superclass for all application-layer messages (which are them-
 * selves network-layer payloads). 
 */
public abstract class ApplicationLayerMessage {
	/** True if this message is inbound; false if it is outbound. */
	private boolean inbound;
	
	/** 
	 * Payload. For incoming packages, this is the payload as received from
	 * the network layer. For outgoing packages, this is the serialized message.
	 */
	private Payload payload;
	
	/** Construct an outobund ApplicationLayerMessage with an empty payload. */
	public ApplicationLayerMessage(byte address) {
		this.inbound = false;
		this.payload = new Payload(new byte[]{}, address);
	}
	
	/** Construct an inbound ApplicationLayerMessage from a network layer payload. */
	public ApplicationLayerMessage(Payload payload) {
		this.inbound = true;
		this.payload = payload;
	}
	
	/** Returns true if this is an inbound (incoming/ingress) packet. */
	public boolean isInbound() {
		return inbound;
	}
	
	/** Returns true if this is an outbound (outgoing/egress) packet. */
	public boolean isOutbound() {
		return !inbound;
	}
	
	/** Returns the message payload. */
	public Payload getPayload() {
		return payload;
	}
	
	/** Returns the message address. */
	public byte getAddress() {
		return payload.address;
	}
	
	/** Sets the payload data. */
	protected Payload setData(byte[] data) {
		payload.data = data;
		return payload;
	}
}

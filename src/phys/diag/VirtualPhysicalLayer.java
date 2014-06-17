package phys.diag;

import phys.PhysicalLayer;

/**
 * An implementation of a physical layer that can be used to connect different
 * threads on the same machine.
 */
public class VirtualPhysicalLayer extends PhysicalLayer {
	private static VirtualPhysicalLayer FIRST_INSTANCE;
	private VirtualPhysicalLayer that;
	private byte state;

	public VirtualPhysicalLayer(){
		if(FIRST_INSTANCE==null){
			System.out.println("Setting first Instance to: "+this);
			FIRST_INSTANCE = this;
		}
	}
	
	/** Connect this instance to another instance of VirtualPhysicalLayer. */
	public void connect(VirtualPhysicalLayer that) {
		this.that = that;
		this.state = 0;
	}

	@Override
	public void sendByte(byte data) {
		that.takeByte(data);
	}

	@Override
	public byte readByte() {
		synchronized (this) {
			return state;
		}
	}

	/** Takes a byte sent by (possibly) another thread. */
	public void takeByte(byte data) {
		if(FIRST_INSTANCE==this){
			System.out.print((data&1)==1 ? "1" : "0");
		}
		state = data;
	}
}

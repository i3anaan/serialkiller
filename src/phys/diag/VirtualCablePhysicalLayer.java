package phys.diag;

import phys.PhysicalLayer;

public class VirtualCablePhysicalLayer extends PhysicalLayer {
	private VirtualCable cable;
	private int side;
	
	public VirtualCablePhysicalLayer(VirtualCable cable, int side) {
		this.cable = cable;
		this.side = side;
	}

	@Override
	public void sendByte(byte data) {
		cable.locks[other(side)].lock();
		cable.bytes[other(side)] = data;
		cable.locks[other(side)].unlock();
	}

	@Override
	public byte readByte() {
		byte b;
		
		cable.locks[side].lock();
		b = cable.bytes[side];
		cable.locks[side].unlock();
		
		return b;
	}
	
	private int other(int side) {
		return (side == 0) ? 1 : 0;
	}
}

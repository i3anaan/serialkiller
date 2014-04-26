package phys;

/**
 * If used on both sides, just after the physical layer, will make sure both
 * sides always read a set startByte (default 0) as first byte. This is useful
 * for context sensitive follow-up layers to make sure it always start the same
 * way.
 * 
 * @author I3anaan
 * 
 */
public class CleanStartPhysicalLayer extends PhysicalLayer {

	byte startByte = 0;
	boolean fakingFirst = true;
	byte startData;

	public CleanStartPhysicalLayer(PhysicalLayer phys) {
		super();
		this.down = phys;
		startData = down.readByte();
		down.sendByte((byte) startByte);
	}

	public CleanStartPhysicalLayer(PhysicalLayer phys, byte startByte) {
		super();
		this.startByte = startByte;

		this.down = phys;
		startData = down.readByte();
		down.sendByte((byte) startByte);
	}

	@Override
	public void sendByte(byte data) {
		down.sendByte(data);
	}

	@Override
	public byte readByte() {
		byte data = down.readByte();
		if (fakingFirst) {
			if(data==startData){
				return startByte;
				//While the first byte has not changed yet, fake it.
			}else{
				//Input has changed, it is not the first byte anymore, return the new input.
				//Also set fakingFirst to false, since it should never fake anything anymore.
				fakingFirst = false;
				return data;
			}
		}else{
			return data;
		}
	}
}

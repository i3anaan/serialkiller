package link.jack;

import java.util.ArrayList;
import java.util.Random;

import link.FrameLinkLayer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.BitSet2;
import util.ByteArrays;
import util.Bytes;
import util.encoding.HammingCode;

public class JackTheRipper extends FrameLinkLayer {
	public static final HammingCode HC = new HammingCode(4);
	public static final Unit UNIT_IN_USE = HammingUnit.getDummy();
	
	public static final Random R = new Random();
	
	DCFDXLLSSReadSendManager2000 down;

	public JackTheRipper(DCFDXLLSSReadSendManager2000 down) {
		this.down = down;
	}

	@Override
	public void sendFrame(byte[] data) {
		if (UNIT_IN_USE instanceof HammingUnit) {
			//System.out.println("Data to send: " + Bytes.format(data[0]));
			BitSet2 dataAsBitSet = ByteArrays.toBitSet(data);
			for (int i = 0; i < data.length * 8; i = i + 4) {
				down.sendUnit(new HammingUnit(dataAsBitSet.get(i, i + 3),
						false, HC));
				//System.out
				//		.println("Sent unit: "
				//				+ new HammingUnit(dataAsBitSet.get(i, i + 3),
				//						false, HC));
			}
			//System.out.println("Sending End Of Frames");
			down.sendUnit(UNIT_IN_USE.getEndOfFrame());
			down.sendUnit(UNIT_IN_USE.getEndOfFrame());
		} else {
			throw new NotImplementedException();
		}
	}

	@Override
	public byte[] readFrame() {
		if (UNIT_IN_USE instanceof HammingUnit) {
			ArrayList<Byte> dataFrame = new ArrayList<Byte>();
			boolean frameComplete = false;
			while (!frameComplete) {
				HammingUnit u1 = (HammingUnit) down.readUnit();
				HammingUnit u2 = (HammingUnit) down.readUnit();
				BitSet2 fullByte = BitSet2.concatenate(
						u2.getDecodedPayloadAsBitSet(),
						u1.getDecodedPayloadAsBitSet());
				if (!u1.isSpecial() != !u1.isSpecial()) {// Error
					// TODO ERROR, out of sync.
					System.out.println("ERROR! out of sync");
				} else if (!u1.isSpecial() && !u1.isSpecial()) {// Data
					dataFrame.add(fullByte.toByteArray()[0]);
				} else if (u1.getDecodedPayloadAsByte() == u2
						.getDecodedPayloadAsByte() && u1.isEndOfFrame()) { // flag
					frameComplete = true;
					//System.out.println("End of frame!!");
				}
			}
			return ByteArrays.fromList(dataFrame);
		} else {
			throw new NotImplementedException();
		}
	}
}

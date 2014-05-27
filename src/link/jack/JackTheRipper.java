package link.jack;

import java.util.ArrayList;
import java.util.Random;

import common.NotReadyForUseException;
import common.Stack;
import common.Startable;
import link.FrameLinkLayer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.BitSet2;
import util.ByteArrays;
import util.Bytes;
import util.encoding.HammingCode;

public class JackTheRipper extends FrameLinkLayer implements Startable {
	public static final HammingCode HC = new HammingCode(4);
	public static final Unit UNIT_IN_USE = HammingUnit.getDummy();
	public static final Frame FRAME_IN_USE = FixingFrame.getDummy();

	public static final Random R = new Random();
	private boolean readyForUse;

	DCFDXLLSSReadSendManager2000 down;
	public JackTheRipper() {
		// run Start();
	}

	public JackTheRipper(DCFDXLLSSReadSendManager2000 down) {
		this.down = down;
		readyForUse = true;
	}

	@Override
	public void sendFrame(byte[] data) {
		if (readyForUse) {
			down.down.log("Sending data: " + Bytes.format(data[0]));
			if (UNIT_IN_USE instanceof HammingUnit) {
				// System.out.println("Data to send: " + Bytes.format(data[0]));
				BitSet2 dataAsBitSet = ByteArrays.toBitSet(data);
				for (int i = 0; i < data.length * 8; i = i + 4) {
					down.sendUnit(new HammingUnit(dataAsBitSet.get(i, i + 4),
							false, HC));
					down.down.log("Sent Unit: "
							+ new HammingUnit(dataAsBitSet.get(i, i + 4),
									false, HC));
				}
				// System.out.println("Sending End Of Frames");
				down.sendUnit(UNIT_IN_USE.getEndOfFrame());
				down.sendUnit(UNIT_IN_USE.getEndOfFrame());
			} else {
				throw new NotImplementedException();
			}
		} else {
			throw new NotReadyForUseException();
		}
	}

	@Override
	public byte[] readFrame() {
		if (readyForUse) {
			if (UNIT_IN_USE instanceof HammingUnit) {
				ArrayList<Byte> dataFrame = new ArrayList<Byte>();
				boolean frameComplete = false;
				while (!frameComplete) {
					HammingUnit u1 = (HammingUnit) down.readUnit();
					HammingUnit u2 = (HammingUnit) down.readUnit();
					BitSet2 fullByte = BitSet2.concatenate(
							u1.getDecodedPayloadAsBitSet(),
							u2.getDecodedPayloadAsBitSet());
					if (!u1.isSpecial() != !u1.isSpecial()) {// Error
						// TODO ERROR, out of sync.
						System.out.println("ERROR! out of sync");
					} else if (!u1.isSpecial() && !u1.isSpecial()) {// Data
						dataFrame.add(fullByte.toByteArray()[0]);
					} else if (u1.getDecodedPayloadAsByte() == u2
							.getDecodedPayloadAsByte() && u1.isEndOfFrame()) { // flag
						frameComplete = true;
						// System.out.println("End of frame!!");
					}
				}
				down.down.log("Delivering dataFrame: "
						+ Bytes.format(ByteArrays.fromList(dataFrame)[0]));
				return ByteArrays.fromList(dataFrame);
			} else {
				throw new NotImplementedException();
			}
		} else {
			throw new NotReadyForUseException();
		}

	}

	@Override
	public Thread start(Stack stack) {
		this.down = new DCFDXLLSSReadSendManager2000(
				new DelayCorrectedFDXLinkLayerSectionSegment(stack.physLayer));
		readyForUse = true;
		return null; // TODO mogelijk hoofdthread teruggeven.
	}
	
	@Override
	public String toString(){
		String jkr = 
				 "     ██╗ █████╗  ██████╗██╗  ██╗    ████████╗██╗  ██╗███████╗    ██████╗ ██╗██████╗ ██████╗ ███████╗██████╗ \n"
				+"     ██║██╔══██╗██╔════╝██║ ██╔╝    ╚══██╔══╝██║  ██║██╔════╝    ██╔══██╗██║██╔══██╗██╔══██╗██╔════╝██╔══██╗\n"
				+"     ██║███████║██║     █████╔╝        ██║   ███████║█████╗      ██████╔╝██║██████╔╝██████╔╝█████╗  ██████╔╝\n"
				+"██   ██║██╔══██║██║     ██╔═██╗        ██║   ██╔══██║██╔══╝      ██╔══██╗██║██╔═══╝ ██╔═══╝ ██╔══╝  ██╔══██╗\n"
				+"╚█████╔╝██║  ██║╚██████╗██║  ██╗       ██║   ██║  ██║███████╗    ██║  ██║██║██║     ██║     ███████╗██║  ██║\n"
				+" ╚════╝ ╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝       ╚═╝   ╚═╝  ╚═╝╚══════╝    ╚═╝  ╚═╝╚═╝╚═╝     ╚═╝     ╚══════╝╚═╝  ╚═╝\n\n";
		return jkr
				+ "Also previously known as DelayCorrectedFullDuplexLinkLayerSectionSegmentReadSendManager2000FrameReadSendManager3000"
				+ "\n##> Using Frame:\n\t"+FRAME_IN_USE.toCoolString()
				+ "\n##> Using Units:\n\t"+UNIT_IN_USE.toCoolString()
				+ "\nReady to rip.\n\n";
	}
}

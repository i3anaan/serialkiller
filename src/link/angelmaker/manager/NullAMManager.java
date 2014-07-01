package link.angelmaker.manager;

import util.BitSet2;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.Node;

/**
 * AMManager that does nothing.
 * Used for testing.
 * @author I3anaan
 *
 */
public class NullAMManager implements AMManager.Server{

	@Override
	public void setExchanger(BitExchanger exchanger) {
	}

	@Override
	public void enable() {		
	}

	@Override
	public void sendBytes(byte[] bytes) {
	}

	@Override
	public byte[] readBytes() {
		return null;
	}

	@Override
	public Node getCurrentSendingNode() {
		return null;
	}

	@Override
	public Node getCurrentReceivingNode() {
		return null;
	}
	@Override
	public BitSet2 getNextBits() {
		return null;
	}

}

package link.angelmaker.manager;

import util.BitSet2;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NullNode;

public class NullAMManager implements AMManager.Server{

	@Override
	public void setExchanger(BitExchanger exchanger) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBytes(byte[] bytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] readBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getCurrentSendingNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getCurrentReceivingNode() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public BitSet2 getNextBits() {
		// TODO Auto-generated method stub
		return null;
	}

}

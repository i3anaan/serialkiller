package link.angelmaker;

import phys.PhysicalLayer;
import phys.diag.NullPhysicalLayer;
import util.BitSet2;
import link.angelmaker.bitexchanger.*;
import link.angelmaker.codec.*;
import link.angelmaker.flags.*;
import link.angelmaker.manager.*;
import link.angelmaker.nodes.*;

public class AngelMakerConfig {
	//Encoding (ErrorDetectionNode)
	public static final Codec CODEC = new MixedCodec(new Codec[]{new HammingCodec(8),new ParityBitsCodec()});
	
	//Packet fromat (SequencedNode)
	public static final int PACKET_BYTE_COUNT = 8;
	public static final int PACKET_BIT_COUNT = PACKET_BYTE_COUNT*8;
	public static final int MESSAGE_BIT_COUNT = 8;
	
	//BitExchanger variables
	public static final int STABILITY = 2;
	public static final long SYNC_RANGE_WAIT = 100l*1000000l;
	public static final long SYNC_TIMEOUT_DESYNC = 1000l*1000000l;
	public static final long READ_TIMEOUT_NO_ACK = 20l*1000000l;
	
	//Flags used by the FlaggingNode
	public static Flag getStartFlag(){
		return new DummyFlag(new BitSet2("11101"));
	}
	public static Flag getEndFlag(){
		return new FixedEndFlag();
	}
	
	//Default modules used by AngelMaker (Starter uses these for Node, Manager and BitExchanger)
	public static PhysicalLayer getPhys(){
		return new NullPhysicalLayer();
	}
	public static Node getNode(){
		return new FlaggingNode(null);
	}
	public static AMManager getAMManager(){
		return new MemoryRetransmittingManager();
	}
	public static BitExchanger getBitExchanger(){
		return new HighSpeedBitExchanger();
	}
}

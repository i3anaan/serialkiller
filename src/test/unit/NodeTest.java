package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.codec.ParityBitsCodec;
import link.angelmaker.nodes.ErrorDetectionNode;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.SequencedNode;

import org.junit.Test;

import util.BitSet2;

public class NodeTest {
	@Test
	public void testSequencedNode(){
		SequencedNode seqNode = new SequencedNode(null, SequencedNode.PACKET_BIT_COUNT, SequencedNode.MESSAGE_BIT_COUNT);
		BitSet2 data = new BitSet2(new byte[]{1,2,3,4,5,6,7,8});
		BitSet2 seq = new BitSet2("1100");
		BitSet2 message = new BitSet2("0111");
		seqNode.giveOriginal(data);
		seqNode.setSeq(seq);
		seqNode.setMessage(message);
		assertEquals(data,seqNode.getOriginal());
		assertEquals(seq,seqNode.getSeq());
		assertEquals(message,seqNode.getMessage());
		
		SequencedNode constructed = new SequencedNode(null, SequencedNode.PACKET_BIT_COUNT, SequencedNode.MESSAGE_BIT_COUNT);
		constructed.giveConverted(seqNode.getConverted());
		assertEquals(data,constructed.getOriginal());
		assertEquals(seq,constructed.getSeq());
		assertEquals(message,constructed.getMessage());
		
		
		SequencedNode clone = (SequencedNode) seqNode.getClone();
		assertEquals(data,clone.getOriginal());
		assertEquals(seq,clone.getSeq());
		assertEquals(message,clone.getMessage());
	}
	
	@Test
	public void testErrorDetectionNode(){
		ErrorDetectionNode errNode = new ErrorDetectionNode(null,64, new ParityBitsCodec());
		BitSet2 data = new BitSet2(new byte[]{1,2,3,4,5,6,7,8});
		BitSet2 seq = new BitSet2("0011");
		BitSet2 message = new BitSet2("1101");
		
		errNode.giveOriginal(data);
		((SequencedNode)errNode.getChildNodes()[0]).setSeq(seq);
		((SequencedNode)errNode.getChildNodes()[0]).setMessage(message);
		assertEquals(data,errNode.getOriginal());
		assertEquals(data,((SequencedNode)errNode.getChildNodes()[0]).getOriginal());
		assertEquals(seq,((SequencedNode)errNode.getChildNodes()[0]).getSeq());
		assertEquals(message,((SequencedNode)errNode.getChildNodes()[0]).getMessage());
		assertEquals(BitSet2.concatenate(seq, BitSet2.concatenate(data, message)),((SequencedNode)errNode.getChildNodes()[0]).getConverted());	
		
		
		ErrorDetectionNode constructed = new ErrorDetectionNode(null,64,new ParityBitsCodec());
		constructed.giveConverted(errNode.getConverted());
		assertEquals(data,constructed.getOriginal());
		assertEquals(seq,((SequencedNode)constructed.getChildNodes()[0]).getSeq());
		assertEquals(message,((SequencedNode)constructed.getChildNodes()[0]).getMessage());
		
		
		ErrorDetectionNode clone = (ErrorDetectionNode) errNode.getClone();
		assertEquals(data,clone.getOriginal());
		assertEquals(seq,((SequencedNode)clone.getChildNodes()[0]).getSeq());
		assertEquals(message,((SequencedNode)clone.getChildNodes()[0]).getMessage());
	}

	@Test
	public void testFlaggingNode(){
		for(int i=0;i<8;i++){
			FlaggingNode flagNode = new FlaggingNode(null);
			BitSet2 data = new BitSet2(new byte[]{1,2,3,4,5,6,7,8});
			data = data.get(0,(i+1)*8);
			BitSet2 seq = new BitSet2("0011");
			BitSet2 message = new BitSet2("1101");
			
			flagNode.giveOriginal(data);
			((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).setSeq(seq);
			((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).setMessage(message);
			assertEquals(data,flagNode.getOriginal());
			assertEquals(data,((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getOriginal());
			assertEquals(seq,((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getMessage());
			assertEquals(BitSet2.concatenate(seq, BitSet2.concatenate(data, message)),((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getConverted());
			
			FlaggingNode constructed = new FlaggingNode(null);
			constructed.giveConverted(flagNode.getConverted());
			assertEquals(data,constructed.getOriginal());
			assertEquals(seq,((SequencedNode)constructed.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)constructed.getChildNodes()[0].getChildNodes()[0]).getMessage());
			
			
			FlaggingNode clone = (FlaggingNode) flagNode.getClone();
			assertEquals(data,clone.getOriginal());
			assertEquals(seq,((SequencedNode)clone.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)clone.getChildNodes()[0].getChildNodes()[0]).getMessage());
		}
	}
}

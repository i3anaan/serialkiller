package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.AngelMakerConfig;
import link.angelmaker.codec.ParityBitsCodec;
import link.angelmaker.nodes.ErrorDetectionNode;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.SequencedNode;

import org.junit.Test;

import util.BitSet2;

public class NodeTest {
	@Test
	public void testSequencedNode(){
		SequencedNode seqNode = new SequencedNode(null);
		BitSet2 data = new BitSet2(new byte[]{1,2,3,4,5,6,7,8});
		BitSet2 seq = new BitSet2("11001100");
		BitSet2 message = new BitSet2("01110111");
		seqNode.giveOriginal(data);
		seqNode.setSeq(seq);
		seqNode.setMessage(message);
		assertEquals(data,seqNode.getOriginal());
		assertEquals(seq,seqNode.getSeq());
		assertEquals(message,seqNode.getMessage());
		
		SequencedNode constructed = new SequencedNode(null);
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
		ErrorDetectionNode errNode = new ErrorDetectionNode(null);
		BitSet2 data = new BitSet2(new byte[]{1,2,3,4,5,6,7,8});
		BitSet2 seq = new BitSet2("00110011");
		BitSet2 message = new BitSet2("11011101");
		
		errNode.giveOriginal(data);
		((SequencedNode)errNode.getChildNodes()[0]).setSeq(seq);
		((SequencedNode)errNode.getChildNodes()[0]).setMessage(message);
		assertEquals(data,errNode.getOriginal());
		assertEquals(data,((SequencedNode)errNode.getChildNodes()[0]).getOriginal());
		assertEquals(seq,((SequencedNode)errNode.getChildNodes()[0]).getSeq());
		assertEquals(message,((SequencedNode)errNode.getChildNodes()[0]).getMessage());
		assertEquals(BitSet2.concatenate(seq, BitSet2.concatenate(message,data )),((SequencedNode)errNode.getChildNodes()[0]).getConverted());	
		
		
		ErrorDetectionNode constructed = new ErrorDetectionNode(null);
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
			BitSet2 seq = new BitSet2("00110011");
			BitSet2 message = new BitSet2("11011101");
			
			flagNode.giveOriginal(data);
			((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).setSeq(seq);
			((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).setMessage(message);
			assertEquals(data,flagNode.getOriginal());
			assertEquals(data,((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getOriginal());
			assertEquals(seq,((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getMessage());
			assertEquals(BitSet2.concatenate(seq, BitSet2.concatenate(message, data)),((SequencedNode)flagNode.getChildNodes()[0].getChildNodes()[0]).getConverted());
			
			FlaggingNode constructed = new FlaggingNode(null);
			constructed.giveConverted(flagNode.getConverted());
			assertEquals(data,constructed.getOriginal());
			assertEquals(seq,((SequencedNode)constructed.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)constructed.getChildNodes()[0].getChildNodes()[0]).getMessage());
			
			
			FlaggingNode clone = (FlaggingNode) flagNode.getClone();
			assertEquals(data,clone.getOriginal());
			assertEquals(seq,((SequencedNode)clone.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)clone.getChildNodes()[0].getChildNodes()[0]).getMessage());
			
			
			//Network simulation.
			FlaggingNode networkConstructed = new FlaggingNode(null);
			BitSet2 converted = flagNode.getConverted();
			BitSet2 randomJunkFront = new BitSet2("00011");
			BitSet2 randomJunkEnd = new BitSet2("001010");
			BitSet2 given = BitSet2.concatenate(randomJunkFront,BitSet2.concatenate(converted, randomJunkEnd));
			int index=0;
			while(index<given.length()){
				int add = Math.min((int)(Math.random()*3),given.length()-index);
				networkConstructed.giveConverted(given.get(index,index+add));
				index = index+add;
			}
			assertEquals(data,networkConstructed.getOriginal());
			assertEquals(seq,((SequencedNode)networkConstructed.getChildNodes()[0].getChildNodes()[0]).getSeq());
			assertEquals(message,((SequencedNode)networkConstructed.getChildNodes()[0].getChildNodes()[0]).getMessage());
			
		}
	}
}

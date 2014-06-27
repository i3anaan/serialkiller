package test.unit;

import static org.junit.Assert.*;

import javax.sound.midi.Sequence;

import link.angelmaker.codec.ParityBitsCodec;
import link.angelmaker.nodes.ErrorDetectionNode;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.SequencedNode;

import org.junit.Test;

import util.BitSet2;

public class NodeTest {
	
	/*
	//@Test
	public void testGeneral() {
		
		
		BitSet2 data = new BitSet2(new byte[] { 0, -7, 127, -128, -1, 10, 4,
				-6, 2, 8, 3, 7 });
		for (Node base : nodes) {
			//System.out.println("Testing: "+base.getClass().getSimpleName());
			assertEquals(root, base.getParent());
			for (int i = 0; i < 200; i++) {
				data = new BitSet2();
				for (int a = 0; a < i; a++) {
					data.set(a, Math.random() > 0.5);
				}
				Node clone = base.getClone();
				Node n = base.getClone();

				BitSet2 unused = n.giveOriginal(data);
				BitSet2 stored = data.get(0, data.length() - unused.length());
				if (i > 0) {
					assertTrue(stored.length() > 0);
				}
				assertTrue(data != unused);
				if (!(base instanceof FlaggingNode)) {
					assertEquals(stored, n.getOriginal());
					assertEquals(data,
							BitSet2.concatenate(n.getOriginal(), unused));
				}
				clone.giveConverted(n.getConverted());
				if (n.isFull()) {
					assertEquals(n.getOriginal(), clone.getOriginal());
					//System.out.println("["+n.getOriginal().length()+"]  "+n.getOriginal());
					assertTrue(n.isCorrect());
				}
			}
		}
	}
	
	//@Test
	public void testNetworkSimulation() {
		// Network exchange simulation
		
		for (Node base : nodes) {

			for (byte start = Byte.MIN_VALUE; start < Byte.MAX_VALUE; start++) {
				byte b = start;
				Node sender = base.getClone();
				Node receiver = base.getClone();
				BitSet2 queueOriginal = new BitSet2();
				BitSet2 queueOriginalTotal = new BitSet2(); // Contains all the
															// data added.
				boolean tookBits = false;
				while (!sender.isFull()) {
					// Add in parts, simulate network behaviour.
					BitSet2 before = (BitSet2) queueOriginal.clone();
					System.out.println(sender.isFull());
					queueOriginal = sender.giveOriginal(queueOriginal);
					BitSet2 taken = before.get(0, before.length()
							- queueOriginal.length());
					if (!before.equals(queueOriginal)) {
						tookBits = true;
					}
					queueOriginal = BitSet2.concatenate(queueOriginal,
							new BitSet2(b));
					
					System.out.println("Taken:\t"+taken);
					queueOriginalTotal = BitSet2.concatenate(
							queueOriginalTotal, taken);
					if (tookBits) {
						assertFalse(queueOriginal.equals(queueOriginalTotal));
					}
					b++;
				}
				assertTrue(sender.isReady());
				assertTrue(tookBits);
				// Is full, dont consume anymore
				//TODO what is this assert?
				//assertEquals(new BitSet2((byte) 5),
				//		sender.giveOriginal(new BitSet2((byte) 5)));
				BitSet2 queueConvertedTotal = sender.getConverted();
				BitSet2 queueConvertedReceived = new BitSet2();
				BitSet2 receivedSoFar = new BitSet2();
				int from = 0;
				while (!receiver.isFull()) {
					// Add in parts, simulate network behaviour.
					queueConvertedReceived = receiver
							.giveConverted(queueConvertedReceived);
					int to = Math.min((int) (Math.random() * 8),
							queueConvertedTotal.length() - from);
					queueConvertedReceived = BitSet2.concatenate(
							queueConvertedReceived,
							queueConvertedTotal.get(from, from + to));
					receivedSoFar = BitSet2.concatenate(receivedSoFar, queueConvertedReceived);
					
					from = from + to;
				}
				assertTrue(receiver.isReady());

				assertEquals(queueConvertedTotal.length(), from);
				System.out.println("Sender Original:\t" + sender.getOriginal()
						+ " \t[" + sender.getOriginal().length() + "]");
				System.out.println("Sender Converted:\t"
						+ sender.getConverted() + " \t["
						+ sender.getConverted().length() + "]");
				System.out.println("Received Converted:\t"
						+ receiver.getConverted() + " \t["
						+ receiver.getConverted().length() + "]");
				System.out.println("Received Original:\t"
						+ receiver.getOriginal() + " \t["
						+ receiver.getOriginal().length() + "]");
				System.out.println("Test Original:\t\t" + queueOriginalTotal
						+ " \t[" + queueOriginalTotal.length() + "]");
				
				assertEquals(queueOriginalTotal.length(),receiver.getOriginal().length());
				assertEquals(receiver.getOriginal(), queueOriginalTotal);
			}
			System.out.println("Test "+base.getClass().getSimpleName()+" Done.");
			// TODO Continue testing.
			// TODO check if split from isComplete() into isFull() and isReady()
			// was succesfully completed.
			// Allong the lines of:
			if (base instanceof Node.Fillable) {
				// test filling.
			}
		}
	}

	@Test
	public void testPureNode() {
	}
	*/
	
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
		ErrorDetectionNode errNode = new ErrorDetectionNode(null,64);
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
		
		
		ErrorDetectionNode constructed = new ErrorDetectionNode(null,64);
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
		FlaggingNode flagNode = new FlaggingNode(null);
		BitSet2 data = new BitSet2(new byte[]{1,2,3,4,5,6,7,8});
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

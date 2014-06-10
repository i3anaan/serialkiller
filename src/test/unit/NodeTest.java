package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.nodes.BasicLeafNode;
import link.angelmaker.nodes.FlaggingNode;
import link.angelmaker.nodes.FrameCeptionNode;
import link.angelmaker.nodes.FrameNode;
import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.PureNode;
import org.junit.Test;

import util.BitSet2;

public class NodeTest {
	public  Node root = new PureNode(null, 1);
	public Node[] nodes = new Node[] { new PureNode(root, 80),
		new FrameNode<Node>(root, 10),
		new FrameCeptionNode<Node>(root, 2),
		new FrameCeptionNode<Node>(root, 0), new FlaggingNode(root, 8) };
	
	//@Test
	public void testGeneral() {
		
		
		BitSet2 data = new BitSet2(new byte[] { 0, -7, 127, -128, -1, 10, 4,
				-6, 2, 8, 3, 7 });
		for (Node base : nodes) {
			System.out.println("Testing: " + base);
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
				System.out.println("i=" + i);
				if (!(base instanceof FlaggingNode)) {
					assertEquals(stored, n.getOriginal());
					assertEquals(data,
							BitSet2.concatenate(n.getOriginal(), unused));
				}
				System.out.println("Converted = " + n.getConverted());
				clone.giveConverted(n.getConverted());
				if (n.isFull()) {
					System.out.println("N-Original:\t" + n.getOriginal());
					System.out.println("C-Original:\t" + clone.getOriginal());
					assertEquals(n.getOriginal(), clone.getOriginal());
					assertTrue(n.isCorrect());
				}
			}
		}
	}

	@Test
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

				System.out.println("####################>Hand data to sender");
				boolean tookBits = false;
				while (!sender.isFull()) {
					// Add in parts, simulate network behaviour.
					BitSet2 before = (BitSet2) queueOriginal.clone();
					System.out.println("QueueOriginal: " + queueOriginal);
					queueOriginal = sender.giveOriginal(queueOriginal);
					System.out.println("Sender has:" + sender.getOriginal());
					BitSet2 taken = before.get(0, before.length()
							- queueOriginal.length());
					if (!before.equals(queueOriginal)) {
						tookBits = true;
					}
					queueOriginal = BitSet2.concatenate(queueOriginal,
							new BitSet2(b));
					queueOriginalTotal = BitSet2.concatenate(
							queueOriginalTotal, taken);
					if (tookBits) {
						assertFalse(queueOriginal.equals(queueOriginalTotal));
					}
					b++;
				}
				assertTrue(sender.isReady());
				assertTrue(tookBits);
				System.out.println("##> Done giving sender data");
				// Is full, dont consume anymore
				//TODO what is this assert?
				//assertEquals(new BitSet2((byte) 5),
				//		sender.giveOriginal(new BitSet2((byte) 5)));
				BitSet2 queueConvertedTotal = sender.getConverted();
				BitSet2 queueConvertedReceived = new BitSet2();
				BitSet2 receivedSoFar = new BitSet2();
				int from = 0;
				System.out.println("Giving Receiver: "+queueConvertedTotal);
				while (!receiver.isFull()) {
					// Add in parts, simulate network behaviour.
					queueConvertedReceived = receiver
							.giveConverted(queueConvertedReceived);
					System.out.println("Received so far: "+receivedSoFar);
					int to = Math.min((int) (Math.random() * 8),
							queueConvertedTotal.length() - from);
					queueConvertedReceived = BitSet2.concatenate(
							queueConvertedReceived,
							queueConvertedTotal.get(from, from + to));
					receivedSoFar = BitSet2.concatenate(receivedSoFar, queueConvertedReceived);
					
					from = from + to;
				}
				System.out.println("Before ready receiver has Converted:\t"+receiver.getConverted());
				System.out.println("Before ready receiver has Original:\t"+receiver.getOriginal());
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

}

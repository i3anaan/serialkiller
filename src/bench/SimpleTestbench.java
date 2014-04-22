package bench;

import link.MockLinkLayer;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class SimpleTestbench {

	public static void main(String[] args) {
		byte x = 42;
		
		MockLinkLayer a = new MockLinkLayer();
		MockLinkLayer b = new MockLinkLayer();
		
		a.setPrefix(">").setDebug(true).connect(b);
		b.setPrefix("<").setDebug(true).connect(a);
		
		a.sendByte(x);
		checkEqual(b.readByte(), x);
	}
	
	/** Like assert, but doesn't care about the assertion flag. */
	private static void checkEqual(Object a, Object b) {
		if (!a.equals(b)) throw new AssertionError("Assertion failed: " + a + " != " + b);
	}

}

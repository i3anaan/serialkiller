package link.angelmaker.codec;

import static org.junit.Assert.*;

import org.junit.Test;

import util.BitSet2;

public class NaiveRepeaterTest {
	private Codec codec = new NaiveRepeaterCodec();
	
	@Test
	public void testEncode() {
		assertEquals(new BitSet2("0000 0000 0000 0000"),codec.encode(new BitSet2("0000 0000")));
		assertEquals(new BitSet2("1111 1111 1111 1111"),codec.encode(new BitSet2("1111 1111")));
		assertEquals(new BitSet2("0011 0011 0011 0011"),codec.encode(new BitSet2("0101 0101")));
	}
	@Test
	public void testDecode(){
		assertEquals(new BitSet2("0000 0000"),codec.decode(new BitSet2("0000 0000 0000 0000")).get());
		assertEquals(new BitSet2("1111 1111"),codec.decode(new BitSet2("1111 1111 1111 1111")).get());
		assertEquals(new BitSet2("0101 0101"),codec.decode(new BitSet2("0011 0011 0011 0011")).get());
	}
	@Test
	public void testBoth(){
		assertEquals(new BitSet2("0000 0000"),codec.decode(codec.encode(new BitSet2("0000 0000"))).get());
		assertEquals(new BitSet2("1111 1111"),codec.decode(codec.encode(new BitSet2("1111 1111"))).get());
		assertEquals(new BitSet2("0101 0101"),codec.decode(codec.encode(new BitSet2("0101 0101"))).get());
	}

}

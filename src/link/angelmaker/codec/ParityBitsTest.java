package link.angelmaker.codec;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import util.BitSet2;

public class ParityBitsTest  {
	private ParityBitsCodec pbc;
	
	@Before
	public void setUp() {
		pbc = new ParityBitsCodec();
	}

	@Test
	public void testEncode() {
		assertEquals(new BitSet2("0000000000"), pbc.encode(new BitSet2("00000000")));
		assertEquals(new BitSet2("0000000101"), pbc.encode(new BitSet2("00000001")));
		assertEquals(new BitSet2("1000000110"), pbc.encode(new BitSet2("10000001")));
		assertEquals(new BitSet2("1111000000"), pbc.encode(new BitSet2("11110000")));
	}

}

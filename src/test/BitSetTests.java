package test;

import static org.junit.Assert.*;

import util.BitSet2;

import org.junit.Test;

import util.BitSets;

public class BitSetTests {

	@Test
	public void test() {
		BitSet2 bs1 = new BitSet2(5);
		bs1.set(4,true);
		BitSet2 bs2 = new BitSet2(3);
		
		assertEquals(5, bs1.length());
		assertEquals(3, bs2.length());
		assertEquals(8, BitSets.concatenate(bs1,bs2).length());
	}

}

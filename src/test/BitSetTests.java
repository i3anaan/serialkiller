package test;

import static org.junit.Assert.*;

import java.util.BitSet;

import org.junit.Test;

import util.BitSets;

public class BitSetTests {

	@Test
	public void test() {
		BitSet bs1 = new BitSet(5);
		bs1.set(4,true);
		BitSet bs2 = new BitSet(3);
		
		assertEquals(5, bs1.length());
		assertEquals(3, bs2.length());
		assertEquals(8, BitSets.concatenate(bs1,bs2).length());
	}

}

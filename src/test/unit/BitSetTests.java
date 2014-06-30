package test.unit;

import static org.junit.Assert.*;

import util.BitSet2;

import org.junit.Test;

public class BitSetTests {

	@Test
	public void test() {
		BitSet2 bs1 = new BitSet2(5);
		bs1.set(4,true);
		BitSet2 bs2 = new BitSet2(3);
		
		assertEquals(5, bs1.length());
		assertEquals(3, bs2.length());
		assertEquals(8, BitSet2.concatenate(bs1,bs2).length());
		
		BitSet2 bs3 = new BitSet2(8);
		BitSet2 bs4 = new BitSet2(1);
		BitSet2 bs5 = new BitSet2(9);
		bs5.set(8,false);
		bs4.set(0,false);
		assertEquals(bs5,BitSet2.concatenate(bs3, bs4));
		
		BitSet2 bs6 = new BitSet2();
		bs6.set(0,false);
		assertEquals(1,bs6.length());
		bs6.set(1,false);
		assertEquals(2,bs6.length());
		bs6.set(2,false);
		assertEquals(3,bs6.length());
		bs6.set(3,false);
		assertEquals(4,bs6.length());
		bs6.set(4,false);
		assertEquals(5,bs6.length());
		bs6.set(5,false);
		assertEquals(6,bs6.length());
	}
	
	@Test
	public void testContains() {
		BitSet2 bs = new BitSet2("00001001000011");
		assertEquals(4, bs.contains(new BitSet2("1001")));
		assertEquals(0, bs.contains(new BitSet2("00")));
		assertEquals(-1, bs.contains(new BitSet2("1111")));
		assertEquals(10, bs.contains(new BitSet2("0011")));
        assertEquals(-1, bs.contains(new BitSet2("1100")));
	}
	
	@Test
	public void testConstructors(){
		Byte[] arrNonNull = new Byte[]{1,3,5,6};
		Byte[] arrWithNull = new Byte[]{1,3,null,null};
		Byte[] arrOnlyNull = new Byte[]{null,null,null,null};
		BitSet2 bs1 = new BitSet2(arrNonNull);
		BitSet2 bs2 = new BitSet2(arrWithNull);
		BitSet2 bs3 = new BitSet2(arrOnlyNull);
		assertEquals(32,bs1.length());
		assertEquals(1,bs1.toByteArray()[0]);
		assertEquals(3,bs1.toByteArray()[1]);
		assertEquals(5,bs1.toByteArray()[2]);
		assertEquals(6,bs1.toByteArray()[3]);
		assertEquals(16,bs2.length());
		assertEquals(1,bs2.toByteArray()[0]);
		assertEquals(3,bs2.toByteArray()[1]);
		assertEquals(0,bs3.length());
	}

}

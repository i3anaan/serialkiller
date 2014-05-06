package test;

import static org.junit.Assert.*;
import link.Unit;

import org.junit.Test;

import util.BitSet2;
import util.Bytes;

public class UnitTest {

	@Test
	public void test() {
		byte b = -128;
		//System.out.println(Bytes.format((byte)-128));
		assertEquals("10000000",Bytes.format((byte)-128));
		Unit unit0 = new Unit(b);
		Unit unit1 = new Unit(b,true);
		Unit unit2 = new Unit(b,false);
		assertEquals(Bytes.format(b),Bytes.format(unit0.b));
		assertEquals(Bytes.format(b),Bytes.format(unit1.b));
		assertEquals(Bytes.format(b),Bytes.format(unit2.b));
		assertEquals("D"+Bytes.format(b),unit0.toString());
		assertEquals("F"+Bytes.format(b),unit1.toString());
		assertEquals("D"+Bytes.format(b),unit2.toString());
		
		BitSet2 bs = unit0.dataAsBitSet();
		System.out.println(bs);
		bs.set(8,false);
		assertEquals(Bytes.format(b),Bytes.format(Bytes.fromBitSet(bs, 0)));
		Unit unit3 = new Unit(Bytes.fromBitSet(bs, 0),bs.get(8));
		assertEquals(Bytes.format(b),Bytes.format(unit3.b));
		assertEquals("D"+Bytes.format(b),unit3.toString());
	}

}

package test;

import static org.junit.Assert.*;

import java.util.Arrays;

import link.HammingUnit;

import org.junit.Test;

import util.BitSet2;
import util.Bytes;
import util.encoding.HammingCode;

public class HammingUnitTest {

	@Test
	public void test() {
		HammingCode hc = new HammingCode(4);
		BitSet2 bs2 = new BitSet2(4);
		bs2.set(2,true);
		bs2.set(3,true);
		//System.out.println(hc.encode(bs2));
		//System.out.println(Bytes.format(hc.encode(bs2).toByteArray()[0]));
		//System.out.println(Bytes.format((byte)(hc.encode(bs2).toByteArray()[0] | 1)));
		HammingUnit hu = new HammingUnit(bs2,hc);
		//System.out.println(Bytes.format(hu.b));
		//System.out.println("Bitset: "+hu.getEncodedPayloadAsBitSet2());
		//System.out.println(Bytes.format(hu.getEncodedPayload()));
		//System.out.println(hu);
		assertEquals(bs2,hu.getDecodedPayloadAsBitSet2());
		assertEquals((byte)48,hu.getDecodedPayload());
		assertEquals(false,hu.isSpecial());
		//TODO some further testing, especially all the bitset2/byte stuff.
	}

}

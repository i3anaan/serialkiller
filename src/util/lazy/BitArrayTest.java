package util.lazy;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class BitArrayTest {
	private BitArray all_ones;
	private BitArray all_zeroes;
	private BitArray random;
	
	@Before
	public void setUp() {
		Random r = new Random(System.nanoTime());
		
		all_ones = new BitArray(new long[]{-1});
		all_zeroes = new BitArray(new long[]{0});
		random = new BitArray(new long[]{r.nextLong()});
	}
	
	@Test
	public void testToString() {
		assertEquals(66, all_ones.toString().length());
		assertEquals(66, all_zeroes.toString().length());
		assertEquals(66, random.toString().length());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testIndexOutOfBoundsException() {
	    all_ones.get(65);
	}
	
	@Test
	public void testByteConstructor() {
		assertEquals(all_ones, new BitArray(new byte[]{-1,-1,-1,-1,-1,-1,-1,-1}));
	}
	
	@Test
	public void testAlign() {
		assertEquals(0, BitArray.align(0));
		assertEquals(64, BitArray.align(1));
		assertEquals(64, BitArray.align(64));
		assertEquals(128, BitArray.align(65));
	}
}

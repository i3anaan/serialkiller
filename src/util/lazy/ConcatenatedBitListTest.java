package util.lazy;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConcatenatedBitListTest {
	@Test
	public void test() {
		BitArray a = new BitArray(new byte[]{-1});
		BitArray b = new BitArray(new byte[]{0});
		ConcatenatedBitList cbl = new ConcatenatedBitList(a, b);
				
		assertEquals(true, cbl.get(0));
		assertEquals(false, cbl.get(8));
	}
}

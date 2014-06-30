package link.angelmaker.codec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.BitSet2;

import com.google.common.base.Optional;

public class ReedSolomonTest {
	@Test
	public void testCodec() {
		
		ReedSolomonCodec codec = new ReedSolomonCodec();
		BitSet2 data = new BitSet2("10111101");
		assertEquals(Optional.of(data), codec.decode(codec.encode(data)));
	}

}

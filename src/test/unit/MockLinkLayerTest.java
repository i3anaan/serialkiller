package test.unit;

import static org.junit.Assert.*;

import java.util.Arrays;
import link.diag.MockLinkLayer;
import org.junit.Test;
import com.google.common.base.Charsets;
import common.Stack;

public class MockLinkLayerTest {

	@Test
	public void test() {
		Stack a = new Stack(), b = new Stack();
		MockLinkLayer mla = new MockLinkLayer(), mlb = new MockLinkLayer();
		
		a.linkLayer = mlb;
		b.linkLayer = mla;
		
		mla.start(a);
		mlb.start(b);
		
		byte[] test = "Testing!".getBytes(Charsets.UTF_8);
		
		mla.sendFrame(test);
		assertTrue(Arrays.equals(test, mlb.readFrame()));
		
		mlb.sendFrame(test);
		assertTrue(Arrays.equals(test, mla.readFrame()));
	}

}

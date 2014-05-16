package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.encoding.Hamming74;

public class HammingTest {

	@Test
	public void test() {
		System.out.println(Hamming74.encode((byte)11));
	}

}

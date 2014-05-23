package test.unit;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import util.BitSet2;
import util.encoding.HammingCode;

/**
 * RESULTS:
 * Error correction: up to and incluidng >6< bits of data.
 * Single Bit Error detection: up to and including >16< bits of data
 * Double Bit Error detection: up to and including >16< bits of data
 * 
 * 
 * 
 * @author I3anaan
 *
 */
public class HammingTest {

	@Test
	public void testCorrect() {
		for (int dataBitCount = 3; dataBitCount < 7; dataBitCount++) {
			HammingCode hc = new HammingCode(dataBitCount);
			for (BitSet2 data : buildBitSets(dataBitCount)) {
				BitSet2 encoded = hc.encode(data);
				assertEquals(data, hc.decode(encoded));
				assertEquals(data, hc.decode(hc.getCorrected(encoded)));
				for (int i = 0; i < dataBitCount; i++) {
					encoded.flip(i);
					assertEquals(data, hc.decode(hc.getCorrected(encoded)));
					encoded.flip(i);
				}
			}
		}
	}

	@Test
	public void testDetectSingleError() {
		for (int dataBitCount = 3; dataBitCount < 17; dataBitCount++) {
			HammingCode hc = new HammingCode(dataBitCount);
			for (BitSet2 data : buildBitSets(dataBitCount)) {
				BitSet2 encoded = hc.encode(data);
				assertEquals(data, hc.decode(encoded));
				assertEquals(data, hc.decode(hc.getCorrected(encoded)));
				for (int i = 0; i < dataBitCount; i++) {
					assertEquals(false, hc.hasError(encoded));
					encoded.flip(i);
					assertEquals(true, hc.hasError(encoded));
					encoded.flip(i);
				}
			}
		}
	}
	
	@Test
	public void testDetectDoubleError() {
		for (int dataBitCount = 3; dataBitCount < 17; dataBitCount++) {
			HammingCode hc = new HammingCode(dataBitCount);
			for (BitSet2 data : buildBitSets(dataBitCount)) {
				BitSet2 encoded = hc.encode(data);
				assertEquals(data, hc.decode(encoded));
				assertEquals(data, hc.decode(hc.getCorrected(encoded)));
				for (int i = 0; i < dataBitCount; i++) {
					assertEquals(false, hc.hasError(encoded));
					encoded.flip(i);
					for(int j=0;j<dataBitCount;j++){
						if(j!=i){
							encoded.flip(j);
							assertEquals(true, hc.hasError(encoded));
							encoded.flip(j);
						}
					}
					encoded.flip(i);
				}
			}
		}
	}

	public static ArrayList<BitSet2> buildBitSets(int length) {
		ArrayList<BitSet2> arr = new ArrayList<BitSet2>();

		BitSet2 bs0 = new BitSet2();
		bs0.set(0, false);
		BitSet2 bs1 = new BitSet2();
		bs1.set(0, true);
		buildBitSetsHelp(arr, bs0, length);
		buildBitSetsHelp(arr, bs1, length);

		return arr;
	}

	public static void buildBitSetsHelp(ArrayList<BitSet2> storeTo,
			BitSet2 current, int length) {
		BitSet2 bs0 = ((BitSet2) current.clone());
		bs0.set(bs0.length(), false);
		BitSet2 bs1 = ((BitSet2) current.clone());
		bs1.set(bs1.length(), true);
		if (bs0.length() == length && bs1.length() == length) {
			storeTo.add(bs0);
			storeTo.add(bs1);
		} else {
			buildBitSetsHelp(storeTo, bs0, length);
			buildBitSetsHelp(storeTo, bs1, length);
		}
	}
}

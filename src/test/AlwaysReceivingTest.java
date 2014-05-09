package test;

import link.HighSpeedHDXLinkLayer;
import phys.CleanStartPhysicalLayer;
import phys.DebouncePhysicalLayer;
import phys.DumpingPhysicalLayer;
import phys.HardwareLayer;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import util.Bytes;

public class AlwaysReceivingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HardwareLayer ll = new LptErrorHardwareLayer();

		float byte0 = 0;
		float byte1 = 0;
		float byte2 = 0;
		float byte3 = 0;

		byte old = -1;

		float total = 1;

		while (true) {
			byte b = ll.readByte();
			// if (b == old) continue;
			if (b == ll.readByte() && b == ll.readByte() && b == ll.readByte()
					) {
				if (total % 10 == 0) {
					System.out.printf(
							"b=0: %.6f  b=1: %.6f  b=2: %.6f  b=3: %.6f\n",
							byte0 / total, byte1 / total, byte2 / total, byte3
									/ total);
				}

				// Error alleen 0: >> 0.17%
				// b=0: 0.998282 b=1: 0.000845 b=2: 0.000872 b=3:
				// 0.000000
				// Error alleen 1: >> 0.2%
				// b=0: 0.001123 b=1: 0.998034 b=2: 0.000001 b=3:
				// 0.000841
				// Error alleen 2: >> 0.2%
				// b=0: 0.001121 b=1: 0.000001 b=2: 0.998037 b=3:
				// 0.000841
				// Error alleen 3: >> 0.22%
				// b=0: 0.000002 b=1: 0.001089 b=2: 0.001085 b=3:
				// 0.997823

				// Van 0 naar 3 naar 0:
				// b=0: 0.635655 b=1: 0.001686 b=2: 0.002898 b=3:
				// 0.359761
				// Debouncer:
				// b=0: 0.498842 b=1: 0.002717 b=2: 0.004015 b=3:
				// 0.494426
				// Met delay 100 ms en debouncer
				// b=0: 0.253618 b=1: 0.248436 b=2: 0.246891 b=3:
				// 0.251036
				// Met delay 100 ms zonder debouncer
				// b=0: 0.504856 b=1: 0.000502 b=2: 0.000504 b=3:
				// 0.494137

				// Met 1-en ertussen
				// b=0: 0.309392 b=1: 0.511864 b=2: 0.000414 b=3:
				// 0.178329
				// Debouncer:
				// b=0: 0.309503 b=1: 0.486167 b=2: 0.000491 b=3:
				// 0.203839

				// Normaal (vrijwel, 6 decimalen nauwkeurig) foutloos

				// Quadripple debouncer:
				// b=0: 0.000000 b=1: 0.000008 b=2: 0.000008 b=3:
				// 0.999983
				// Quadripple debouncer 00/11/00
				// b=0: 0.743181 b=1: 0.001287 b=2: 0.017524 b=3: 0.238008

				// Dubbel debouncer:
				// b=0: 0.000000 b=1: 0.000014 b=2: 0.000006 b=3:
				// 0.999981
				// Enkel debouncer:
				// b=0: 0.000001 b=1: 0.001094 b=2: 0.001086 b=3:
				// 0.997819

				if (b == 0) {
					byte0++;
				} else if (b == 1) {
					byte1++;
				} else if (b == 2) {
					byte2++;
				} else if (b == 3) {
					byte3++;
				}
				total++;
				old = b;
			}
		}
	}
}

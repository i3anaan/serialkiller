package bench;

import link.AckingLinkLayer;
import link.DumpingLinkLayer;
import link.LinkLayer;
import phys.DumpingPhysicalLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;

public class BandwidthTestBench {
    static final long DURATION = 60000; // 60.000 ms = 1 minute

    public static void main(String[] args) {
        if (args.length != 1 || !(args[0].equals("send") || args[0].equals("receive"))) {
            System.err.println("Invalid arguments.\nUse 'send' or 'receive'.");
        } else {
            PhysicalLayer phys = new LptHardwareLayer();
            LinkLayer link = new AckingLinkLayer(phys);

            System.out.println("STACK: " + link + "\n");

            // Reset line
            phys.sendByte((byte) 0);

            System.out.printf("Please wait %d seconds...\n\n", DURATION / 1000);

            if (args[0].equals("send")) {
                // Just spammin'
                while (true) {
                    for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
                        link.sendByte(i);
                    }
                }
            } else if (args[0].equals("receive")) {
                long start = Long.MAX_VALUE - DURATION - 1;
                int num = 0;
                byte old = Byte.MAX_VALUE;

                while (System.currentTimeMillis() < start + DURATION) {
                    byte in = link.readByte();
                    if (in != old) {
                        if (start > System.currentTimeMillis()) {
                            start = System.currentTimeMillis();
                        }
                        num++;
                    }
                }

                System.out.printf("Average bandwidth: %d\n", num / DURATION / 1000);
            }
        }
    }
}

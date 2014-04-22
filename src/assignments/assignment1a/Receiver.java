package assignments.assignment1a;

import lpt.Lpt;

public class Receiver {
    public static void main(String[] args) {
        Lpt lpt = new Lpt();
        boolean loop_continue = true;
        int oldIn = 0;

        while(loop_continue) {
            int in = lpt.readLPT();

            if (in != oldIn) {
                System.out.println(in + " \t=\t" + Integer.toBinaryString(in));
            }

            oldIn = in;
        }
    }
}

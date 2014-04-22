package assignments.assignment1b;

import lpt.ErrorLpt;
import lpt.Lpt;

public class Receiver {
    public static void main(String[] args) {
        Lpt lpt = new ErrorLpt();
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

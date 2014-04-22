package assignments.assignment1b;

import lpt.ErrorLpt;
import lpt.Lpt;

public class Sender {
    public static void main(String[] args) {
        Lpt lpt = new ErrorLpt();
        boolean loop_continue = true;
        int out = 0;

        try {
            out = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("The first argument should be a valid integer.");
            loop_continue = false;
        } catch (NumberFormatException e) {
            System.err.println("The first argument should be a valid integer.");
            loop_continue = false;
        }

        while(loop_continue) {
            lpt.writeLPT(out);
        }
    }
}

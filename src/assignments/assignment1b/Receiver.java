package assignments.assignment1b;

import lpt.ErrorLpt;
import lpt.Lpt;

public class Receiver {
    public static void main(String[] args) {
        Lpt lpt = new ErrorLpt();
        boolean loop_continue = true;
        int out = 0;
        int errors = 0;
        int steps = 0;

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
            steps++;
            int in = lpt.readLPT();

            if (in != out) {
                errors++;
                System.out.println(errors + "/" + steps + "\t= " + (errors/steps));
            }
        }
    }
}

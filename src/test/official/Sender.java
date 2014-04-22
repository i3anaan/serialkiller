package test.official;

import lpt.Lpt;

public class Sender {
    public static void main(String[] args) {
        Lpt lpt = new Lpt();
        boolean loop_continue = true;
        int out = 0;

        try {
            out = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Give an argument!");
            loop_continue = false;
        } catch (NumberFormatException e) {
            System.err.println("Give a valid number!");
            loop_continue = false;
        }

        while(loop_continue) {
            lpt.writeLPT(out);
        }
    }
}

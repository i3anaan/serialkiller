package test.official;

import lpt.Lpt;

public class Receiver {
    public static void main(String[] args) {
        Lpt lpt = new Lpt();
        boolean loop_continue = true;

        while(loop_continue) {
            int in = lpt.readLPT();
            System.out.println(in);
        }
    }
}

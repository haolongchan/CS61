package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                }

                break;
            // TODO: FILL THE REST IN


            default:
                if (args.length != 0) {
                    System.out.println("No command with that name exists.");
                    System.exit(0);
                }
                System.out.println("Please enter a command.");
                System.exit(0);
        }
    }
}

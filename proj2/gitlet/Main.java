package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.setup()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.addFile(args[1])) {

                    System.exit(0);
                }
                break;
            // TODO: FILL THE REST IN

            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.removeFile(args[1])) {
                    System.exit(0);
                }
                break;
            case "commit":
                if (args.length != 2) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (args[1].isBlank()) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (!Repository.PrepareForCommit(args[1])) {
                    System.exit(0);
                }
                break;

            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.log();
                break;

            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.globallog();
                break;

            case "find":
                if (args.length <= 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.find(args[1])) {
                    System.out.println("Found no commit with that message.");
                    System.exit(0);
                }
                break;

            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.status();
                break;

            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.createBranch(args[1])) {
                    System.out.println("A branch with that name already exists.");
                    System.exit(0);
                }
                break;
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

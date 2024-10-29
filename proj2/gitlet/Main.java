package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Haolong
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {

        // test
//        Repository.merge("other");
          Repository.merge("b2");
//        System.exit(0);

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
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
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.addFile(args[1])) {

                    System.exit(0);
                }
                break;

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
                if (!Repository.prepareForCommit(args[1])) {
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
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if(!Repository.removeBranch(args[1])) {
                    System.exit(0);
                }
                break;

            case "checkout":
                if (args[1].equals("--")) {
                    if (!Repository.checkoutName(args[2])) {
                        System.out.println("File does not exist in that commit.");
                        System.exit(0);
                    }
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                    }
                    Repository.checkoutID(args[1], args[3]);
                    System.exit(0);
                }
                break;

            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.reset(args[1]);
                break;

            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.merge(args[1]);
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

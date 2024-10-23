package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File blobs = join(GITLET_DIR, "blobs");

    /* contain files recording secure hash */
    public static final File branches = join(GITLET_DIR, "branches");
    public static final File stages = join(GITLET_DIR, "stages");
    public static final File ADDFILE = join(stages, "addfile");
    public static final File REMOVEFILE = join(stages, "removefile");
//    public static final File INDEXFILE = join(commits, "indexfile");
//    public static final File BLOBINDEX = join(blobs, "blobindex"); index -> hash: saved in commit and blob files

    /* recording secure hash */
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File MASTER = join(branches, "master");
    public static final File CURRENT = join(branches, "current");

//    public static String commitHash = "";
//    public static String blobHash = "";

    public static Commit head;


    private static class pseudoCommit {
        static String message;
        static String timestamp;
        static String parentHash;
        static String currentHash;
        static LinkedList<String> RefToBlobs;
        private pseudoCommit(String msg, String tms, String prtH, String Ha, LinkedList<String> ref) {
            message = msg;
            timestamp = tms;
            parentHash = prtH;
            currentHash = Ha;
            RefToBlobs = ref;
        }
    }


    public static boolean setup() {
        try {
            if (!GITLET_DIR.mkdir()) {
                return false;
            }
            commits.mkdir();
            branches.mkdir();
            stages.mkdir();
            blobs.mkdir();
            ADDFILE.createNewFile();
            REMOVEFILE.createNewFile();
//        INDEXFILE.createNewFile();
//        BLOBINDEX.createNewFile();
            HEAD.createNewFile();
            MASTER.createNewFile();
            CURRENT.createNewFile();
            writeContents(CURRENT, "master");
//        writeContents(INDEXFILE, String.valueOf(commitHash));
//        writeContents(BLOBINDEX, String.valueOf(blobHash));
            Commit.addAsSetup("initial commit", null);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /*
    * add the file to blog directory
    * add the secure hash1 of the file to addfile of stage
    * */
    public static boolean addFile(String fileName) {
        try {
            File selected = join(CWD, fileName);
            if (!selected.exists()) {
                System.out.println("File does not exist.");
                return false;
            }
            boolean existence = false;
            if (!blobs.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return false;
            }
//        blobHash = readContentsAsString(BLOBINDEX);
//        File blobfile = join(blobs, String.valueOf(blobHash));
//        byte[] contents = readContents(toadd);

            String fileHash = sha1(readContentsAsString(selected));
            LinkedList<String> addContents = readAddStage();
            for (String s : addContents) {
                if (s.equals(fileHash)) {
                    existence = true;
                    return false;
                }
            }
            if (!existence) {
                appendContents(ADDFILE, fileHash, "@");
                File blob = join(blobs, fileHash);
                blob.createNewFile();
                writeObject(blob, readContents(selected));
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean removeFile(String fileName) {
        try {
            File toremove = join(CWD, fileName);
            if (!toremove.exists()) {
                System.out.println("No reason to remove the file.");
            }
            if (!toremove.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return false;
            }

            String fileHash = sha1(readContentsAsString(toremove));
            LinkedList<String> addContents = readAddStage();

            for (String s : addContents) {
                if (s.equals(fileHash)) {
                    writeContents(ADDFILE, "");
                    for (String content : addContents) {
                        if (!content.equals(fileHash)) {
                            appendContents(ADDFILE, content, "@");
                        }
                        else {
                            appendContents(REMOVEFILE, content, "@");
                        }
                    }
                    return true;
                }
            }
            System.out.println("No reason to remove the file.");
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /*
    * get the newest commit and return
    * */
    public static pseudoCommit readCommit(File commits) {
        try {
            String contents = readContentsAsString(commits);
            int size = contents.length();
            String message = "";
            String timestamp = "";
            String parentHash = "";
            String currentHash = "";
            LinkedList<String> RefToBlobs = new LinkedList<>();
            int index = -1;
            for (int i = 0; i < size; i++) {
                if (contents.charAt(i) == '@') {
                    index = i + 1;
                    break;
                }
                message += contents.charAt(i);
            }
            for (int i = index; i < size; i++) {
                if (contents.charAt(i) == '@') {
                    index = i + 1;
                    break;
                }
                timestamp += contents.charAt(i);
            }
            for (int i = index; i < size; i++) {
                if (contents.charAt(i) == '@') {
                    index = i + 1;
                    break;
                }
                parentHash += contents.charAt(i);
            }
            for (int i = index; i < size; i++) {
                if (contents.charAt(i) == '@') {
                    index = i + 1;
                    break;
                }
                currentHash += contents.charAt(i);
            }
            String tmp = "";
            for (int i = index; i < size; i++) {
                if (contents.charAt(i) == '$') {
                    RefToBlobs.add(tmp);
                    tmp = "";
                }
                else {
                    tmp += contents.charAt(i);
                }
            }
            return new pseudoCommit(message, timestamp, parentHash, currentHash, RefToBlobs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void log() {
        try {
            String currentHash = readContentsAsString(HEAD);
            File currentCommit = join(commits, currentHash);
            pseudoCommit currentContents = readCommit(currentCommit);
            System.out.println("===");
            System.out.println("commit " + currentContents.currentHash);
            System.out.println("Date: " + currentContents.timestamp);
            System.out.println(currentContents.message);
            System.out.println("");
            while (currentContents.parentHash.length() != 0) {
                currentCommit = join(commits, currentContents.parentHash);
                currentContents = readCommit(currentCommit); // TODO: Issue1
                System.out.println("===");
                System.out.println("commit " + currentContents.currentHash);
                System.out.println("Date: " + currentContents.timestamp);
                System.out.println(currentContents.message);
                System.out.println("");
            }
            // TODO: ignoring the existence of Merge method
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void globallog() {
        try {
            List<String> fileName = plainFilenamesIn(commits);
            int size = fileName.size();
            for (int i = 0; i < size; i++) {
                String name = fileName.get(i);
                File currentCommit = join(commits, name);
                pseudoCommit currentContents = readCommit(currentCommit);
                System.out.println("===");
                System.out.println("commit " + currentContents.currentHash);
                System.out.println("Date: " + currentContents.timestamp);
                System.out.println(currentContents.message);
                System.out.println("");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean find(String msg) {
        try {
            List<String> fileName = plainFilenamesIn(commits);
            int size = fileName.size();
            boolean found = false;
            for (int i = 0; i < size; i++) {
                String name = fileName.get(i);
                File currentCommit = join(commits, name);
                pseudoCommit currentContents = readCommit(currentCommit);
                if (currentContents.message.equals(msg)) {
                    System.out.println(currentContents.currentHash);
                    found = true;
                }
            }
            return found;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void status() {
        List<String> branchName = plainFilenamesIn(branches);
        int size = branchName.size();
        Collections.sort(branchName);
        String curbranch = readContentsAsString(CURRENT);
        System.out.println("=== Branches ===");
        for (int i = 0; i < size; ++i) {
            if (branchName.get(i).equals(CURRENT)) {
                continue;
            }
            if (branchName.get(i).equals(curbranch)) {
                System.out.println("*" + curbranch);
            }
            else {
                System.out.println(branchName.get(i));
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        LinkedList<String> stagedHash = readAddStage();
        List<String> stageName = plainFilenamesIn(CWD);
        size = stageName.size();
        Collections.sort(stageName);
        List<String> stage = new ArrayList<>();
        List<String> remove = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < stagedHash.size(); j++) {
                if (stagedHash.get(j).equals(sha1(readContentsAsString(join(CWD, stageName.get(i)))))) {
                    stage.add(stageName.get(i));
                }
            }
        }
        LinkedList<String> removedHash = readRemoveStage();
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < removedHash.size(); j++) {
                if (removedHash.get(j).equals(sha1(readContentsAsString(join(CWD, stageName.get(i)))))) {
                    remove.add(stageName.get(i)); // TODO: matching wrong file
                    stage.add(stageName.get(i));
                }
            }
        }
        Collections.sort(remove);
        Collections.sort(stage);
        size = stage.size();
        for (int i = 0; i < size; ++i) {
            System.out.println(stage.get(i));
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        size = remove.size();
        for (int i = 0; i < size; ++i) {
            System.out.println(remove.get(i));
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println("");
        System.out.println("=== Untracked Files ===");

    }

    public static LinkedList<String> readAddStage() {
        LinkedList<String> stages = new LinkedList<>();
        String content = readContentsAsString(ADDFILE);
        int size = content.length();
        String singleHash = "";
        for (int i = 0; i < size; i++) {
            if (content.charAt(i) != '@') {
                singleHash += content.charAt(i);
            }
            else {
                stages.add(singleHash);
                singleHash = "";
            }
        }
        return stages;
    }

    public static LinkedList<String> readRemoveStage() {
        LinkedList<String> stages = new LinkedList<>();
        String content = readContentsAsString(REMOVEFILE);
        int size = content.length();
        String singleHash = "";
        for (int i = 0; i < size; i++) {
            if (content.charAt(i) != '@') {
                singleHash += content.charAt(i);
            }
            else {
                stages.add(singleHash);
                singleHash = "";
            }
        }
        return stages;
    }

    public static boolean PrepareForCommit(String message) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        LinkedList<String> addContents = readAddStage();
        if (addContents.isEmpty() || readContentsAsString(ADDFILE).length() == 0) {
            System.out.println("No changes added to the commit.");
            return false;
        }
        String parentHash = readContentsAsString(HEAD);
        Commit.add(message, parentHash, addContents);
        return true;
    }

    /*
    * create a commit
    * 1. Linked to parent
    * 2. save commit to file
    * 3. update head and current branch
    * 4. clear stage files
    * */
    public static void createcommits(Commit arg) {
        
        try {
            String timestamp = Commit.formatDate(arg.getTimestamp());
            LinkedList<Commit> commitlist = new LinkedList<>();
            String parentHash = readContentsAsString(HEAD);
            arg.parentHash = parentHash;//commit has been created locally


            String currentHash = "";
            if (arg.getRefToBlobs() == null) {
                currentHash = sha1(arg.getMessage(), timestamp, parentHash);
            }
            else {
                currentHash = sha1(arg.getMessage(), timestamp, arg.getRefToBlobs().toString(), parentHash);
            }
            arg.hash = currentHash;
//        commitHash = readContentsAsString(INDEXFILE);

            /* save secure hash to head */
            File commitFile = join(commits, currentHash);
            commitFile.createNewFile();
//        writeObject(commitFile, (Serializable) arg);

            /* TODO: message + timestamp + parentHash + currentHash + reference to blobs */
            appendContents(commitFile, arg.getMessage(), "@", timestamp, "@", parentHash, "@", currentHash, "@");
            if (arg.getRefToBlobs() != null) {
                for (String blobs : arg.getRefToBlobs()) {
                    appendContents(commitFile, blobs, "$");
                }
            }
            writeContents(HEAD, currentHash);

            /* get current branch and save secure hash to it */
            String currentBranch = readContentsAsString(CURRENT);
            File currentBranchFile = join(branches, currentBranch);
            writeContents(currentBranchFile, currentHash);
//        head = new Commit(arg.getMessage(), arg.getTimestamp(), arg.getRefToBlobs(), currentHash);
//        if (arg.getRefToBlobs() == null) {
//            return;
//        }
//        for (int ref : arg.getRefToBlobs()) {
//            writeContents(commitFile, ref, "@", String.valueOf(ref), "@\n");
//        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean createBranch(String branchName) {
        try {
            File branchFile = join(branches, branchName);
            if (branchFile.exists()) {
                return false;
            }
            String curhash = readContentsAsString(HEAD);
            branchFile.createNewFile();
            writeContents(branchFile, curhash);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createcommitassetup(Commit arg) {
        try {
            String timestamp = Commit.formatDate(arg.getTimestamp());
            String currentHash = "";
            if (arg.getRefToBlobs() == null) {
                currentHash = sha1(arg.getMessage(), timestamp);
            }
            else {
                currentHash = sha1(arg.getMessage(), timestamp, arg.getRefToBlobs().toString());
            }
            File commitFile = join(commits, currentHash);
            commitFile.createNewFile();
//        writeObject(commitFile, (Serializable) arg);
            appendContents(commitFile, arg.getMessage(), "@", timestamp, "@@", currentHash, "@");
            // TODO: for all commitfile writing, '@' is used to split message, timestamp, parentHash and hash, while '$' is used to split refs, beginning with '@' and ending with '$'
            writeContents(HEAD, currentHash);
            writeContents(MASTER, currentHash);
            writeContents(CURRENT, "master");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


}

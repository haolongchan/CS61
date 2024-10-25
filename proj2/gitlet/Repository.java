package gitlet;

import java.io.File;
import java.util.*;
import java.io.IOException;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File BLOBS = join(GITLET_DIR, "blobs");

    /* contain files recording secure hash */
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    public static final File STAGES = join(GITLET_DIR, "stages");
    public static final File ADDFILE = join(STAGES, "addfile");
    public static final File REMOVEFILE = join(STAGES, "removefile");

    /* recording secure hash */
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File MASTER = join(BRANCHES, "master");
    public static final File CURRENT = join(BRANCHES, "current");

//    public static String commitHash = "";
//    public static String blobHash = "";

    private static class PseudoCommit {
        static String message;
        static String timestamp;
        static String parentHash;
        static String currentHash;
        static LinkedList<String> refToBlobs;
        static LinkedList<String> fileLocation;
        private PseudoCommit(String msg, String tms, String prtH, String ha,
                             LinkedList<String> ref, LinkedList<String> loc) {
            message = msg;
            timestamp = tms;
            parentHash = prtH;
            currentHash = ha;
            refToBlobs = ref;
            fileLocation = loc;
        }
    }


    public static boolean setup() {
        try {
            if (!GITLET_DIR.mkdir()) {
                return false;
            }
            COMMITS.mkdir();
            BRANCHES.mkdir();
            STAGES.mkdir();
            BLOBS.mkdir();
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
        } catch (IOException e) {
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
            if (!BLOBS.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                return false;
            }
            String fileHash = sha1(readContentsAsString(selected), fileName);
            LinkedList<String>[] addContents = readAddStage();

            List<String> blobName = plainFilenamesIn(BLOBS);
            for (String s : blobName) {
                if (s.equals(fileHash)) {
                    if (readContentsAsString(join(BLOBS, fileHash)).length() != 1) {
                        return false;
                    }
                }
            }

            if (!addContents[0].isEmpty()) {
                for (String s : addContents[0]) {
                    if (s.equals(fileHash)) {
                        return false;
                    }
                }
            }
            File blob = join(BLOBS, fileHash);
            blob.createNewFile();
            writeContents(blob, readContentsAsString(selected));
//            for (String s : removeContents) {
//                if (s.equals(fileHash)) {
//                    return true;
//                }
//            }
            appendContents(ADDFILE, fileHash, ":", fileName, "@");
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean removeFile(String fileName) {
        File toremove = join(CWD, fileName);
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        if (!toremove.exists()) {
            PseudoCommit commitContents = readCommit(join(COMMITS, readContentsAsString(HEAD)));
            int size = commitContents.fileLocation.size();
            for (int i = 0; i < size; i++) {
                if (commitContents.fileLocation.get(i).equals(fileName)) {
                    restrictedDelete(toremove);
                    appendContents(REMOVEFILE, "^@", fileName, "@");
                    writeContents(join(BLOBS, commitContents.refToBlobs.get(i)), "^");
                    return true;
                }
            }

            System.out.println("No reason to remove the file.");
            return false;
        }
        String fileHash = sha1(readContentsAsString(toremove), fileName);
        LinkedList<String>[] addContents = readAddStage();
        LinkedList<String>[] removeContents = readRemoveStage();
        if (!removeContents[0].isEmpty()) {
            for (String s : removeContents[0]) {
                if (s.equals(fileHash)) {
                    System.out.println("No reason to remove the file.");
                    return false;
                }
            }
            appendContents(REMOVEFILE, fileHash, "@", fileName, "@");
            PseudoCommit commitContents = readCommit(join(COMMITS, readContentsAsString(HEAD)));
            for (String s : commitContents.refToBlobs) {
                if (s.equals(fileHash)) {
                    restrictedDelete(toremove);
                    writeContents(join(BLOBS, fileHash), "^");

                    return true;
                }
            }
            return true;
        }
        if (addContents[0].isEmpty()) {
            PseudoCommit contents = readCommit(join(COMMITS, readContentsAsString(HEAD)));
            for (String s : contents.refToBlobs) {
                if (s.equals(fileHash)) {
                    appendContents(REMOVEFILE, fileHash, "@", fileName, "@");
                    restrictedDelete(toremove);
                    writeContents(join(BLOBS, fileHash), "^");
                    return true;
                }
            }
            System.out.println("No reason to remove the file.");
            return false;
        }

        for (String s : addContents[0]) {
            if (s.equals(fileHash)) {
                appendContents(REMOVEFILE, fileHash, "@", fileName, "@");
                return true;
            }
        }
        System.out.println("No reason to remove the file.");
        return false;
    }


    /*
    * get the newest commit and return
    * */
    public static PseudoCommit readCommit(File commit) {
        String contents = readContentsAsString(commit);
        int size = contents.length();
        String message = "";
        String timestamp = "";
        String parentHash = "";
        String currentHash = "";
        LinkedList<String> refToBlob = new LinkedList<>();
        LinkedList<String> fileLoc = new LinkedList<>();
        int index = -1;

        /* read message */
        for (int i = 0; i < size; i++) {
            if (contents.charAt(i) == '@') {
                index = i + 1;
                break;
            }
            message += contents.charAt(i);
        }

        /* read timestamp */
        for (int i = index; i < size; i++) {
            if (contents.charAt(i) == '@') {
                index = i + 1;
                break;
            }
            timestamp += contents.charAt(i);
        }

        /* read parent hash */
        for (int i = index; i < size; i++) {
            if (contents.charAt(i) == '@') {
                index = i + 1;
                break;
            }
            parentHash += contents.charAt(i);
        }

        /* read current Hash */
        for (int i = index; i < size; i++) {
            if (contents.charAt(i) == '@') {
                index = i + 1;
                break;
            }
            currentHash += contents.charAt(i);
        }
        String tmp = "";

        /* read reference to blob */
        for (int i = index; i < size; i++) {
            if (contents.charAt(i) == '!') {
                index = i + 1;
                tmp = "";
                break;
            }
            if (contents.charAt(i) == '$') {
                refToBlob.add(tmp);
                tmp = "";
            } else {
                tmp += contents.charAt(i);
            }
        }

        /* read file name */
        for (int i = index; i < size; i++) {
            if (contents.charAt(i) == '@') {
                fileLoc.add(tmp);
                tmp = "";
            } else {
                tmp += contents.charAt(i);
            }
        }
        return new PseudoCommit(message, timestamp, parentHash,
                currentHash, refToBlob, fileLoc);
    }

    public static void log() {
        String currentHash = readContentsAsString(HEAD);
        File currentCommit = join(COMMITS, currentHash);
        PseudoCommit currentContents = readCommit(currentCommit);
        System.out.println("===");
        System.out.println("commit " + currentContents.currentHash);
        System.out.println("Date: " + currentContents.timestamp);
        System.out.println(currentContents.message);
        System.out.println("");
        while (currentContents.parentHash.length() != 0) {
            currentCommit = join(COMMITS, currentContents.parentHash);
            currentContents = readCommit(currentCommit);
            System.out.println("===");
            System.out.println("commit " + currentContents.currentHash);
            System.out.println("Date: " + currentContents.timestamp);
            System.out.println(currentContents.message);
            System.out.println("");
        }
    }

    public static void globallog() {
        List<String> fileName = plainFilenamesIn(COMMITS);
        int size = fileName.size();
        for (int i = 0; i < size; i++) {
            String name = fileName.get(i);
            File currentCommit = join(COMMITS, name);
            if (readContentsAsString(currentCommit).length() == 0) {
                continue;
            }
            PseudoCommit currentContents = readCommit(currentCommit);
            System.out.println("===");
            System.out.println("commit " + currentContents.currentHash);
            System.out.println("Date: " + currentContents.timestamp);
            System.out.println(currentContents.message);
            System.out.println("");
        }
    }

    public static boolean find(String msg) {
        List<String> fileName = plainFilenamesIn(COMMITS);
        int size = fileName.size();
        boolean found = false;
        for (int i = 0; i < size; i++) {
            String name = fileName.get(i);
            File currentCommit = join(COMMITS, name);
            PseudoCommit currentContents = readCommit(currentCommit);
            if (currentContents.message.equals(msg)) {
                System.out.println(currentContents.currentHash);
                found = true;
            }
        }
        return found;
    }

    public static void status() {
        List<String> branchName = plainFilenamesIn(BRANCHES);
        int size = branchName.size();
        Collections.sort(branchName);
        String curbranch = readContentsAsString(CURRENT);
        System.out.println("=== Branches ===");
        for (int i = 0; i < size; ++i) {
            if (branchName.get(i).equals("current")) {
                continue;
            }
            if (branchName.get(i).equals(curbranch)) {
                System.out.println("*" + curbranch);
            } else {
                System.out.println(branchName.get(i));
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        LinkedList<String>[] stagedHash = readAddStage();
        LinkedList<String>[] removedHash = readRemoveStage();
        List<String> stage = new ArrayList<>();
        List<String> remove = new ArrayList<>();
        size = stagedHash[1].size();
        for (int i = 0; i < size; i++) {
            stage.add(stagedHash[1].get(i));
        }
        size = removedHash[1].size();
        for (int i = 0; i < size; i++) {
            remove.add(removedHash[1].get(i));
        }
        List<String> common = new ArrayList<>(stage);
        common.retainAll(remove);
        stage.removeAll(common);
        remove.removeAll(common);
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

    public static LinkedList<String>[] readAddStage() {
        LinkedList<String>[] stage = new LinkedList[2];
        stage[0] = new LinkedList<>();
        stage[1] = new LinkedList<>();
        String content = readContentsAsString(ADDFILE);
        int size = content.length();
        String singleHash = "";
        String singleName = "";
        boolean readHash = true;
        for (int i = 0; i < size; i++) {

            if (readHash) {
                if (content.charAt(i) != ':') {
                    singleHash += content.charAt(i);
                } else {
                    stage[0].add(singleHash);
                    singleHash = "";
                    readHash = false;
                }
            } else {
                if (content.charAt(i) != '@') {
                    singleName += content.charAt(i);
                } else {
                    stage[1].add(singleName);
                    singleName = "";
                    readHash = true;
                }
            }



        }
        return stage;
    }

    public static LinkedList<String>[] readRemoveStage() {
        LinkedList<String>[] stage = new LinkedList[2];
        stage[0] = new LinkedList<>();
        stage[1] = new LinkedList<>();
        String content = readContentsAsString(REMOVEFILE);
        int size = content.length();
        String singleHash = "";
        String singleName = "";
        boolean readHash = true;
        for (int i = 0; i < size; i++) {
            if (readHash) {
                if (content.charAt(i) != '@') {
                    singleHash += content.charAt(i);
                } else {
                    stage[0].add(singleHash);
                    singleHash = "";
                    readHash = false;
                }
            } else {
                if (content.charAt(i) != '@') {
                    singleName += content.charAt(i);
                } else {
                    stage[1].add(singleName);
                    singleName = "";
                    readHash = true;
                }
            }
        }
        return stage;
    }

    public static boolean prepareForCommit(String message) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        LinkedList<String>[] addContents = readAddStage();
        LinkedList<String>[] removeContents = readRemoveStage();
        if (addContents[0].isEmpty() && removeContents[0].isEmpty()) {
            System.out.println("No changes added to the commit.");
            return false;
        }
        String parentHash = readContentsAsString(HEAD);
        Commit.add(message, parentHash, addContents, removeContents);
        return true;
    }

    private static void partOfCommits(Commit arg) {
        int size = arg.getRmHash().size();
        PseudoCommit parentContents = readCommit(join(COMMITS, readContentsAsString(HEAD)));
        String currentHash = sha1(arg.getMessage(), arg.getTimestamp(),
                arg.getRmHash(), arg.getRmFile());
        int psize = parentContents.refToBlobs.size();
        for (int i = 0; i < size; i++) {
            boolean exist = false;
            for (int j = 0; j < psize; j++) {
                if (arg.getRmHash().get(i).equals("^")) {
                    continue;
                }
                if (parentContents.refToBlobs.get(j).equals(arg.getRmHash().get(i))) {
                    exist = true;
                    parentContents.refToBlobs.remove(j);
                    parentContents.fileLocation.remove(j);
                    psize--;
                    if (join(CWD, arg.getRmFile().get(i)).exists()) {
                        restrictedDelete(join(CWD, arg.getRmFile().get(i)));
                        writeContents(join(BLOBS, arg.getRmHash().get(i)), "^");
                    }
                }
            }
            if (!exist) {
                writeContents(REMOVEFILE, "");
                System.out.println("No changes added to the commit.");
                return;
            }
        }
        writeContents(REMOVEFILE, "");
        writeContents(join(COMMITS, readContentsAsString(HEAD)), parentContents.message,
                "@", parentContents.timestamp, "@", parentContents.parentHash, "@",
                parentContents.currentHash, "@");
        if (parentContents.refToBlobs.size() > 0) {
            for (String s : parentContents.refToBlobs) {
                appendContents(join(COMMITS, readContentsAsString(HEAD)), s, "$");
            }
            appendContents(join(COMMITS, readContentsAsString(HEAD)),  "!");
            for (String s : parentContents.fileLocation) {
                appendContents(join(COMMITS, readContentsAsString(HEAD)), s, "@");
            }
        }
    }

    private static void rmOfCommits(Commit arg, List<String> removed) {
        int size = removed.size();
        PseudoCommit parentContents = readCommit(join(COMMITS, readContentsAsString(HEAD)));
        int psize = parentContents.refToBlobs.size();
        for (int i = 0; i < size; i++) {
            boolean exist = false;
            for (int j = 0; j < psize; j++) {
                if (parentContents.refToBlobs.get(j).equals("^")) {
                    continue;
                }
                if (parentContents.fileLocation.get(j).equals(removed.get(i))) {
                    exist = true;
                    String deleteHash = parentContents.refToBlobs.get(j);
                    parentContents.refToBlobs.remove(j);
                    parentContents.fileLocation.remove(j);
                    psize--;
                    if (join(CWD, removed.get(i)).exists()) {
                        restrictedDelete(join(CWD, removed.get(i)));
                        writeContents(join(BLOBS, deleteHash), "^");
                    }
                }
            }
            if (!exist) {
                writeContents(REMOVEFILE, "");
                writeContents(ADDFILE, "");
                System.out.println("No changes added to the commit.");
                return;
            }
        }
        writeContents(REMOVEFILE, "");
        writeContents(ADDFILE, "");
        writeContents(join(COMMITS, readContentsAsString(HEAD)), parentContents.message,
                "@", parentContents.timestamp, "@", parentContents.parentHash, "@",
                parentContents.currentHash, "@");
        if (parentContents.refToBlobs.size() > 0) {
            for (String s : parentContents.refToBlobs) {
                appendContents(join(COMMITS, readContentsAsString(HEAD)), s, "$");
            }
            appendContents(join(COMMITS, readContentsAsString(HEAD)),  "!");
            for (String s : parentContents.fileLocation) {
                appendContents(join(COMMITS, readContentsAsString(HEAD)), s, "@");
            }
        }
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
            String parentHash = readContentsAsString(HEAD);
            arg.addparentHash(parentHash);
            String currentHash = "";
            if (arg.getRefToBlobs() == null) {
                partOfCommits(arg);
                currentHash = sha1(arg.getMessage(), arg.getTimestamp(),
                        arg.getRmHash(), arg.getRmFile());
                File newCommit = join(COMMITS, currentHash);
                newCommit.createNewFile();
                writeContents(newCommit, arg.getMessage(), "@", timestamp, "@",
                        parentHash, "@", currentHash, "@$!@");
                writeContents(ADDFILE, "");
                writeContents(REMOVEFILE, "");
                writeContents(HEAD, currentHash);
                writeContents(join(BRANCHES, readContentsAsString(CURRENT)), currentHash);
                return;
            }
            LinkedList<String>[] stagedHash = readAddStage();
            LinkedList<String>[] removedHash = readRemoveStage();
            List<String> staged = new ArrayList<>();
            List<String> removed = new ArrayList<>();
            int size = stagedHash[1].size();
            for (int i = 0; i < size; i++) {
                staged.add(stagedHash[1].get(i));
            }
            size = removedHash[1].size();
            for (int i = 0; i < size; i++) {
                removed.add(removedHash[1].get(i));
            }
            List<String> common = new ArrayList<>(staged);
            common.retainAll(removed);
            staged.removeAll(common);
            removed.removeAll(common);
            if (removed.size() > 0) {
                rmOfCommits(arg, removed);
                if (staged.isEmpty()) {
                    currentHash = sha1(arg.getMessage(), timestamp, parentHash);
                    File newCommit = join(COMMITS, currentHash);
                    newCommit.createNewFile();
                    appendContents(newCommit, arg.getMessage(), "@", timestamp, "@",
                            parentHash, "@", currentHash, "@$!@");
                    writeContents(ADDFILE, "");
                    writeContents(REMOVEFILE, "");
                    writeContents(HEAD, currentHash);
                    writeContents(join(BRANCHES, readContentsAsString(CURRENT)), currentHash);
                }
            }
            if (staged.size() > 0) {
                currentHash = sha1(arg.getMessage(), timestamp, staged.toString(),
                        parentHash, removed.toString());
                File commitFile = join(COMMITS, currentHash);
                commitFile.createNewFile();
                appendContents(commitFile, arg.getMessage(), "@", timestamp,
                        "@", parentHash, "@", currentHash, "@");
                if (arg.getRefToBlobs().size() != 0) {
                    for (String blobsItem : arg.getRefToBlobs()) {
                        appendContents(commitFile, blobsItem, "$");
                    }
                }
                appendContents(commitFile, "!");
                if (arg.getFileLocation().size() != 0) {
                    for (String location : arg.getFileLocation()) {
                        appendContents(commitFile, location, "@");
                    }
                }
                writeContents(HEAD, currentHash);
                String currentBranch = readContentsAsString(CURRENT);
                File currentBranchFile = join(BRANCHES, currentBranch);
                writeContents(currentBranchFile, currentHash);
                writeContents(ADDFILE, "");
                writeContents(REMOVEFILE, "");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean createBranch(String branchName) {
        try {
            File branchFile = join(BRANCHES, branchName);
            if (branchFile.exists()) {
                return false;
            }
            String curhash = readContentsAsString(HEAD);
            branchFile.createNewFile();
            writeContents(branchFile, curhash);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean removeBranch(String branchName) {
        if (!join(BRANCHES, branchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }
        if (readContentsAsString(CURRENT).equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return false;
        }
        return deleteBranch(join(BRANCHES, branchName));
    }

    public static boolean checkoutName(String name) {
        try {
            PseudoCommit contents = readCommit(join(COMMITS, readContentsAsString(HEAD)));

            if (contents.fileLocation == null) {
                return false;
            }
            int size = contents.fileLocation.size();
            for (int i = 0; i < size; i++) {
                if (contents.fileLocation.get(i).equals(name)) {
                    File overwriteFile = join(CWD, contents.fileLocation.get(i));
                    if (!overwriteFile.exists()) {
                        overwriteFile.createNewFile();
                    }
                    String content = readContentsAsString(join(BLOBS,
                            contents.refToBlobs.get(i)));
                    writeContents(overwriteFile, content);
                    writeContents(HEAD, contents.currentHash);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkoutID(String id, String name) {
        try {
            String commitHash = readContentsAsString(HEAD);
            String subId = id;
            subId.substring(0, 6);
            while (commitHash.length() > 0) {
                File commitFile = join(COMMITS, commitHash);
                PseudoCommit contents = readCommit(commitFile);
                String subCurrentHash = contents.currentHash;
                subCurrentHash.substring(0, 6);
                if (contents.currentHash.substring(0, 6).equals(id.substring(0, 6))) {
                    if (contents.fileLocation == null) {
                        System.out.println("File does not exist in that commit.");
                        return false;
                    }
                    int size = contents.fileLocation.size();
                    for (int i = 0; i < size; i++) {
                        if (contents.fileLocation.get(i).equals(name)) {
                            File overwriteFile = join(CWD, contents.fileLocation.get(i));
                            if (!overwriteFile.exists()) {
                                overwriteFile.createNewFile();
                            }
                            String content = readContentsAsString(join(BLOBS,
                                    contents.refToBlobs.get(i)));
                            writeContents(overwriteFile, content);
                            return true;
                        }
                    }
                }
                commitHash = contents.parentHash;
            }
            System.out.println("No commit with that id exists.");
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkoutBranch(String branchName) {
        try {
            List<String> branch = plainFilenamesIn(BRANCHES);
            int size = branch.size();
            for (int i = 0; i < size; i++) {
                if (branch.get(i).equals(branchName)) {
                    if (readContentsAsString(CURRENT).equals(branchName)) {
                        System.out.println("No need to checkout the current branch.");
                        return;
                    }
                    PseudoCommit contents = readCommit(join(COMMITS,
                            readContentsAsString(join(BRANCHES, branchName))));
                    List<String> allFile = plainFilenamesIn(CWD);
                    List<String> allHash = new ArrayList<>(allFile.size());
                    size = allFile.size();
                    for (int j = 0; j < size; j++) {
                        allHash.add(sha1(readContentsAsString(join(CWD,
                                allFile.get(j))), allFile.get(j)));
                    }
                    for (String contentHash : contents.refToBlobs) {
                        for (String fileHash : allHash) {
                            if (contentHash.equals(fileHash)) {
                                System.out.println("There is an untracked file in the way; "
                                        + "delete it, or add and commit it first.");
                                return;
                            }
                        }
                    }
                    writeContents(HEAD, contents.currentHash);
                    writeContents(CURRENT, branchName);
                    size = contents.refToBlobs.size();
                    for (int j = 0; j < size; j++) {
                        File writeFile = join(CWD, contents.fileLocation.get(j));
                        if (!writeFile.exists()) {
                            writeFile.createNewFile();
                        }
                        writeContents(writeFile, readContentsAsString(
                                join(BLOBS, contents.refToBlobs.get(j))));
                    }
                    return;
                }
            }
            System.out.println("No such branch exists.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reset(String id) {
        try {
            List<String> commitFile = plainFilenamesIn(COMMITS);
            for (String file : commitFile) {
                PseudoCommit current = readCommit(join(COMMITS, file));
                if (current.currentHash.substring(0, 6).equals(id.substring(0, 6))) {
                    List<String> blobFiles = plainFilenamesIn(BLOBS);
                    List<String> allFile = plainFilenamesIn(CWD);
                    List<String> allHash = new ArrayList<>(allFile.size());
                    int size = allFile.size();
                    if (blobFiles == null) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                    if (blobFiles.isEmpty()) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                    for (int i = 0; i < size; i++) {
                        allHash.add(sha1(readContentsAsString(join(CWD,
                                allFile.get(i))), allFile.get(i)));
                    }
                    for (String fileHash : allHash) {
                        boolean checked = false;
                        for (String blobHash : blobFiles) {
                            if (blobHash.equals(fileHash)) {
                                checked = true;
                            }
                        }
                        if (!checked) {
                            System.out.println("There is an untracked file in the way; "
                                    + "delete it, or add and commit it first.");
                            return;
                        }
                    }
                    for (int i = 0; i < size; ++i) {
                        File dlt = join(CWD, allFile.get(i));
                        if (dlt.exists()) {
                            restrictedDelete(dlt);
                        }
                    }
                    size = current.refToBlobs.size();
                    for (int i = 0; i < size; ++i) {
                        File writeFile = join(CWD, current.fileLocation.get(i));
                        writeFile.createNewFile();
                        writeContents(writeFile, readContentsAsString(
                                join(BLOBS, current.refToBlobs.get(i))));
                    }
                    writeContents(HEAD, current.currentHash);
                    return;
                }
            }
            System.out.println("No commit with that id exists.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createcommitassetup(Commit arg) {
        try {
            String timestamp = Commit.formatDate(arg.getTimestamp());
            String currentHash = "";
            if (arg.getRefToBlobs() == null) {
                currentHash = sha1(arg.getMessage(), timestamp);
            } else {
                currentHash = sha1(arg.getMessage(), timestamp, arg.getRefToBlobs().toString());
            }
            File commitFile = join(COMMITS, currentHash);
            commitFile.createNewFile();
//        writeObject(commitFile, (Serializable) arg);
            appendContents(commitFile, arg.getMessage(), "@", timestamp, "@@", currentHash, "@");
            writeContents(HEAD, currentHash);
            writeContents(MASTER, currentHash);
            writeContents(CURRENT, "master");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


}

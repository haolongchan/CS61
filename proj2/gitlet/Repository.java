package gitlet;

import java.io.File;
import java.util.*;
import java.io.IOException;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Haolong
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
    public static final File OLDBLOBS = join(GITLET_DIR, "oldblobs");
    public static final File OLDCOMMITS = join(GITLET_DIR, "oldcommits");
    public static final File STORAGE = join(GITLET_DIR, "storage");

    /* contain files recording secure hash */
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    public static final File STAGES = join(GITLET_DIR, "stages");
    public static final File ADDFILE = join(STAGES, "addfile");
    public static final File REMOVEFILE = join(STAGES, "removefile");

    /* recording secure hash */
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File OLDHEAD = join(GITLET_DIR, "oldhead");
    public static final File MASTER = join(BRANCHES, "master");
    public static final File CURRENT = join(BRANCHES, "current");

//    public static String commitHash = "";
//    public static String blobHash = "";

    private static class PseudoCommit {
        String message;
        String timestamp;
        String parentHash;
        String currentHash;
        LinkedList<String> refToBlobs;
        LinkedList<String> fileLocation;
        String firstParentHash;
        String secondParentHash;
        private PseudoCommit(String msg, String tms, String prtH, String ha,
                             LinkedList<String> ref, LinkedList<String> loc,
                             String firstHash, String secondHash) {
            message = msg;
            timestamp = tms;
            parentHash = prtH;
            currentHash = ha;
            refToBlobs = ref;
            fileLocation = loc;
            firstParentHash = firstHash;
            secondParentHash = secondHash;
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
            OLDCOMMITS.mkdir();
            OLDBLOBS.mkdir();
            STORAGE.mkdir();
//        INDEXFILE.createNewFile();
//        BLOBINDEX.createNewFile();
            HEAD.createNewFile();
            OLDHEAD.createNewFile();
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
                    if (!readContentsAsString(join(BLOBS, fileHash)).isEmpty()) {
                        return false;
                    }
                }
            }

            if (!addContents[1].isEmpty()) {
                for (String s : addContents[1]) {
                    if (s.equals(fileName)) {
                        return false;
                    }
                }
            }
            File blob = join(BLOBS, fileHash);
            File oldBlob = join(OLDBLOBS, fileHash);
            blob.createNewFile();
            oldBlob.createNewFile();
            writeContents(blob, readContentsAsString(selected));
            writeContents(oldBlob, readContentsAsString(selected));
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
            if (size == 1 && commitContents.fileLocation.get(0).equals("")) {
                size = 0;
            }
            while (!commitContents.parentHash.isEmpty()) {
                for (int i = 0; i < size; i++) {
                    if (commitContents.fileLocation.get(i).equals(fileName)) {
                        restrictedDelete(toremove);
                        appendContents(REMOVEFILE, "^@", fileName, "@");
                        writeContents(join(BLOBS, commitContents.refToBlobs.get(i)), "");
                        return true;
                    }
                }
                commitContents = readCommit(join(COMMITS, commitContents.parentHash));
            }
            System.out.println("No reason to remove the file.");
            return false;
        }
        String fileHash = sha1(readContentsAsString(toremove), fileName);
        LinkedList<String>[] addContents = readAddStage();
        LinkedList<String>[] removeContents = readRemoveStage();
        if (!removeContents[0].isEmpty()) {
            for (String s : removeContents[1]) {
                if (s.equals(fileName)) {
                    System.out.println("No reason to remove the file.");
                    return false;
                }
            }
        }
        if (!addContents[0].isEmpty()) {
            for (String s : addContents[1]) {
                if (s.equals(fileName)) {
                    appendContents(REMOVEFILE, fileHash, "@", fileName, "@");
                    return true;
                }
            }
        }
        PseudoCommit contents = readCommit(join(COMMITS, readContentsAsString(HEAD)));
        boolean mark = false;
        while (!contents.parentHash.isEmpty()) {
            for (String s : contents.refToBlobs) {
                if (s.equals(fileHash)) {
                    appendContents(REMOVEFILE, fileHash, "@", fileName, "@");
                    restrictedDelete(toremove);
                    writeContents(join(BLOBS, fileHash), "");
                    mark = true;
                    break;
                }
            }
            contents = readCommit(join(COMMITS, contents.parentHash));
            if (contents.parentHash == null) {
                break;
            }
        }
        if (!mark) {
            System.out.println("No reason to remove the file.");
        }
        return mark;
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
        for (int i = index; i < size; i++) {
            if (contents.charAt(i) == '#') {
                index = i + 1;
                break;
            }
            if (contents.charAt(i) == '@') {
                fileLoc.add(tmp);
                index = i + 1;
                tmp = "";
            } else {
                tmp += contents.charAt(i);
            }
        }
        String[] ret = helpForReadCommit(contents, index, size);
        String firstParentHash = ret[0];
        String secondParentHash = ret[1];
        return new PseudoCommit(message, timestamp, parentHash,
                currentHash, refToBlob, fileLoc, firstParentHash, secondParentHash);
    }

    private static String[] helpForReadCommit(String contents, int index, int size) {
        String firstParentHash = "";
        String secondParentHash = "";
        if (index < size) {
            for (int i = index; i < size; i++) {
                if (contents.charAt(i) == '@') {
                    index = i + 1;
                    break;
                }
                firstParentHash += contents.charAt(i);
            }
            for (int i = index; i < size; i++) {
                if (contents.charAt(i) == '@') {
                    break;
                }
                secondParentHash += contents.charAt(i);
            }
        }
        return new String[]{firstParentHash, secondParentHash};
    }

    public static void log() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        String currentHash = readContentsAsString(HEAD);
        File currentCommit = join(COMMITS, currentHash);
        PseudoCommit currentContents = readCommit(currentCommit);
        System.out.println("===");
        System.out.println("commit " + currentContents.currentHash);
        if (!currentContents.firstParentHash.isEmpty()) {
            System.out.println("Merge: " + currentContents.firstParentHash.substring(0, 7)
                    + " " + currentContents.secondParentHash.substring(0, 7));
        }
        System.out.println("Date: " + currentContents.timestamp);
        System.out.println(currentContents.message);
        System.out.println();
        while (currentContents.parentHash.length() != 0) {
            currentCommit = join(COMMITS, currentContents.parentHash);
            currentContents = readCommit(currentCommit);
            System.out.println("===");
            System.out.println("commit " + currentContents.currentHash);
            if (!currentContents.firstParentHash.isEmpty()) {
                System.out.println("Merge: " + currentContents.firstParentHash.substring(0, 7)
                        + " " + currentContents.secondParentHash.substring(0, 7));
            }
            System.out.println("Date: " + currentContents.timestamp);
            System.out.println(currentContents.message);
            System.out.println();
        }
    }

    public static void globallog() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
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
            if (!currentContents.firstParentHash.isEmpty()) {
                System.out.println("Merge: " + currentContents.firstParentHash.substring(0, 7)
                        + " " + currentContents.secondParentHash.substring(0, 7));
            }
            System.out.println("Date: " + currentContents.timestamp);
            System.out.println(currentContents.message);
            System.out.println();
        }
    }

    public static boolean find(String msg) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
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
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
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
        System.out.println();
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
        System.out.println();
        System.out.println("=== Removed Files ===");
        size = remove.size();
        for (int i = 0; i < size; ++i) {
            System.out.println(remove.get(i));
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");

    }

    public static LinkedList<String>[] readAddStage() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return null;
        }
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
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return null;
        }
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
        int psize = parentContents.refToBlobs.size();
        if (psize == 1 && parentContents.refToBlobs.get(0).equals("")) {
            psize = 0;
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < psize; j++) {
                if (arg.getRmHash().get(i).isEmpty()) {
                    continue;
                }
                if (parentContents.refToBlobs.get(j).equals(arg.getRmHash().get(i))) {
                    parentContents.refToBlobs.remove(j);
                    parentContents.fileLocation.remove(j);
                    psize--;
                    if (join(CWD, arg.getRmFile().get(i)).exists()) {
                        restrictedDelete(join(CWD, arg.getRmFile().get(i)));
                        writeContents(join(BLOBS, arg.getRmHash().get(i)), "");
                    }
                }
            }
        }
        writeContents(REMOVEFILE, "");
        writeContents(join(COMMITS, readContentsAsString(HEAD)), parentContents.message,
                "@", parentContents.timestamp, "@", parentContents.parentHash, "@",
                parentContents.currentHash, "@");
        if (!parentContents.refToBlobs.isEmpty()) {
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
        if (psize == 1 && parentContents.refToBlobs.get(0).equals("")) {
            psize = 0;
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < psize; j++) {
                if (parentContents.refToBlobs.get(j).length() == 0) {
                    continue;
                }
                if (parentContents.fileLocation.get(j).equals(removed.get(i))) {
                    String deleteHash = parentContents.refToBlobs.get(j);
                    parentContents.refToBlobs.remove(j);
                    parentContents.fileLocation.remove(j);
                    psize--;
                    if (join(CWD, removed.get(i)).exists()) {
                        restrictedDelete(join(CWD, removed.get(i)));
                        writeContents(join(BLOBS, deleteHash), "");
                    }
                }
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
            checkGitlet();
            String timestamp = Commit.formatDate(arg.getTimestamp());
            String parentHash = readContentsAsString(HEAD);
            arg.addparentHash(parentHash);
            String currentHash = sha1(arg.getMessage(), arg.getTimestamp().toString(),
                    arg.getRmHash().toString(), arg.getRmFile().toString());
            if (arg.getRefToBlobs() == null) {
                partOfCommits(arg);
                File newCommit = join(COMMITS, currentHash);
                File oldCommit = join(OLDCOMMITS, currentHash);
                oldCommit.createNewFile();
                newCommit.createNewFile();
                writeContents(newCommit, arg.getMessage(), "@", timestamp, "@",
                        parentHash, "@", currentHash, "@$!@");
                writeContents(oldCommit, arg.getMessage(), "@", timestamp, "@",
                        parentHash, "@", currentHash, "@$!@");
                writeContents(ADDFILE, "");
                writeContents(REMOVEFILE, "");
                writeContents(HEAD, currentHash);
                writeContents(OLDHEAD, currentHash);
                writeContents(join(BRANCHES, readContentsAsString(CURRENT)), currentHash);
                setStorage();
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
                    File oldCommit = join(OLDCOMMITS, currentHash);
                    newCommit.createNewFile();
                    oldCommit.createNewFile();
                    appendContents(newCommit, arg.getMessage(), "@", timestamp, "@",
                            parentHash, "@", currentHash, "@$!@");
                    appendContents(oldCommit, arg.getMessage(), "@", timestamp, "@",
                            parentHash, "@", currentHash, "@$!@");
                    writeContents(ADDFILE, "");
                    writeContents(REMOVEFILE, "");
                    writeContents(HEAD, currentHash);
                    writeContents(OLDHEAD, currentHash);
                    writeContents(join(BRANCHES, readContentsAsString(CURRENT)), currentHash);
                    setStorage();
                }
            }
            if (staged.size() > 0) {
                helpForCommits(arg, timestamp, parentHash, staged, removed);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void helpForCommits(Commit arg, String timestamp, String parentHash,
                                       List<String> removed, List<String> staged) {
        try {
            String currentHash = sha1(arg.getMessage(), timestamp, staged.toString(),
                    parentHash, removed.toString());
            File commitFile = join(COMMITS, currentHash);
            File oldCommitFile = join(OLDCOMMITS, currentHash);
            oldCommitFile.createNewFile();
            commitFile.createNewFile();
            appendContents(commitFile, arg.getMessage(), "@", timestamp,
                    "@", parentHash, "@", currentHash, "@");
            appendContents(oldCommitFile, arg.getMessage(), "@", timestamp, "@",
                    parentHash, "@", currentHash, "@");
            if (arg.getRefToBlobs().size() != 0) {
                for (String blobsItem : arg.getRefToBlobs()) {
                    appendContents(commitFile, blobsItem, "$");
                    appendContents(oldCommitFile, blobsItem, "$");
                }
            }
            appendContents(commitFile, "!");
            appendContents(oldCommitFile, "!");
            if (arg.getFileLocation().size() != 0) {
                for (String location : arg.getFileLocation()) {
                    appendContents(commitFile, location, "@");
                    appendContents(oldCommitFile, location, "@");
                }
            }
            writeContents(HEAD, currentHash);
            writeContents(OLDHEAD, currentHash);
            writeContents(join(BRANCHES, readContentsAsString(CURRENT)), currentHash);
            writeContents(ADDFILE, "");
            writeContents(REMOVEFILE, "");
            setStorage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean createBranch(String branchName) {
        try {
            checkGitlet();
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
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
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
            checkGitlet();
            PseudoCommit contents = readCommit(join(OLDCOMMITS, readContentsAsString(OLDHEAD)));

            if (contents.fileLocation == null) {
                return false;
            }
            int size = contents.fileLocation.size();
            if (size == 1 && contents.fileLocation.get(0).equals("")) {
                size = 0;
            }
            for (int i = 0; i < size; i++) {
                if (contents.fileLocation.get(i).equals(name)) {
                    File overwriteFile = join(CWD, name);
                    if (!overwriteFile.exists()) {
                        overwriteFile.createNewFile();
                    }
                    String content = readContentsAsString(join(OLDBLOBS,
                            contents.refToBlobs.get(i)));
                    writeContents(overwriteFile, content);
                    writeContents(HEAD, contents.currentHash);
                    writeContents(OLDHEAD, contents.currentHash);
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
            checkGitlet();
            String commitHash = readContentsAsString(OLDHEAD);
            String subId = id;
            subId.substring(0, 6);
            while (commitHash.length() > 0) {
                File commitFile = join(OLDCOMMITS, commitHash);
                PseudoCommit contents = readCommit(commitFile);
                String subCurrentHash = contents.currentHash;
                subCurrentHash.substring(0, 6);
                if (contents.currentHash.substring(0, 6).equals(id.substring(0, 6))) {
                    int size = contents.fileLocation.size();
                    if (size == 1 && contents.fileLocation.get(0).equals("")) {
                        size = 0;
                    }
                    for (int i = 0; i < size; i++) {
                        if (contents.fileLocation.get(i).equals(name)) {
                            File overwriteFile = join(CWD, contents.fileLocation.get(i));
                            if (!overwriteFile.exists()) {
                                overwriteFile.createNewFile();
                            }
                            String content = readContentsAsString(join(OLDBLOBS,
                                    contents.refToBlobs.get(i)));
                            writeContents(overwriteFile, content);
                            return true;
                        }
                    }
                    System.out.println("File does not exist in that commit.");
                    return false;
                }
                commitHash = contents.parentHash;
            }
            System.out.println("No commit with that id exists.");
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkGitlet() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static PseudoCommit getHeadCommit() {
        return readCommit(join(OLDCOMMITS, readContentsAsString(OLDHEAD)));
    }

    private static PseudoCommit getNewCommit(String branchName) {
        return readCommit(join(OLDCOMMITS, readContentsAsString(join(BRANCHES, branchName))));
    }

    private static void helpForCheckout(String branchName) {
        if (readContentsAsString(CURRENT).equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
    }

    public static void checkoutBranch(String branchName) {
        try {
            checkGitlet();
            List<String> branch = plainFilenamesIn(BRANCHES);
            for (int i = 0; i < branch.size(); i++) {
                if (branch.get(i).equals(branchName)) {
                    helpForCheckout(branchName);
                    PseudoCommit contents = getNewCommit(branchName);
                    PseudoCommit headContents = getHeadCommit();
                    List<String> allFile = plainFilenamesIn(CWD);
                    int branchSize = contents.fileLocation.size();
                    int headSize = headContents.fileLocation.size();
                    if (branchSize == 1 && contents.fileLocation.get(0).equals("")) {
                        branchSize = 0;
                    }
                    if (headSize == 1 && headContents.fileLocation.get(0).equals("")) {
                        headSize = 0;
                    }
                    for (int j = 0; j < branchSize; j++) {
                        boolean checked = false;
                        for (int k = 0; k < headSize; k++) {
                            if (contents.refToBlobs.get(j).equals
                                    (headContents.refToBlobs.get(k))) {
                                checked = true;
                                break;
                            }
                        }
                        if (!checked) {
                            if (join(CWD, contents.fileLocation.get(j)).exists()) {
                                if (!contents.refToBlobs.get(j).equals(sha1(readContentsAsString(
                                                join(CWD, contents.fileLocation.get(j))),
                                        contents.fileLocation.get(j)))) {
                                    if (join(OLDBLOBS, sha1(readContentsAsString(join(CWD,
                                                    contents.fileLocation.get(j))),
                                            contents.fileLocation.get(j))).exists()) {
                                        continue;
                                    }
                                    System.out.println("There is an untracked file in the way; "
                                            + "delete it, or add and commit it first.");
                                    return;
                                }
                            }
                        }
                    }
                    for (int j = 0; j < branchSize; j++) {
                        boolean checked = false;
                        for (int k = 0; k < headSize; k++) {
                            if (contents.refToBlobs.get(j).equals
                                    (headContents.refToBlobs.get(k))) {
                                checked = true;
                                break;
                            }
                        }
                        if (!checked) {
                            restrictedDelete(join(CWD, contents.fileLocation.get(j)));
                        }
                    }
                    for (String deleteFileInCWD : allFile) {
                        File fileToDelete = join(CWD, deleteFileInCWD);
                        restrictedDelete(fileToDelete);
                    }
                    for (int j = 0; j < branchSize; j++) {
                        File writeFile = join(CWD, contents.fileLocation.get(j));
                        if (!writeFile.exists()) {
                            writeFile.createNewFile();
                        }
                        writeContents(writeFile, readContentsAsString(
                                join(OLDBLOBS, contents.refToBlobs.get(j))));
                    }
                    operateForCheckout(contents, branchName);
                    return;
                }
            }
            System.out.println("No such branch exists.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void operateForCheckout(PseudoCommit contents, String branchName) {
        writeContents(HEAD, contents.currentHash);
        writeContents(OLDHEAD, contents.currentHash);
        writeContents(CURRENT, branchName);
        renew();
        writeOldCommitToCurrent();
    }

    private static void writeOldCommitToCurrent() {
        String id = readContentsAsString(HEAD);
        File oldCommit = join(OLDCOMMITS, id);
        File commit = join(COMMITS, id);
        writeContents(commit, readContentsAsString(oldCommit));
    }

    private static void setStorage() {
        try {
            List<String> allFile = plainFilenamesIn(CWD);
            File writeFile = join(STORAGE, readContentsAsString(HEAD));
            if (!writeFile.exists()) {
                writeFile.createNewFile();
            } else {
                writeContents(writeFile, "");
            }
            for (String name : allFile) {
                String hash = sha1(readContentsAsString(join(CWD, name)), name);
                appendContents(writeFile, hash, ":", name, "@");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static LinkedList<String>[] readStorage(String id) {
        File readFile = join(STORAGE, id);
        LinkedList<String>[] ret = new LinkedList[2];
        ret[0] = new LinkedList();
        ret[1] = new LinkedList();
        String fileHash = "";
        String fileName = "";
        String content = readContentsAsString(readFile);
        int size = content.length();
        boolean hash = true;
        for (int i = 0; i < size; i++) {
            if (hash) {
                if (content.charAt(i) == ':') {
                    hash = false;
                    ret[0].add(fileHash);
                    fileHash = "";
                } else {
                    fileHash += content.charAt(i);
                }
            } else {
                if (content.charAt(i) == '@') {
                    hash = true;
                    ret[1].add(fileName);
                    fileName = "";
                } else {
                    fileName += content.charAt(i);
                }
            }
        }
        return ret;
    }

    private static void renew() {
        try {
            List<String> deleteFile = plainFilenamesIn(CWD);
            for (String name : deleteFile) {
                restrictedDelete(join(CWD, name));
            }
            String id = readContentsAsString(HEAD);
            LinkedList<String>[] file = readStorage(id);
            int size = file[0].size();
            for (int i = 0; i < size; i++) {
                String fileName = file[1].get(i);
                String fileHash = file[0].get(i);
                String content = readContentsAsString(join(OLDBLOBS, fileHash));
                File writeFile = join(CWD, fileName);
                if (!writeFile.exists()) {
                    writeFile.createNewFile();
                }
                writeContents(writeFile, content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reset(String id) {
        try {
            checkGitlet();
            List<String> commitFile = plainFilenamesIn(OLDCOMMITS);
            for (String file : commitFile) {
                PseudoCommit current = readCommit(join(OLDCOMMITS, file));
                if (current.currentHash.substring(0, 6).equals(id.substring(0, 6))) {
                    List<String> blobFiles = plainFilenamesIn(OLDBLOBS);
                    List<String> allFile = plainFilenamesIn(CWD);
                    List<String> allHash = new ArrayList<>(allFile.size());
                    int size = allFile.size();
                    if (blobFiles == null && allFile != null) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                    if (blobFiles.isEmpty() && !allFile.isEmpty()) {
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
                                break;
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
                    if (size == 1 && current.refToBlobs.get(0).equals("")) {
                        size = 0;
                    }
                    for (int i = 0; i < size; ++i) {
                        File writeFile = join(CWD, current.fileLocation.get(i));
                        writeFile.createNewFile();
                        writeContents(writeFile, readContentsAsString(
                                join(OLDBLOBS, current.refToBlobs.get(i))));
                    }
                    writeContents(HEAD, current.currentHash);
                    writeContents(OLDHEAD, current.currentHash);
                    writeContents(join(BRANCHES, readContentsAsString(CURRENT)),
                            current.currentHash);
                    writeContents(ADDFILE, "");
                    writeContents(REMOVEFILE, "");
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
            checkGitlet();
            String timestamp = Commit.formatDate(arg.getTimestamp());
            String currentHash = "";
            if (arg.getRefToBlobs() == null) {
                currentHash = sha1(arg.getMessage(), timestamp);
            } else {
                currentHash = sha1(arg.getMessage(), timestamp, arg.getRefToBlobs().toString());
            }
            File commitFile = join(COMMITS, currentHash);
            File oldCommit = join(OLDCOMMITS, currentHash);
            commitFile.createNewFile();
            oldCommit.createNewFile();
//        writeObject(commitFile, (Serializable) arg);
            appendContents(commitFile, arg.getMessage(),
                    "@", timestamp, "@@", currentHash, "@$!@");
            appendContents(oldCommit, arg.getMessage(), "@", timestamp, "@@", currentHash, "@$!@");
            writeContents(HEAD, currentHash);
            writeContents(OLDHEAD, currentHash);
            writeContents(MASTER, currentHash);
            writeContents(CURRENT, "master");
            File file = join(STORAGE, currentHash);
            file.createNewFile();
            writeContents(file, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static String lca(String givenBranch, String currentBranch) {
        LinkedList<String>[] givenCommit = readStorage(
                readContentsAsString(join(BRANCHES, givenBranch)));
        LinkedList<String>[] currentCommit = readStorage(
                readContentsAsString(join(BRANCHES, currentBranch)));
        LinkedList<String> givenParentBranch = new LinkedList<>();
        String parentHash = readContentsAsString(join(BRANCHES, givenBranch));
        LinkedList<String> parentList = new LinkedList<>();
        parentList.addLast(parentHash);
        LinkedList<String> currentParentBranch = new LinkedList<>();
        String currentHash = readContentsAsString(join(BRANCHES, currentBranch));
        LinkedList<String> currentList = new LinkedList<>();
        currentList.addLast(currentHash);
            // mistake may occur
        while (!parentList.isEmpty()) {
            String cur = parentList.removeFirst();
            givenParentBranch.addLast(cur);
            if (readCommit(join(COMMITS, cur)).firstParentHash == null) {
                parentList.addLast(readCommit(join(COMMITS, cur)).parentHash);
                continue;
            }
            if (readCommit(join(COMMITS, cur)).firstParentHash.isEmpty()) {
                parentList.addLast(readCommit(join(COMMITS, cur)).parentHash);
                continue;
            }
            parentList.addLast(readCommit(join(COMMITS, cur)).firstParentHash);
            parentList.addLast(readCommit(join(COMMITS, cur)).secondParentHash);
        }
        while (!currentList.isEmpty()) {
            String cur = currentList.removeFirst();
            currentParentBranch.addLast(cur);
            if (readCommit(join(COMMITS, cur)).firstParentHash == null) {
                currentList.addLast(readCommit(join(COMMITS, cur)).parentHash);
                continue;
            }
            if (readCommit(join(COMMITS, cur)).firstParentHash.isEmpty()) {
                currentList.addLast(readCommit(join(COMMITS, cur)).parentHash);
                continue;
            }
            currentList.addLast(readCommit(join(COMMITS, cur)).firstParentHash);
            currentList.addLast(readCommit(join(COMMITS, cur)).secondParentHash);
        }
        for (String s : givenParentBranch) {
            for (String ss : currentParentBranch) {
                if (s.equals(ss)) {
                    return s;
                }
            }
        }
        return null;
    }

    private static void checkBranchName(List<String> allBranchNames, String branchName) {
        boolean checked = false;
        if (readContentsAsString(ADDFILE).length() != 0
                || readContentsAsString(REMOVEFILE).length() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        for (String branch : allBranchNames) {
            if (branch.equals(branchName)) {
                checked = true;
                break;
            }
        }
        if (!checked) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    private static void checkForMerge(String branchName,
                                      String ancestorHash, int givenSize, int currentSize,
                                      LinkedList<String>[] givenCommit,
                                      LinkedList<String>[] currentCommit) {
        boolean checked = true;
        if (readContentsAsString(CURRENT).equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String branchHash = readContentsAsString(join(BRANCHES, branchName));
        if (branchHash.equals(ancestorHash)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (readContentsAsString(join(BRANCHES, readContentsAsString(CURRENT))).
                equals(ancestorHash)) {
            System.out.println("Current branch fast-forwarded.");
        }
        for (int i = 0; i < givenSize; i++) {
            checked = false;
            for (int j = 0; j < currentSize; ++j) {
                if (givenCommit[0].get(i).equals
                        (currentCommit[0].get(j))) {
                    checked = true;
                    break;
                }
            }
            if (!checked) {
                if (join(CWD, givenCommit[1].get(i)).exists()) {
                    if (!givenCommit[0].get(i).equals(sha1(readContentsAsString(
                            join(CWD, givenCommit[1].get(i))
                    ), givenCommit[1].get(i)))) {
                        if (join(BLOBS, sha1(readContentsAsString(join(CWD,
                                        givenCommit[1].get(i))),
                                givenCommit[1].get(i))).exists()) {
                            continue;
                        }
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    private static boolean threeFiles(String splitHash, String currentHash, String givenHash,
                                      int givenSize, String fileName,
                                      LinkedList<String>[] givenCommit) {
        // case: 1, 2, 3
        if (!splitHash.equals(givenHash) && splitHash.equals(currentHash)) {
            // case: 1
            for (int i = 0; i < givenSize; i++) {
                if (givenCommit[1].get(i).equals(fileName)) {
                    writeContents(join(CWD, fileName), readContentsAsString
                            (join(OLDBLOBS, givenCommit[0].get(i))));
                    addFile(fileName);
                    break;
                }
            }
        }
        if (splitHash.equals(givenHash) && !splitHash.equals(currentHash)) {
            // case: 2
            addFile(fileName);
        }
        if (!splitHash.equals(givenHash) && !splitHash.equals(currentHash)) {
            if (givenHash.equals(currentHash)) {
                // case: 3.1
                addFile(fileName);
            } else {
                // case: 3.2
                String currentContent = readContentsAsString(join(OLDBLOBS, currentHash));
                String givenContent = "";
                for (int i = 0; i < givenSize; ++i) {
                    if (givenCommit[1].get(i).equals(fileName)) {
                        givenContent = readContentsAsString(join(OLDBLOBS,
                                givenCommit[0].get(i)));
                    }
                }
                writeContents(join(CWD, fileName), "<<<<<<< HEAD\n",
                        currentContent, "=======\n", givenContent, ">>>>>>>\n");
                return false;
            }
        }
        return true;
    }

    private static boolean lackGiven(String splitHash, String currentHash, String fileName,
                                     LinkedList<String>[] givenCommit,
                                     LinkedList<String>[] currentCommit,
                                     LinkedList<String>[] splitCommit) {
        int givenSize = givenCommit[1].size();
        int currentSize = currentCommit[1].size();
        int splitSize = splitCommit[1].size();
        if (givenSize == 1 && givenCommit[1].get(0).equals("")) {
            givenSize = 0;
        }
        if (currentSize == 1 && currentCommit[1].get(0).equals("")) {
            currentSize = 0;
        }
        if (splitSize == 1 && splitCommit[1].get(0).equals("")) {
            splitSize = 0;
        }
        if (!splitHash.equals(currentHash)) {
            if (currentHash.isEmpty() || currentHash.equals("@")) {
                return true;
            }
            // case: 3.2
            String currentContent = readContentsAsString(join(OLDBLOBS, currentHash));
            String splitContent = "";
            for (int i = 0; i < givenSize; ++i) {
                if (givenCommit[1].get(i).equals(fileName)) {
                    splitContent = readContentsAsString(join(OLDBLOBS,
                            splitCommit[0].get(i)));
                }
            }
            writeContents(join(CWD, fileName), "<<<<<<< HEAD\n",
                    currentContent, "=======\n", splitContent, ">>>>>>>\n");
            return false;
        }
        // case: 6
        for (int i = 0; i < currentSize; ++i) {
            if (currentCommit[1].get(i).equals(fileName)) {
                for (int j = 0; j < splitSize; ++j) {
                    if (splitCommit[1].get(j).equals(fileName)) {
                        if (currentCommit[0].get(i).equals
                                (splitCommit[0].get(j))) {
                            restrictedDelete(join(CWD, fileName));
                            writeContents(join(BLOBS, currentCommit[0].get(i)), "");
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean lackCurrent(String splitHash, String givenHash, String fileName,
                                       int givenSize, int splitSize,
                                       LinkedList<String>[] givenCommit,
                                       LinkedList<String>[] splitCommit) {
        if (!splitHash.equals(givenHash)) {
            if (givenHash.isEmpty() || givenHash.equals("@")) {
                restrictedDelete(join(CWD, fileName));
                return true;
            }
            // case: 3.2
            String splitContent = "";
            String givenContent = "";
            for (int i = 0; i < splitSize; ++i) {
                if (splitCommit[1].get(i).equals(fileName)) {
                    splitContent = readContentsAsString(join(OLDBLOBS,
                            splitCommit[0].get(i)));
                    break;
                }
            }
            for (int i = 0; i < givenSize; ++i) {
                if (givenCommit[1].get(i).equals(fileName)) {
                    givenContent = readContentsAsString(join(OLDBLOBS,
                            givenCommit[0].get(i)));
                }
            }
            writeContents(join(CWD, fileName), "<<<<<<< HEAD\n",
                    splitContent, "=======\n", givenContent, ">>>>>>>\n");
            return false;
        }
        // case: 7
        return true;
    }

    private static boolean lackSplit(LinkedList<String>[] currentCommit, String fileName,
                                     int givenSize,
                                     LinkedList<String>[] givenCommit, String currentHash,
                                     String givenHash) {
        try {
            if (currentCommit[1].contains(fileName)) {
                if (!givenCommit[1].contains(fileName)) {
                    // case: 4
                    return true;
                } else {
                    if (currentHash.equals(givenHash)) {
                        if (currentHash.equals("@")) {
                            restrictedDelete(join(CWD, fileName));
                        }
                        return true;
                    }
                    // case: 3.2
                    String currentContent = readContentsAsString(join(OLDBLOBS, currentHash));
                    String givenContent = "";
                    for (int i = 0; i < givenSize; ++i) {
                        if (givenCommit[1].get(i).equals(fileName)) {
                            givenContent = readContentsAsString(join(OLDBLOBS,
                                    givenCommit[0].get(i)));
                        }
                    }
                    writeContents(join(CWD, fileName), "<<<<<<< HEAD\n",
                            currentContent, "=======\n", givenContent, ">>>>>>>\n");
                    return false;
                }
            } else {
                // case: 5
                if (givenCommit[1].contains(fileName)) {
                    File createFile = join(CWD, fileName);
                    createFile.createNewFile();
                    for (int i = 0; i < givenSize; ++i) {
                        if (givenCommit[1].get(i).equals(fileName)) {
                            writeContents(createFile, readContentsAsString(join(
                                    OLDBLOBS, givenCommit[0].get(i))));
                            break;
                        }
                    }
                }
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> offerAllFile(LinkedList<String>[] givenCommit,
                                            LinkedList<String>[] currentCommit,
                                             LinkedList<String>[] splitCommit) {
        Set<String> allFileName = new HashSet<>();
        for (String fileName : givenCommit[1]) {
            if (!fileName.equals("")) {
                allFileName.add(fileName);
            }
        }
        for (String fileName : currentCommit[1]) {
            if (!fileName.equals("")) {
                allFileName.add(fileName);
            }
        }
        for (String fileName : splitCommit[1]) {
            if (!fileName.equals("")) {
                allFileName.add(fileName);
            }
        }
        return allFileName;
    }

    public static void merge(String branchName) {
        List<String> allBranchNames = plainFilenamesIn(BRANCHES);
        checkBranchName(allBranchNames, branchName);
        String ancestorHash = lca(branchName, readContentsAsString(CURRENT));
        LinkedList<String>[] givenCommit = readStorage(
                readContentsAsString(join(BRANCHES, branchName)));
        LinkedList<String>[] currentCommit = readStorage(readContentsAsString(HEAD));
        LinkedList<String>[] splitCommit = readStorage(ancestorHash);
        boolean flag = true;
        Set<String> allFileName = offerAllFile(givenCommit, currentCommit, splitCommit);
        int givenSize = givenCommit[1].size();
        int currentSize = currentCommit[1].size();
        int splitSize = splitCommit[0].size();
        if (givenSize == 1 && givenCommit[1].get(0).equals("")) {
            givenSize = 0;
        }
        if (currentSize == 1 && currentCommit[1].get(0).equals("")) {
            currentSize = 0;
        }
        if (splitSize == 1 && splitCommit[1].get(0).equals("")) {
            splitSize = 0;
        }
        checkForMerge(branchName, ancestorHash, givenSize, currentSize,
                givenCommit, currentCommit);
        for (String fileName : allFileName) {
            String givenHash = "@";
            String currentHash = "@";
            String splitHash = "@";
            for (int i = 0; i < givenSize; i++) {
                if (givenCommit[1].get(i).equals(fileName)) {
                    givenHash = givenCommit[0].get(i);
                    break;
                }
            }
            for (int i = 0; i < currentSize; i++) {
                if (currentCommit[1].get(i).equals(fileName)) {
                    currentHash = currentCommit[0].get(i);
                    break;
                }
            }
            for (int i = 0; i < splitSize; i++) {
                if (splitCommit[1].get(i).equals(fileName)) {
                    splitHash = splitCommit[0].get(i);
                    break;
                }
            }
            if (splitCommit[1].contains(fileName)) {
                if (currentCommit[1].contains(fileName)) {
                    if (givenCommit[1].contains(fileName)) {
                        flag = threeFiles(splitHash, currentHash, givenHash, givenSize, fileName,
                                givenCommit);
                    } else {
                        flag = lackGiven(splitHash, currentHash, fileName, givenCommit,
                                currentCommit, splitCommit);
                    }
                } else {
                    flag = lackCurrent(splitHash, givenHash, fileName, givenSize, splitSize,
                            givenCommit, splitCommit);
                }
            } else {
                flag = lackSplit(currentCommit, fileName, givenSize, givenCommit,
                        currentHash, givenHash);
            }
        }
        endOfMerge(branchName, flag);
    }

    private static void endOfMerge(String branchName, boolean flag) {
        try {
            if (!flag) {
                System.out.println("Encountered a merge conflict.");
            }
            String givenHash = readContentsAsString(join(BRANCHES, branchName));
            String message = "Merged " + branchName + " into " + readContentsAsString(CURRENT)
                    + ".";
            String timestamp = Commit.formatDate(new Date());
            String commitHash = sha1(message, timestamp);
            File commitFile = join(COMMITS, commitHash);
            File oldCommit = join(OLDCOMMITS, commitHash);
            oldCommit.createNewFile();
            commitFile.createNewFile();
            writeContents(commitFile, message, "@", timestamp, "@", readContentsAsString(join(
                            BRANCHES, readContentsAsString(CURRENT))), "@", commitHash, "@$!@#",
                    readContentsAsString(join(BRANCHES, readContentsAsString(CURRENT))),
                    "@", givenHash);
            writeContents(oldCommit, message, "@", timestamp, "@", readContentsAsString(join(
                            BRANCHES, readContentsAsString(CURRENT))), "@", commitHash, "@$!@#",
                    readContentsAsString(join(BRANCHES, readContentsAsString(CURRENT))),
                    "@", givenHash);
            writeContents(join(BRANCHES, readContentsAsString(CURRENT)), commitHash);
            writeContents(HEAD, commitHash);
            writeContents(OLDHEAD, commitHash);
            writeContents(ADDFILE, "");
            writeContents(REMOVEFILE, "");
            setStorage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}

package gitlet;

import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Locale;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Haolong
 */
public class Commit {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */

    private static String message;
    private static Date timestamp;
    private static LinkedList<String> refToBlobs = new LinkedList<>();
    private static String parentHash;
    private static String hash;
    private static LinkedList<String> fileLocation;
    private static LinkedList<String> rmHash;
    private static LinkedList<String> rmFile;

    public void addparentHash(String hsh) {
        this.parentHash = hsh;
    }

    public void addhash(String hsh) {
        this.hash = hsh;
    }

    public LinkedList<String> getFileLocation() {
        return fileLocation;
    }

    public LinkedList<String> getRmHash() {
        return rmHash;
    }

    public LinkedList<String> getRmFile() {
        return rmFile;
    }

    public String getMessage() {
        return message;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public LinkedList<String> getRefToBlobs() {
        return refToBlobs;
    }

    public Commit(String message, Date timestamp, LinkedList<String> refToBlobs,
                  String parentHash, LinkedList<String> fileLocation, LinkedList<String> rmHash,
                  LinkedList<String> rmFile) {
        this.message = message;
        this.timestamp = timestamp;
        this.refToBlobs = refToBlobs;
        this.parentHash = parentHash;
        this.fileLocation = fileLocation;
        this.rmHash = rmHash;
        this.rmFile = rmFile;
    }

    public static String formatDate(Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);

        // Use Formatter to format the date
        formatter.format(Locale.US, "%ta %tb %td %tT %tY %tz",
                date, date, date, date, date, date);

        return sb.toString();
    }

    public static void addAsSetup(String msg, String parent) {
        timestamp = new Date(0);
        message = msg;
        refToBlobs = null;
        parentHash = parent;
        Repository.createcommitassetup(new Commit(message, timestamp,
                refToBlobs, parentHash, null, null, null));
    }

    public static void add(String msg, String parent, LinkedList<String>[] refs, LinkedList<String>[] rmFileLocation) {
        message = msg;
        timestamp = new Date();
        refToBlobs = refs[0];
        fileLocation = refs[1];
        parentHash = parent;
        rmHash = rmFileLocation[0];
        rmFile = rmFileLocation[1];
        Repository.createcommits(new Commit(message, timestamp,
                refToBlobs, parentHash, fileLocation, rmHash, rmFile));
    }

}

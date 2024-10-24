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

    public void addparentHash(String hash) {
        this.parentHash = hash;
    }

    public void addhash(String hash) {
        this.hash = hash;
    }

    public LinkedList<String> getFileLocation() {
        return fileLocation;
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
                  String parentHash, LinkedList<String> fileLocation) {
        this.message = message;
        this.timestamp = timestamp;
        this.refToBlobs = refToBlobs;
        this.parentHash = parentHash;
        this.fileLocation = fileLocation;
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
                refToBlobs, parentHash, null));
    }

    public static void add(String msg, String parent, LinkedList<String>[] refs) {
        message = msg;
        timestamp = new Date();
        refToBlobs = refs[0];
        fileLocation = refs[1];
        parentHash = parent;
        Repository.createcommits(new Commit(message, timestamp,
                refToBlobs, parentHash, fileLocation));
    }

}

package capers;

import java.io.File;
import java.io.Serializable;
import static capers.Utils.*;

/** Represents a dog that can be serialized.
 * @author TODO
*/
public class Dog { // TODO

    /** Folder that dogs live in. */
    static final File CWD = new File(System.getProperty("user.dir"));
    static final File DOG_FOLDER = join(CWD.getPath(), ".capers", "dogs"); // TODO (hint: look at the `join`
    static final File DOG_FILE = join(DOG_FOLDER, "dogs.txt");

    /** Age of dog. */
    private int age;
    /** Breed of dog. */
    private String breed;
    /** Name of dog. */
    private String name;

    /**
     * Creates a dog object with the specified parameters.
     * @param name Name of dog
     * @param breed Breed of dog
     * @param age Age of dog
     */
    public Dog(String name, String breed, int age) {
        this.name = name;
        this.breed = breed;
        this.age = age;
    }

    /**
     * Reads in and deserializes a dog from a file with name NAME in DOG_FOLDER.
     *
     * @param name Name of dog to load
     * @return Dog read from file
     */
    public static Dog fromFile(String name) {
        // TODO (hint: look at the Utils file)
        String data = readContentsAsString(DOG_FILE);
        int sz = data.length();
        int index = data.lastIndexOf(name);
        Dog ret = new Dog("", "", 0);
        if (index == -1){
            return ret;
        }
        while(data.charAt(index) != ' '){
            ret.name += data.charAt(index);
            index++;
        }
        index++;
        while(data.charAt(index + 1) > '9' || data.charAt(index + 1) < '0'){
            ret.breed += data.charAt(index);
            index++;
        }
        index++;
        String num = "";
        while(data.charAt(index) >= '0' && data.charAt(index) <= '9'){
            num += data.charAt(index);
            index++;
            if (index == sz){
                break;
            }
        }
        ret.age = Integer.parseInt(num);
        return ret;
    }

    /**
     * Increases a dog's age and celebrates!
     */
    public void haveBirthday() {
        age += 1;
        String output = toString();
        System.out.println(output);
        System.out.println("Happy birthday! Woof! Woof!");
    }

    /**
     * Saves a dog to a file for future use.
     */
    public void saveDog() {
        // TODO (hint: don't forget dog names are unique)
        writeContents(DOG_FILE, this.name, " ", this.breed, " ", Integer.toString(this.age), "\n");
    }

    @Override
    public String toString() {
        return String.format(
            "Woof! My name is %s and I am a %s! I am %d years old! Woof!",
            name, breed, age);
    }

}

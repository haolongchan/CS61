package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();
        correct.addLast(4);
        correct.addLast(5);
        correct.addLast(6);
        buggy.addLast(4);
        buggy.addLast(5);
        buggy.addLast(6);
        assertEquals(correct.size(), buggy.size());
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.removeLast(), buggy.removeLast());

    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();
        int N = 500000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 2);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggy.addLast(randVal);
                assertEquals(L.size(), buggy.size());
//                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 2) {
                // size
                int size = L.size();
                int bsize = buggy.size();
                assertEquals(size, bsize);
//                System.out.println("size: " + size);
            }
            else if (operationNumber == 1) {
                // removelast
                if (L.size() > 0){
                    int lst = L.removeLast();
                    int blst = buggy.removeLast();
                    assertEquals(lst, blst);
//                    System.out.println("removeLast(" + lst + ")");
                }
            }
            else if (operationNumber == 3) {
                // getlast
                if (L.size() > 0){
                    int get;
                    get = L.getLast();
                    int bget = buggy.getLast();
                    assertEquals(get, bget);
//                    System.out.println("Last: " + get);
                }
            }
        }
    }
}

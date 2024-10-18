package deque;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    public void Fillup_Empty_Fillup(){
        ArrayDeque<Double> deque = new ArrayDeque<>();
        for (double i = 0; i < 1000; ++i)
        {
            deque.addLast(i);
        }
        for (double i = 0; i < 1000; ++i){
            assertEquals("buggy Empty", i, (double)(deque.removeFirst()), 0.0);
        }
        for (double i = 0; i < 1000; ++i){
            deque.addLast(i);
        }
        for (double i = 0; i < 1000; ++i){
            assertEquals("buggy Empty", i, (double)(deque.removeFirst()), 0.0);
        }
    }

    @Test
    public void equals(){
        ArrayDeque<Double> deque = new ArrayDeque<>();
        ArrayDeque<Double> deque2 = new ArrayDeque<>();
        for (double i = 0; i < 1000; ++i){
            deque.addLast(i);
            deque2.addLast(i);
        }
        assertTrue(deque.equals(deque2));
    }
}

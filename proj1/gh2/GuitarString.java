package gh2;

// import deque.ArrayDeque;
import deque.Deque;
import deque.LinkedListDeque;

// import java.util.LinkedList;

//Note: This file will not compile until you complete the Deque implementations
public class GuitarString {
    /** Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday. */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor
    private int capacity;

    /* Buffer for storing sound data. */
     private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        capacity = (int) (SR / frequency);
        buffer = new LinkedListDeque<>();
        for (int i = 0; i < capacity; i++) {
            buffer.addLast(0.0);
        }
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.
        buffer = (Deque<Double>) new LinkedListDeque<Double>();
        for (int i = 0; i < capacity; ++i) {
            buffer.addLast(Math.random() - 0.5);
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double first = buffer.removeFirst();
        double second = buffer.getFirst();
        buffer.addLast((first + second) * DECAY / 2);
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        if (buffer != null) {
            return (double) (buffer.getFirst());
        }
        throw new NullPointerException("buffer is null");
    }
}

package deque;

import java.util.Iterator;

public interface Deque<T> {
    public void addFirst(T item);
    public void addLast(T item);
    public T removeFirst();
    public T removeLast();

    default boolean isEmpty() {
        return (size() == 0);
    }

    public int size();
    public void clear();
    public T getFirst();
    public T getLast();
    public void printDeque();
    public T get(int index);
    public Iterator<T> iterator();
    public boolean equals(Object o);

}

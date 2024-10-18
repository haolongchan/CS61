package deque;

import java.util.Iterator;

public interface Deque<Item> {
    public void addFirst(Item item);
    public void addLast(Item item);
    public Item removeFirst();
    public Item removeLast();

    default boolean isEmpty() {
        return (size() == 0);
    }

    public int size();
    public void clear();
    public Item getFirst();
    public Item getLast();
    public void printDeque();
    public Item get(int index);
    public Iterator<Item> iterator();
    public boolean equals(Object o);

}

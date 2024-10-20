package deque;

//import java.util.Collection;
//import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T> {
    public T[] node;
    private int size;
    private T head;
    private T tail;

    public ArrayDeque() {
        node = (T[]) new Object[8];
        size = 0;
        head = null;
        tail = null;
    }

    public T getRecursive(int index) {
        return node[index];
    }

//    @Override
    public T get(int index) {
        return node[index];
    }

//    @Override
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<T> {
        private T current;
        private int index;

        MyIterator() {
            current = head;
            index = 0;
        }

//        @Override
        public boolean hasNext() {
            return index < size;
        }

//        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T item = current;
            current = node[index + 1];
            index++;
            return item;
        }
    }

//    @Override
    public boolean equals(Object o) {
        if (o instanceof ArrayDeque<?>) {
            ArrayDeque<?> other = (ArrayDeque<?>) o;
            if (size != other.size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!node[i].equals(other.node[i])) return false;
            }
            return true;
        }
        return false;
    }

    public void printDeque() {
        for (int i = 0; i < size; ++i) {
            System.out.print(node[i] + " ");
        }
    }

    private void resize(int capacity, int operation) {
        T[] tmp = (T[]) new Object[capacity];
        for (int i = 0; i < size; i++) {
            tmp[i] = node[(i + operation + node.length) % node.length];
        }
        node = tmp;
    }

//    @Override
    public void addFirst(T item) {
        if (size == node.length) {
            resize(node.length * 2, -1);
        }
        node[0] = item;
        size++;
        head = node[0];
        tail = node[size - 1];
    }

//    @Override
    public void addLast(T item) {
        if (size == node.length) {
            resize(node.length * 2, 0);
        }
        node[size] = item;
        size++;
        tail = node[size - 1];
        head = node[0];
    }

//    @Override
    public T removeFirst() {
        if (size == 0){
            return null;
        }
        T first = head;
        size--;
        if (size != 0) {
            resize(size, 1);
            head = node[0];
        }
        else {
            clear();
        }
        return first;
    }

//    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T last = tail;
        --size;
        node[size] = null;
        if (size > 1) {
            resize(size, 0);
            tail = node[size - 1];
        }
        else {
            clear();
        }
        return last;
    }

//    @Override
    public T getFirst() {
        if (size == 0) {
            return null;
        }
        return head;
    }

//    @Override
    public T getLast() {
        if (size == 0) {
            return null;
        }
        return tail;
    }

//    @Override
    public void clear() {
        node = (T[]) new Object[8];
        size = 0;
        head = null;
        tail = null;
    }

//    @Override
    public int size() {
        return size;
    }
}

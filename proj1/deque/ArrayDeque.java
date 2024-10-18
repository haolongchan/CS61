package deque;

import java.util.Collection;
//import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<Item> implements Deque<Item> {
    public Item[] node;
    private int size;
    private Item head;
    private Item tail;

    public ArrayDeque() {
        node = (Item[]) new Object[8];
        size = 0;
        head = null;
        tail = null;
    }

    public Item getRecursive(int index) {
        return node[index];
    }

//    @Override
    public Item get(int index) {
        return node[index];
    }

//    @Override
    public Iterator<Item> iterator() {
        return new myIterator();
    }

    private class myIterator implements Iterator<Item> {
        private Item current;
        private int index;

        myIterator() {
            current = head;
            index = 0;
        }

//        @Override
        public boolean hasNext() {
            return current != null;
        }

//        @Override
        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
            Item item = current;
            current = node[index + 1];
            index++;
            return item;
        }
    }

//    @Override
    public boolean equals(Object o){
        if (o instanceof ArrayDeque<?>){
            ArrayDeque<?> other = (ArrayDeque<?>) o;
            if (size != other.size) return false;
            for (int i = 0; i < size; i++){
                if (!node[i].equals(other.node[i])) return false;
            }
            return true;
        }
        return false;
    }

    public void printDeque() {
        for (int i = 0; i < size; ++i){
            System.out.print(node[i] + " ");
        }
    }

    public void outside_resize(int capacity) {
        Item[] temp = (Item[]) new Object[capacity];
    }

    private void resize(int capacity, int operation){
        Item[] tmp = (Item[]) new Object[capacity];
        for (int i = 0; i < size; i++){
            tmp[i] = node[(i + operation + node.length) % node.length];
        }
        node = tmp;
    }

//    @Override
    public void addFirst(Item item) {
        if (size == node.length){
            resize(node.length * 2, -1);
        }
        node[0] = item;
        size++;
        head = node[0];
        tail = node[size - 1];
    }

//    @Override
    public void addLast(Item item) {
        if (size == node.length){
            resize(node.length * 2, 0);
        }
        node[size] = item;
        size++;
        tail = node[size-1];
        head = node[0];
    }

//    @Override
    public Item removeFirst() {
        if (size == 0){
            return null;
        }
        Item first = head;
        size--;
        if (size != 0){
            resize(size, 1);
            head = node[0];
        }
        else{
            clear();
        }
        return first;
    }

//    @Override
    public Item removeLast() {
        if (size == 0){
            return null;
        }
        Item last = tail;
        --size;
        node[size] = null;
        if (size > 1){
            resize(size, 0);
        }
        else{
            clear();
        }
        return last;
    }

//    @Override
    public Item getFirst() {
        if (size == 0){
            return null;
        }
        return head;
    }

//    @Override
    public Item getLast() {
        if (size == 0){
            return null;
        }
        return tail;
    }

//    @Override
    public void clear() {
        node = (Item[]) new Object[8];
        size = 0;
        head = null;
        tail = null;
    }

//    @Override
    public int size() {
        return size;
    }
}

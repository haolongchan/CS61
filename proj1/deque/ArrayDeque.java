package deque;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class ArrayDeque<Item> implements Deque<Item> {
    private Item[] node;
    private int size;
    private Item head;
    private Item tail;
    private int MAX_EXCEED_CACHE = 50;

    public ArrayDeque() {
        node = (Item[]) new Object[8];
        size = 0;
        head = null;
        tail = null;
    }

    public Item getRecursive(int index) {
        return node[index];
    }

    public Item get(int index) {
        return node[index];
    }

    public boolean equals(Object o){
        if (o instanceof ArrayDeque){
            ArrayDeque other = (ArrayDeque) o;
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

    private void resize(int capacity, int operation){
        Item[] tmp = (Item[]) new Object[capacity];
        for (int i = 0; i < size; i++){
            if (i + operation > -1){
                tmp[i] = node[i + operation];
            }
        }
        node = tmp;
    }

    @Override
    public void addFirst(Item item) {
        if (size == node.length){
            resize(node.length * 2, -1);
        }
        node[0] = item;
        size++;
        head = node[0];
        tail = node[size - 1];
    }

    @Override
    public void addLast(Item item) {
        if (size == node.length){
            resize(node.length * 2, 0);
        }
        node[size] = item;
        size++;
        tail = node[size-1];
        head = node[0];
    }

    @Override
    public boolean offerFirst(Item item) {
        return false;
    }

    @Override
    public boolean offerLast(Item item) {
        return false;
    }

    @Override
    public Item removeFirst() {
        if (size == 0){
            return null;
        }
        Item first = head;
        size--;
        if (size != 0){
            if (node.length - size > MAX_EXCEED_CACHE){
                resize(size, 1);
            }else{
                for (int i = 0; i < size; ++i){
                    node[i] = node[i + 1];
                }
            }
            head = node[0];
        }
        else{
            clear();
        }
        return first;
    }

    @Override
    public Item removeLast() {
        if (size == 0){
            return null;
        }
        Item last = tail;
        --size;
        node[size] = null;
        if (size > 1){
            if (node.length - size > MAX_EXCEED_CACHE){
                resize(size, 0);
            }
            if (size != 0){
                tail = node[size - 1];
            }
        }
        else{
            clear();
        }
        return last;
    }

    @Override
    public Item pollFirst() {
        return null;
    }

    @Override
    public Item pollLast() {
        return null;
    }

    @Override
    public Item getFirst() {
        if (size == 0){
            return null;
        }
        return head;
    }

    @Override
    public Item getLast() {
        if (size == 0){
            return null;
        }
        return tail;
    }

    @Override
    public Item peekFirst() {
        return null;
    }

    @Override
    public Item peekLast() {
        return null;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return false;
    }

    @Override
    public boolean add(Item item) {
        return false;
    }

    @Override
    public boolean offer(Item item) {
        return false;
    }

    @Override
    public Item remove() {
        return null;
    }

    @Override
    public Item poll() {
        return null;
    }

    @Override
    public Item element() {
        return null;
    }

    @Override
    public Item peek() {
        return null;
    }

    @Override
    public boolean addAll(Collection<? extends Item> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        node = (Item[]) new Object[8];
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public void push(Item item) {

    }

    @Override
    public Item pop() {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<Item> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public Iterator<Item> descendingIterator() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        if (size == 0){
            return true;
        }
        return false;
    }
}

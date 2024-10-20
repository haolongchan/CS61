package deque;

//import java.util.Collection;
//import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T> {

    private node sentinel;
    private int size;
    private node head;
    private node tail;

    private class node{
        private node next;
        private node prev;
        private T item;
    }

    public LinkedListDeque(){
        size = 0;
        head = null;
        tail = null;
        sentinel = null;
    }

    public void printDeque(){
        sentinel = head;
        while(sentinel != null){
            System.out.print(sentinel.item + " ");
            sentinel = sentinel.next;
        }
        sentinel = head;
    }

//    @Override
    public T get(int index) {
        Iterator<T> it = iterator();
        for (int i = 0; i < index; i++) {
            it.next();
        }
        return it.next();
    }

//    @Override
    public Iterator<T> iterator() {
        return new myIterator();
    }

    private class myIterator implements Iterator<T>{
        private node current;
        public myIterator(){
            current = head;
        }

//        @Override
        public boolean hasNext() {
            return current != null;
        }

//        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T item = current.item;
            current = current.next;
            return item;
        }
    }

//    @Override
    public void addFirst(T item) {
        node current = new node();
        current.next = null;
        current.prev = null;
        current.item = item;
        if (size == 0){
            head = current;
            tail = current;
        }else{
            head.prev = current;
            current.next = head;
            head = current;
        }

        size++;
    }

//    @Override
    public void addLast(T item) {
        node current = new node();
        current.item = item;
        current.prev = null;
        current.next = null;
        if (size == 0){
            head = current;
            tail = current;
        }else{
            tail.next = current;
            current.prev = tail;
            tail = current;
        }

        size++;
    }

//    @Override
    public T removeFirst() {
        if (size == 0){
            return null;
        }
        if (size == 1){
            node current = head;
            clear();
            return current.item;
        }
        node current = head;
        head = head.next;
        head.prev = null;
        size--;
        return current.item;
    }

//    @Override
    public T removeLast() {
        if (size == 0){
            return null;
        }
        if (size == 1){
            node current = tail;
            clear();
            return current.item;
        }
        node current = tail;
        tail = tail.prev;
        tail.next = null;
        size--;
        return current.item;
    }

    public T getRecursive(int index) {
        if (index == 0){
            T item = sentinel.item;
            sentinel = head;
            return item;
        }
        sentinel = sentinel.next;
        return getRecursive(index - 1);
    }

//    @Override
    public T getFirst() {
        return head.item;
    }

//    @Override
    public T getLast() {
        return tail.item;
    }

//    @Override
    public void clear() {
        size = 0;
        head = null;
        tail = null;
    }

//    @Override
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

}

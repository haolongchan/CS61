package deque;

import java.util.Collection;
//import java.util.Deque;
import java.util.Iterator;

public class LinkedListDeque<Item> implements Deque<Item> {

    private node sentinel;
    private int size;
    private node head;
    private node tail;

    public class node{
        public node next;
        public node prev;
        public Item item;
    }

    public LinkedListDeque(){
        size = 0;
        head = null;
        tail = null;
        sentinel = head;
    }

    public void printDeque(){
        sentinel = head;
        while(head != null){
            System.out.print(head.item + " ");
            head = head.next;
        }
    }

    @Override
    public void addFirst(Item item) {
        node current = new node();
        current.next = head;
        current.prev = null;
        current.item = item;
        if (size == 0){
            head = current;
            tail = current;
        }else{
            head.prev = current;
            head = current;
        }

        size++;
    }

    @Override
    public void addLast(Item item) {
        node current = new node();
        current.prev = tail;
        current.item = item;
        current.next = null;
        if (size == 0){
            head = current;
            tail = current;
        }else{
            tail.next = current;
            tail = current;
        }

        size++;
    }

    @Override
    public Item removeFirst() {
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

    @Override
    public Item removeLast() {
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

    @Override
    public Item getFirst() {
        return head.item;
    }

    @Override
    public Item getLast() {
        return tail.item;
    }

    @Override
    public void clear() {
        size = 0;
        head = null;
        tail = null;
    }

    @Override
    public int size() {
        return size;
    }

}

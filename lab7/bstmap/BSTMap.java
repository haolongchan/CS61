package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/*
* it is just a node of a tree
* */
public class BSTMap<K, V> implements Map61B<K, V>{

    private K keys;
    private V vals;
    private BSTMap<K, V> lson;
    private BSTMap<K, V> rson;
    private int size;


    public BSTMap() {
        keys = null;
        vals = null;
        lson = null;
        rson = null;
        size = 0;
    }

    @Override
    public void clear() {
        keys = null;
        vals = null;
        lson = null;
        rson = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V get(K key) {
        BSTMap<K, V> current = this;
        while(current.keys != null) {
            int com = current.keys.toString().compareTo(key.toString());
            if(com == 0) return current.vals;
            else if(com < 0) current = current.lson;
            else current = current.rson;
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        BSTMap<K, V> current = this;
        while(current.keys != null) {
            int com = current.keys.toString().compareTo(key.toString());
            if (com > 0) current = current.lson;
            else if (com < 0) current = current.rson;
        }
        BSTMap<K, V> temp = new BSTMap<>();
        temp.keys = key;
        temp.vals = value;
        current = temp;
        size++;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super K> action) {
        Map61B.super.forEach(action);
    }

    @Override
    public Spliterator<K> spliterator() {
        return Map61B.super.spliterator();
    }

    public void printInOrder() {

    }
}

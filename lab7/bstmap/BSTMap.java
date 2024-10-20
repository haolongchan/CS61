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
        if (this.keys == null) {
            return false;
        }
        int comp = this.keys.toString().compareTo(key.toString());
        if (comp == 0) {
            return true;
        }
        if (comp < 0) {
            return this.rson.containsKey(key);
        }
        return this.lson.containsKey(key);
    }

    @Override
    public V get(K key) {
        if (this.keys == null) {
            return null;
        }
        int comp = this.keys.toString().compareTo(key.toString());
        if (comp == 0) {
            return this.vals;
        }
        if (comp < 0) {
            return this.rson.get(key);
        }
        return this.lson.get(key);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (this.keys != null) {
            int compare = this.keys.toString().compareTo(key.toString());
            if(compare > 0) {
                this.lson = new BSTMap<>();
                this.lson.put(key, value);
            }
            else if(compare < 0) {
                this.rson = new BSTMap<>();
                this.rson.put(key, value);
            }
        } else {
            this.keys = key;
            this.vals = value;
        }
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

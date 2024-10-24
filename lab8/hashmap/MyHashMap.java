package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    private static final int DEFAULT_INITIAL_SIZE = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;
    private int size;

    @Override
    public void clear() {
        this.size = 0;
        loadFactor = DEFAULT_LOAD_FACTOR;
        buckets = createTable(DEFAULT_INITIAL_SIZE);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public V get(K key) {
        int index = (key.hashCode() & 0x7fffffff) % buckets.length;
        for (Node n : buckets[index]) {
            if (n.key.equals(key)) {
                return n.value;
            }
        }

        return null;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void put(K key, V value) {
        int index = (key.hashCode() & 0x7fffffff) % buckets.length;

        if (buckets[index] != null) {
            for (Node n : buckets[index]) {
                if (n.key.equals(key)) {
                    n.value = value;
                    return;
                }
            }
        }

        buckets[index].add(createNode(key, value));
        size++;

        if ((double) size / buckets.length > this.loadFactor) {
            resize();
        }
    }

    private void resize() {
        Collection<Node>[] old = buckets;
        buckets = createTable(old.length * 2);
        for (int i = 0; i < old.length * 2; i++) {
            buckets[i] = createBucket();
        }

        size = 0;

        for (Collection<Node> c : old) {
            if (c != null) {
                for (Node n : c) {
                    int index = (n.key.hashCode() & 0x7fffffff) % buckets.length;
                    buckets[index].add(createNode(n.key, n.value));
                    size++;
                }
            }
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> c : buckets) {
            if (c != null) {
                for (Node n : c) {
                    keys.add(n.key);
                }
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        int index = (key.hashCode() & 0x7fffffff) % buckets.length;
        for (Node n : buckets[index]) {
            if (n.key.equals(key)) {
                V value = n.value;
                n.value = null;
                return value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        int index = (key.hashCode() & 0x7fffffff) % buckets.length;
        for (Node n : buckets[index]) {
            if (n.key.equals(key)) {
                if (n.value == value) {
                    n.value = null;
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        List<K> list = new ArrayList<>();
        for (Collection<Node> c : buckets) {
            if (c != null) {
                for (Node n : c) {
                    list.add(n.key);
                }
            }
        }

        return list.iterator();
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private double loadFactor;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        this.loadFactor = maxLoad;
        this.size = 0;
        for (int i = 0; i < initialSize; i++) {
            buckets[i] = createBucket();
        }
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

}

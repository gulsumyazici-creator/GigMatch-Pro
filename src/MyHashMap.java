import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Simple HashMap implementation using separate chaining.
 * Supports put, get, remove, containsKey, resize, and values().
 */
public class MyHashMap<K, V> {

    /**
     * Entry stored in a bucket: key–value pair.
     */
    private static class Entry<K,V> {
        final K key;
        V value;
        Entry(K k, V v){ key = k; value = v; }
    }

    /** Default initial capacity. */
    private static final int DEFAULT_CAPACITY = 500;

    /** Load factor threshold for resizing. */
    private static final double LOAD_FACTOR = 0.70;

    /** Array of buckets. Each bucket is a LinkedList of entries. */
    private LinkedList<Entry<K,V>>[] table =
            (LinkedList<Entry<K,V>>[]) new LinkedList[DEFAULT_CAPACITY];

    /** Number of key–value pairs stored. */
    private int size = 0;

    /** Creates a map with default capacity. */
    public MyHashMap(){}

    /**
     * Creates a map with custom capacity.
     */
    public MyHashMap(int capacity){
        table = (LinkedList<Entry<K,V>>[]) new LinkedList[capacity];
    }

    /**
     * Computes bucket index for given key using hash & modulo.
     */
    private int index(Object key){
        int h = (key == null ? 0 : key.hashCode());
        return (h & 0x7fffffff) % table.length;
    }

    /**
     * @return number of stored pairs
     */
    public int size(){
        return size;
    }

    /**
     * Checks if the map contains given key.
     */
    public boolean containsKey(K key){
        return get(key) != null;
    }

    /**
     * Retrieves value for a key, or null if not found.
     */
    public V get(K key){
        int i = index(key);
        LinkedList<Entry<K,V>> bucket = table[i];
        if (bucket == null) return null;

        for (int j = 0, n = bucket.size(); j < n; j++) {
            Entry<K,V> e = bucket.get(j);
            if ((key == null && e.key == null) ||
                    (key != null && key.equals(e.key))) {
                return e.value;
            }
        }
        return null;
    }

    /**
     * Inserts a key–value pair.
     * Triggers resize if load factor exceeded.
     */
    public boolean put(K key, V value){

        if ((size + 1.0) / table.length > LOAD_FACTOR) {
            resize();
        }

        int i = index(key);
        if (table[i] == null) table[i] = new LinkedList<>();
        LinkedList<Entry<K,V>> bucket = table[i];

        bucket.addFirst(new Entry<>(key, value));
        size++;
        return true;
    }

    /**
     * Removes key–value pair if key exists.
     */
    public void remove(K key){
        int i = index(key);
        LinkedList<Entry<K,V>> bucket = table[i];
        if (bucket == null) return;

        for (int j = 0; j < bucket.size(); j++) {
            Entry<K,V> e = bucket.get(j);
            if ((key == null && e.key == null) ||
                    (key != null && key.equals(e.key))) {
                bucket.remove(j);
                size--;
                if (bucket.isEmpty()) table[i] = null;
                return;
            }
        }
    }

    /**
     * Doubles table size and rehashes all entries.
     */
    private void resize(){
        LinkedList<Entry<K,V>>[] old = table;
        table = (LinkedList<Entry<K,V>>[]) new LinkedList[old.length * 2];
        size = 0;

        for (int b = 0; b < old.length; b++) {
            LinkedList<Entry<K,V>> bucket = old[b];
            if (bucket == null) continue;
            for (int j = 0; j < bucket.size(); j++) {
                Entry<K,V> e = bucket.get(j);
                put(e.key, e.value);
            }
        }
    }

    /**
     * Returns all values in the map in an ArrayList.
     */
    public ArrayList<V> values(){
        ArrayList<V> out = new ArrayList<>(size);
        for (int i = 0; i < table.length; i++){
            LinkedList<Entry<K,V>> bucket = table[i];
            if (bucket == null) continue;
            for (int j = 0; j < bucket.size(); j++){
                out.add(bucket.get(j).value);
            }
        }
        return out;
    }
}

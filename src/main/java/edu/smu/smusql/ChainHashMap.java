package edu.smu.smusql;

import java.util.*;

public class ChainHashMap<K, V> {
    private static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private LinkedList<Entry<K, V>>[] table;
    private int size;
    private static final int INITIAL_CAPACITY = 16;

    @SuppressWarnings("unchecked")
    public ChainHashMap() {
        table = new LinkedList[INITIAL_CAPACITY];
        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<>();
        }
        size = 0;
    }

    private int hash(K key) {
        return (key == null) ? 0 : Math.abs(key.hashCode() % table.length);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        LinkedList<Entry<K, V>>[] oldTable = table;
        table = new LinkedList[oldTable.length * 2];
        for (int i = 0; i < table.length; i++) {
            table[i] = new LinkedList<>();
        }
        size = 0; // Reset size to re-add entries
    
        for (LinkedList<Entry<K, V>> bucket : oldTable) {
            for (Entry<K, V> entry : bucket) {
                put(entry.key, entry.value); // Rehash entries into the new table
            }
        }
    }

    public void put(K key, V value) {
        if (size >= table.length * 0.75) {
            resize(); // Check load factor before adding
        }
        int index = hash(key);
        LinkedList<Entry<K, V>> bucket = table[index];

        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value; // Update value if key already exists
                return;
            }
        }

        bucket.add(new Entry<>(key, value));
        size++;
    }

    public V get(K key) {
        int index = hash(key);
        LinkedList<Entry<K, V>> bucket = table[index];

        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                return entry.value; // Return the value if key is found
            }
        }
        return null; // Key not found
    }

    @SuppressWarnings("unchecked")
    private void shrink() {
        if (size <= table.length / 4 && table.length > INITIAL_CAPACITY) {
            // Store the old table
            LinkedList<Entry<K, V>>[] oldTable = table;
            // Create a new smaller table
            table = new LinkedList[oldTable.length / 2];
            for (int i = 0; i < table.length; i++) {
                table[i] = new LinkedList<>();
            }
            
            // Reset the size
            size = 0;
    
            // Rehash and insert all entries into the new table
            for (LinkedList<Entry<K, V>> bucket : oldTable) {
                for (Entry<K, V> entry : bucket) {
                    put(entry.key, entry.value); // Re-add entries to the new table
                }
            }
        }
    }

    public void remove(K key) {
        int index = hash(key);
        LinkedList<Entry<K, V>> bucket = table[index];
    
        Iterator<Entry<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            if (entry.key.equals(key)) {
                iterator.remove(); // Safely remove the entry
                size--;
                shrink();
                return;
            }
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Iterable<K> keys() {
        List<K> keys = new LinkedList<>();
        for (LinkedList<Entry<K, V>> bucket : table) {
            if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    keys.add(entry.key);
                }
            }
        }
        return keys; // Return an iterable of keys
    }


}

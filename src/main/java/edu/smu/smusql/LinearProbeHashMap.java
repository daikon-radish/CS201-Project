package edu.smu.smusql;

import java.util.*;

public class LinearProbeHashMap<K, V> {
    private static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Entry<K, V>[] table;
    private int size;
    private static final int INITIAL_CAPACITY = 16;
    private static final Entry<?, ?> DELETED = new Entry<>(null, null); // Sentinel for deleted slots

    @SuppressWarnings("unchecked")
    public LinearProbeHashMap() {
        table = new Entry[INITIAL_CAPACITY];
        size = 0;
    }

    private int hash(K key) {
        return (key == null) ? 0 : Math.abs(key.hashCode() % table.length);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldTable = table;
        table = new Entry[oldTable.length * 2];
        size = 0;

        for (Entry<K, V> entry : oldTable) {
            if (entry != null && entry != DELETED) {
                put(entry.key, entry.value); // Rehash entries into the new table
            }
        }
    }

    public void put(K key, V value) {
        if (size >= table.length * 0.75) {
            resize(); // Check load factor before adding
        }
        
        int index = hash(key);

        // Linear probing: find the next available or matching slot
        while (table[index] != null && table[index] != DELETED && !table[index].key.equals(key)) {
            index = (index + 1) % table.length;
        }
        
        if (table[index] == null || table[index] == DELETED) {
            size++;
        }
        table[index] = new Entry<>(key, value);
    }

    public V get(K key) {
        int index = hash(key);
        
        // Linear probing: search for the key
        while (table[index] != null) {
            if (table[index] != DELETED && table[index].key.equals(key)) {
                return table[index].value; // Return the value if key is found
            }
            index = (index + 1) % table.length;
        }
        return null; // Key not found
    }

    @SuppressWarnings("unchecked")
    private void shrink() {
        if (size <= table.length / 4 && table.length > INITIAL_CAPACITY) {
            Entry<K, V>[] oldTable = table;
            table = new Entry[oldTable.length / 2];
            size = 0;
    
            for (Entry<K, V> entry : oldTable) {
                if (entry != null && entry != DELETED) {
                    put(entry.key, entry.value); // Rehash entries into the smaller table
                }
            }
        }
    }

    public void remove(K key) {
        int index = hash(key);
        
        // Linear probing: search for the key to remove
        while (table[index] != null) {
            if (table[index] != DELETED && table[index].key.equals(key)) {
                table[index] = (Entry<K, V>) DELETED; // Mark as deleted
                size--;
                
                shrink(); // Directly call shrink; it has its own conditions
                
                return;
            }
            index = (index + 1) % table.length;
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
        for (Entry<K, V> entry : table) {
            if (entry != null && entry != DELETED) {
                keys.add(entry.key);
            }
        }
        return keys;
    }


}

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

    //original hash
    private int hash(K key) {
        return (key == null) ? 0 : Math.abs(key.hashCode() % table.length);
    }

    //bit manipulation hash
    // private int hash(K key) {
    //     int hash = (key == null) ? 0 : key.hashCode();
    //     hash ^= (hash >>> 16);   // XOR the hash with a right-shifted version to mix bits
    //     hash ^= (hash << 5);     // Left shift and XOR to further scramble bits
    //     hash ^= (hash >>> 4);    // Additional shift to spread out bits
    //     return Math.abs(hash % table.length);  // Ensure positive and within bounds
    // }

    //polynomial hash, memory 115MB for 500000 queries
    // private int hash(K key) {
    //     // Ensure the key is not null
    //     if (key == null) {
    //         throw new IllegalArgumentException("Key cannot be null");
    //     }

    //     // Convert the key to a string
    //     String keyStr = key.toString();  // Convert K to String
    //     long p = 31;   // Prime base (common choice)
    //     long m = 1_000_000_007;  // Large prime modulus for minimizing overflow
    //     long hash = 0;  // Use long to avoid overflow
    //     long power = 1; // Use long to avoid overflow

    //     for (int i = 0; i < keyStr.length(); i++) {
    //         hash = (hash + (keyStr.charAt(i) * power) % m) % m; // Character value times power of base
    //         power = (power * p) % m;  // Update the power of p for next character
    //     }
    //     return (int) Math.abs(hash % table.length);  // Ensure positive hash within bounds
    // }

    //cyclic shift hash
    // private int hash(K key) {
    //     int hash = 0;
    //     String strKey = key.toString(); // Convert the key to a string for demonstration
    
    //     for (int i = 0; i < strKey.length(); i++) {
    //         hash = (hash << 5) | (hash >>> 27);  // Left rotate by 5 bits
    //         hash += strKey.charAt(i);            // Add character value
    //     }
    //     return Math.abs(hash % table.length);  // Ensure hash is within bounds
    // }

    //multiplicative hash
    // private int hash(K key) {
    //     double A = 0.6180339887;  // Fractional part of (sqrt(5) - 1) / 2
    //     int hash = (key == null) ? 0 : key.hashCode();
    //     double fractionalPart = (hash * A) % 1;  // Keep only the fractional part
    //     return (int) (table.length * fractionalPart);  // Scale to table size
    // }

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

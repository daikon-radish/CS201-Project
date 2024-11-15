package edu.smu.smusql;

import java.util.*;

public class QuadraticProbeHashMap<K, V> {
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
    public QuadraticProbeHashMap() {
        table = new Entry[INITIAL_CAPACITY];
        size = 0;
    }

    // original hash
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

    //polynomial hash
    // private int hash(String key) {
    //     int p = 31;   // Prime base (common choice)
    //     int m = 1_000_000_007;  // Large prime modulus for minimizing overflow
    //     int hash = 0;
    //     int power = 1;  // To store p^i values
    
    //     for (int i = 0; i < key.length(); i++) {
    //         hash = (hash + (key.charAt(i) * power) % m) % m; // Character value times power of base
    //         power = (power * p) % m;  // Update the power of p for next character
    //     }
    //     return Math.abs(hash % table.length);  // Ensure positive hash within bounds
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
        int i = 0; // Step counter for quadratic probing
    
        // Quadratic probing: find the next available or matching slot
        while (table[Math.abs((index + i * i) % table.length)] != null && 
               table[Math.abs((index + i * i) % table.length)] != DELETED &&
               !table[Math.abs((index + i * i) % table.length)].key.equals(key)) {
            i++;
            // Optional safeguard to avoid infinite loops:
            if (i >= table.length) break; // Prevent infinite loop if no free slot found
        }
    
        index = Math.abs((index + i * i) % table.length); // Final index after probing
        if (table[index] == null || table[index] == DELETED) {
            size++;
        }
        table[index] = new Entry<>(key, value);
    }
    

    public V get(K key) {
        int index = hash(key);
        int i = 0; // Step counter for quadratic probing

        // Quadratic probing: search for the key
        while (table[(index + i * i) % table.length] != null) {
            if (table[(index + i * i) % table.length] != DELETED && 
                table[(index + i * i) % table.length].key.equals(key)) {
                return table[(index + i * i) % table.length].value; // Return the value if key is found
            }
            i++;
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
        int i = 0; // Step counter for quadratic probing

        // Quadratic probing: search for the key to remove
        while (table[(index + i * i) % table.length] != null) {
            if (table[(index + i * i) % table.length] != DELETED && 
                table[(index + i * i) % table.length].key.equals(key)) {
                table[(index + i * i) % table.length] = (Entry<K, V>) DELETED; // Mark as deleted
                size--;
                
                shrink(); // Directly call shrink; it has its own conditions
                
                return;
            }
            i++;
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

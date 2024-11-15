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

    // original hash
    // private int hash(K key) {
    //     return (key == null) ? 0 : Math.abs(key.hashCode() % table.length);
    // }

    //bit manipulation hash
    // private int hash(K key) {
    //     int hash = (key == null) ? 0 : key.hashCode();
    //     hash ^= (hash >>> 16);   // XOR the hash with a right-shifted version to mix bits
    //     hash ^= (hash << 5);     // Left shift and XOR to further scramble bits
    //     hash ^= (hash >>> 4);    // Additional shift to spread out bits
    //     return Math.abs(hash % table.length);  // Ensure positive and within bounds
    // }

    //polynomial hash, memory 115MB for 500000 queries
    private int hash(K key) {
        // Ensure the key is not null
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        // Convert the key to a string
        String keyStr = key.toString();  // Convert K to String
        long p = 31;   // Prime base (common choice)
        long m = 1_000_000_007;  // Large prime modulus for minimizing overflow
        long hash = 0;  // Use long to avoid overflow
        long power = 1; // Use long to avoid overflow

        for (int i = 0; i < keyStr.length(); i++) {
            hash = (hash + (keyStr.charAt(i) * power) % m) % m; // Character value times power of base
            power = (power * p) % m;  // Update the power of p for next character
        }
        return (int) Math.abs(hash % table.length);  // Ensure positive hash within bounds
    }

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
        LinkedList<Entry<K, V>>[] oldTable = table;
        table = new LinkedList[oldTable.length * 2]; //maybe can play around with this to 1.5 to see if improved performance
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

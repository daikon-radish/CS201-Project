package edu.smu.smusql;

import java.util.*;

public class Table {
    private String name;
    private List<String> columns;
    private TreeMap<String, Map<String, String>> bTree; // B-Tree for ordered data
    private HashMap<String, Map<String, String>> hashMap; // HashMap for fast lookups

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
        this.bTree = new TreeMap<>();
        this.hashMap = new HashMap<>();
    }

    public List<String> getColumns() {
        return columns;
    }

    public TreeMap<String, Map<String, String>> getBTree() {
        return bTree;
    }

    public HashMap<String, Map<String, String>> getHashMap() {
        return hashMap;
    }

    public void addRow(String key, Map<String, String> newRow) {
        if (newRow.keySet().containsAll(columns)) {
            hashMap.put(key, newRow); // Add to HashMap
            bTree.put(key, newRow); // Add to TreeMap
        } else {
            throw new IllegalArgumentException("Row must contain all columns");
        }
    }

    public void removeRow(String key) {
        bTree.remove(key);
        hashMap.remove(key);
    }


    //Retrieves a single row by key (uses HashMap)
    public Map<String, String> getRow(String key) {
        return hashMap.get(key); // HashMap for exact match queries
    }

    public List<Map<String, String>> getRowsInRange(String startKey, String endKey) {
        return new ArrayList<>(bTree.subMap(startKey, true, endKey, true).values());
    }

    public Collection<Map<String, String>> getAllRowsOrdered() {
        return bTree.values(); // Ordered retrieval
    }
}

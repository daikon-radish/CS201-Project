package edu.smu.smusql;

import java.util.*;

public class Table {
    private ChainHashMap<String, Map<String, String>> dataList; // Use ChainHashMap for rows
    private String name;
    private List<String> columns;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = columns;
        this.dataList = new ChainHashMap<>(); // Initialize ChainHashMap
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void addRow(String key, Map<String, String> newRow) {
        if (newRow.keySet().containsAll(columns)) {
            dataList.put(key, newRow); // Add the row with the specified key
        } else {
            throw new IllegalArgumentException("Row must contain all columns");
        }
    }

    public Map<String, String> getRow(String key) {
        return dataList.get(key); // Retrieve a row by its key
    }

    public void removeRow(String key) {
        dataList.remove(key); // Remove a row by its key
    }

    public ChainHashMap<String, Map<String, String>> getDataList() {
        return dataList;
    }

    // public List<Map<String, String>> getDataList() {
    //     List<Map<String, String>> rows = new ArrayList<>();
    //     for (String key : dataList.keys()) { // Assuming you have a method to get keys from ChainHashMap
    //         rows.add(dataList.get(key)); // Add each row to the list
    //     }
    //     return rows; // Return the list of rows
    // }


    public void setDataList(ChainHashMap<String, Map<String, String>> dataList) {
        this.dataList = dataList; // Set the dataList to a new ChainHashMap
    }
}

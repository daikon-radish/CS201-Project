package edu.smu.smusql;

import java.util.*;
import java.util.stream.Collectors;

public class Engine {

    private HashMap<String, Table> tableMap = new HashMap<>();


    public String executeSQL(String query) {
        String[] tokens = query.trim().split("\\s+");
        String command = tokens[0].toUpperCase();

        switch (command) {
            case "CREATE":
                return create(tokens);
            case "INSERT":
                return insert(tokens);
            case "SELECT":
                return select(tokens);
            case "UPDATE":
                return update(tokens);
            case "DELETE":
                return delete(tokens);
            default:
                return "ERROR: Unknown command";
        }
    }

    public String insert(String[] tokens) {
        if (tokens.length < 5 || !tokens[1].equalsIgnoreCase("INTO")) {
            return "ERROR: Invalid INSERT INTO syntax.";
        }
    
        String tableName = tokens[2];
    
        // Look up the table in the hash map
        Table tbl = tableMap.get(tableName);
        if (tbl == null) {
            return "ERROR: No such table: " + tableName;
        }
    
        // Extract the values list between parentheses
        String valueList = queryBetweenParentheses(tokens, 4); // Assuming this method is already defined
        if (valueList.startsWith("ERROR:")) {
            return valueList; // Return the error from queryBetweenParentheses
        }
    
        // Split and trim values
        List<String> values = Arrays.stream(valueList.split(","))
                                    .map(String::trim)
                                    .collect(Collectors.toList());
    
        List<String> columns = tbl.getColumns();
    
        // Ensure the number of values matches the number of columns
        if (values.size() != columns.size()) {
            return "ERROR: Column count doesn't match value count.";
        }
    
        Map<String, String> rowData = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            rowData.put(columns.get(i), values.get(i));
        }
    
        // Create a unique key for the row
        String rowKey = generateUniqueKey(rowData); // Implement this method as needed
    
        // Add the new row to the table with the key
        tbl.addRow(rowKey, rowData); 
    
        return "Row inserted into " + tableName;
    }

    public String delete(String[] tokens) {
        if (!tokens[1].toUpperCase().equals("FROM")) {
            return "ERROR: Invalid DELETE syntax";
        }
    
        String tableName = tokens[2];
        Table tbl = tableMap.get(tableName); // Access the table directly using the hash map
    
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }
    
        // Get the data from the table (assuming it's stored in a chain hash map)
        QuadraticProbeHashMap<String, Map<String, String>> tableData = tbl.getDataList(); // Assuming getData() returns your ChainHashMap
        List<String> columns = tbl.getColumns();
    
        // Initialize whereClauseConditions list
        List<String[]> whereClauseConditions = new ArrayList<>();
    
        // Parse WHERE clause conditions
        if (tokens.length > 3 && tokens[3].toUpperCase().equals("WHERE")) {
            for (int i = 4; i < tokens.length; i++) {
                if (tokens[i].toUpperCase().equals("AND") || tokens[i].toUpperCase().equals("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[] {null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }
    
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers
    
        int ct = 0; // count number of rows affected.
        
        // Iterate over the keys in the hash map and check for matches
        Iterable<String> keys = tableData.keys();
        for (String key : keys) {
            Map<String, String> row = tableData.get(key);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);
    
            if (match) {
                tableData.remove(key); // Remove the matching entry
                ct++; // Increment count of affected rows
            }
        }
    
        return "Rows deleted from " + tableName + ". " + ct + " rows affected.";
    }

    public String select(String[] tokens) {
    if (!tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
        return "ERROR: Invalid SELECT syntax.";
    }

    String tableName = tokens[3];
    
    // Use HashMap for direct table lookup
    Table tbl = tableMap.get(tableName);
    if (tbl == null) {
        return "ERROR: No such table: " + tableName;
    }

    QuadraticProbeHashMap<String, Map<String, String>> tableData = tbl.getDataList(); // Assuming getData() returns your ChainHashMap
    List<String> columns = tbl.getColumns();

    // Initialize whereClauseConditions list
    List<String[]> whereClauseConditions = new ArrayList<>();

    // Parse WHERE clause conditions
    if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
        for (int i = 5; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("AND") || tokens[i].equalsIgnoreCase("OR")) {
                // Add AND/OR conditions
                whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
            } else if (isOperator(tokens[i])) {
                // Add condition with operator (column, operator, value)
                String column = tokens[i - 1];
                String operator = tokens[i];
                String value = tokens[i + 1];
                whereClauseConditions.add(new String[] {null, column, operator, value});
                i += 1; // Skip the value since it has been processed
            }
        }
    }

    StringBuilder result = new StringBuilder();
    result.append(String.join("\t", columns)).append("\n"); // Print column headers

    // Filter rows based on WHERE clause
    for (String key : tableData.keys()) {
        Map<String, String> row = tableData.get(key);
        boolean match = evaluateWhereConditions(row, whereClauseConditions);
        if (match) {
            for (String column : columns) {
                result.append(row.getOrDefault(column, "NULL")).append("\t");
            }
            result.append("\n");
        }
    }

    return result.toString();
    }

    public String update(String[] tokens) {
        if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET")) {
            return "ERROR: Invalid UPDATE syntax.";
        }
    
        String tableName = tokens[1];
        Table tbl = tableMap.get(tableName);
        if (tbl == null) {
            return "Error: no such table: " + tableName;
        }
    
        String setColumn = tokens[3]; // column to be updated
        String newValue = tokens[5]; // new value for above column
    
        List<String> columns = tbl.getColumns();
    
        // Retrieve table data
        QuadraticProbeHashMap<String, Map<String, String>> tableData = tbl.getDataList(); // Assuming getData() returns your ChainHashMap
    
        // Initialize whereClauseConditions list
        List<String[]> whereClauseConditions = new ArrayList<>();
    
        // Parse WHERE clause conditions
        if (tokens.length > 6 && tokens[6].equalsIgnoreCase("WHERE")) {
            for (int i = 7; i < tokens.length; i++) {
                if (tokens[i].equalsIgnoreCase("AND") || tokens[i].equalsIgnoreCase("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[]{tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[]{null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }
    
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Print column headers
    
        // Update rows based on WHERE clause
        int ct = 0; // count number of affected rows
        for (String key : tableData.keys()) { // Ensure you have a keys() method to get all keys
            Map<String, String> row = tableData.get(key);
            boolean match = evaluateWhereConditions(row, whereClauseConditions);
            if (match) {
                row.put(setColumn, newValue);
                ct++;
            }
        }
    
        return "Table " + tableName + " updated. " + ct + " rows affected.";
    }

    public String create(String[] tokens) { 
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        String tableName = tokens[2];

        // Check if the table already exists
        if (tableMap.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }

        // Extract the column list between parentheses
        String columnList = queryBetweenParentheses(tokens, 3);
        List<String> columns = Arrays.stream(columnList.split(","))
                                     .map(String::trim)
                                     .collect(Collectors.toList());

        // Create the new table and add it to the hash map
        Table newTable = new Table(tableName, columns);
        tableMap.put(tableName, newTable); // Store table by name

        return "Table " + tableName + " created";
    }

    //Additional methods
    private String queryBetweenParentheses(String[] tokens, int startIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            result.append(tokens[i]).append(" ");
        }
        return result.toString().trim().replaceAll("\\(", "").replaceAll("\\)", "");
    }

// Helper method to determine if a string is an operator
private boolean isOperator(String token) {
    if (token == null) {
        return false; // Handle null input
    }
    Set<String> operators = new HashSet<>(Set.of("=", ">", "<", ">=", "<="));
    return operators.contains(token);
}

private boolean evaluateWhereConditions(Map<String, String> row, List<String[]> conditions) {
    boolean overallMatch = true;
    boolean nextConditionShouldMatch = true; // Default behavior for AND

    for (String[] condition : conditions) {
        if (condition[0] != null) { // AND/OR operator
            nextConditionShouldMatch = condition[0].equals("AND");
        } else {
            // Parse column, operator, and value
            String column = condition[1];
            String operator = condition[2];
            String value = condition[3];

            // Get the row value once
            String rowValue = row.get(column);
            boolean currentMatch = evaluateCondition(rowValue, operator, value);

            if (nextConditionShouldMatch) {
                // Short-circuit for AND: if currentMatch is false, overallMatch is false
                overallMatch = overallMatch && currentMatch;
                // If overallMatch is already false, exit early
                if (!overallMatch) {
                    return false; // Short-circuit
                }
            } else {
                // Short-circuit for OR: if currentMatch is true, overallMatch is true
                overallMatch = overallMatch || currentMatch;
                // If overallMatch is already true, exit early
                if (overallMatch) {
                    return true; // Short-circuit
                }
            }
        }
    }

    return overallMatch; // Return the overall result
}

// Helper method to evaluate a single condition
private boolean evaluateCondition(String columnValue, String operator, String value) {
    if (columnValue == null || value == null) return false;

    // Compare strings as numbers if possible
    boolean isNumeric = isNumeric(columnValue) && isNumeric(value);
    
    if (isNumeric) {
        double columnNumber = Double.parseDouble(columnValue);
        double valueNumber = Double.parseDouble(value);

        switch (operator) {
            case "=": return columnNumber == valueNumber;
            case ">": return columnNumber > valueNumber;
            case "<": return columnNumber < valueNumber;
            case ">=": return columnNumber >= valueNumber;
            case "<=": return columnNumber <= valueNumber;
            default: throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    } else {
        switch (operator) {
            case "=": return columnValue.equals(value);
            case ">": return columnValue.compareTo(value) > 0;
            case "<": return columnValue.compareTo(value) < 0;
            case ">=": return columnValue.compareTo(value) >= 0;
            case "<=": return columnValue.compareTo(value) <= 0;
            default: throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }
}

    // Helper method to determine if a string is numeric
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String generateUniqueKey(Map<String, String> rowData) {
        // Assuming "id" is a column in rowData that contains a unique identifier
        return rowData.get("id"); // Change "id" to your actual column name
    }
    
}

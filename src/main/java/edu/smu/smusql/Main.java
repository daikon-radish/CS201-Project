package edu.smu.smusql;

import java.util.*;
import java.util.stream.Collectors;

// @author ziyuanliu@smu.edu.sg

public class Main {
    /*
     *  Main method for accessing the command line interface of the database engine.
     *  MODIFICATION OF THIS FILE IS NOT RECOMMENDED!
     */
    static Engine dbEngine = new Engine();
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("smuSQL Starter Code version 0.5");
        System.out.println("Have fun, and good luck!");

        while (true) {
            System.out.print("smusql> ");
            String query = scanner.nextLine();
            if (query.equalsIgnoreCase("exit")) {
                break;
            } else if (query.equalsIgnoreCase("evaluate")) {
                long startTime = System.nanoTime();
                autoEvaluate();
                long stopTime = System.nanoTime();
                long elapsedTime = stopTime - startTime;
                double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
                System.out.println("Time elapsed: " + elapsedTimeInSecond + " seconds");
                break;
            } else if (query.equalsIgnoreCase("custom")) {
                customTestCases();
                break;
            }

            System.out.println(dbEngine.executeSQL(query));
        }
        scanner.close();
    }

    //custom test cases
    public static void customTestCases() {
        System.out.println("Setting up database...");
        setupDatabase();
    
        System.out.println("Running custom test cases...");
        runTests();
    }
    
    private static void setupDatabase() {
        // Create tables and insert initial data
        dbEngine.executeSQL("CREATE TABLE student (id, name, age)");
        dbEngine.executeSQL("INSERT INTO student VALUES (1, 'Jon', 22)");
        dbEngine.executeSQL("INSERT INTO student VALUES (2, 'Mary', 25)");
        dbEngine.executeSQL("INSERT INTO student VALUES (3, 'Jane', 23)");
        dbEngine.executeSQL("INSERT INTO student VALUES (4, 'Tom', 28)");
        dbEngine.executeSQL("INSERT INTO student VALUES (5, 'Ali', 22)");
    }
    
    private static List<String> formatResult(String rawResult) {
        // Split raw result into lines and normalize spacing for each line
        return Arrays.stream(rawResult.split("\n"))
            .map(line -> line.trim().replaceAll("\\s+", " ")) // Trim and normalize spacing
            .collect(Collectors.toList());
    }

    private static void runTests() {
        List<TestCase> testCases = new ArrayList<>();
        // Define test cases
        testCases.add(new TestCase(
            "SELECT * FROM student",
            List.of(
                "id name age",
                "1 'Jon' 22",
                "2 'Mary' 25",
                "3 'Jane' 23",
                "4 'Tom' 28",
                "5 'Ali' 22"
            )
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE id >= 3 AND age = 22",
            List.of(
                "id name age",
                "5 'Ali' 22"
            )
        ));
        testCases.add(new TestCase(
            "INSERT INTO student VALUES (6, 'Sara', 24)",
            List.of("Row inserted into student")
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE id = 6",
            List.of(
                "id name age",
                "6 'Sara' 24"
            )
        ));
        testCases.add(new TestCase(
            "UPDATE student SET age = 26 WHERE id = 2",
            List.of("Table student updated. 1 rows affected.")
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE id = 2",
            List.of(
                "id name age",
                "2 'Mary' 26"
            )
        ));
        testCases.add(new TestCase(
            "DELETE FROM student WHERE id = 4",
            List.of("Rows deleted from student. 1 rows affected.")
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE id = 4",
            List.of("id name age") // Expect only headers since no rows match
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE age >= 23",
            List.of(
                "id name age",
                "2 'Mary' 26",
                "3 'Jane' 23",
                "6 'Sara' 24"
            )
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE id <= 3 AND age < 24",
            List.of(
                "id name age",
                "1 'Jon' 22",
                "3 'Jane' 23"
            )
        ));
        testCases.add(new TestCase(
            "SELECT * FROM student WHERE id <= 3 OR age > 25",
            List.of(
                "id name age",
                "1 'Jon' 22",
                "2 'Mary' 26",
                "3 'Jane' 23"
            )
        ));

        // Execute and validate each test case
        for (TestCase testCase : testCases) {
            System.out.println("Executing query: " + testCase.query);
            // Normalize the raw result
            String rawResult = dbEngine.executeSQL(testCase.query);
            List<String> actualResult = formatResult(rawResult);

            // Check for strict row-by-row match
            boolean passed = testCase.expectedResults.equals(actualResult);

            System.out.println("Test " + (passed ? "PASSED" : "FAILED"));
            if (!passed) {
                System.out.println("Expected: " + testCase.expectedResults);
                System.out.println("Actual: " + actualResult);
            }
            System.out.println();
        }
    }

    
    private static class TestCase {
        String query;
        List<String> expectedResults;
    
        TestCase(String query, List<String> expectedResults) {
            this.query = query;
            this.expectedResults = expectedResults;
        }
    }

    /*
     *  Below is the code for auto-evaluating your work.
     *  DO NOT CHANGE ANYTHING BELOW THIS LINE!
     */
    public static void autoEvaluate() {

        // Set the number of queries to execute
        int numberOfQueries = 1000000;

        // Create tables
        dbEngine.executeSQL("CREATE TABLE users (id, name, age, city)");
        dbEngine.executeSQL("CREATE TABLE products (id, name, price, category)");
        dbEngine.executeSQL("CREATE TABLE orders (id, user_id, product_id, quantity)");

        // Random data generator
        Random random = new Random();

        // Prepopulate the tables in preparation for evaluation
        prepopulateTables(random);

        // Start the timer
        long startTime = System.nanoTime();

        //track memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);


        // Loop to simulate millions of queries
        for (int i = 0; i < numberOfQueries; i++) {
            int queryType = random.nextInt(6);  // Randomly choose the type of query to execute

            switch (queryType) {
                case 0:  // INSERT query
                    insertRandomData(random);
                    break;
                case 1:  // SELECT query (simple)
                    selectRandomData(random);
                    break;
                case 2:  // UPDATE query
                    updateRandomData(random);
                    break;
                case 3:  // DELETE query
                    deleteRandomData(random);
                    break;
                case 4:  // Complex SELECT query with WHERE, AND, OR, >, <, LIKE
                    complexSelectQuery(random);
                    break;
                case 5:  // Complex UPDATE query with WHERE
                    complexUpdateQuery(random);
                    break;
            }

            // Print progress every 100,000 queries
            if (i % 10000 == 0){
                long currentTime = System.nanoTime();
                double elapsedSeconds = (currentTime - startTime) / 1_000_000_000.0;
                System.out.println("Processed " + i + " queries...");
                System.out.println("Time after processing " + i + " queries: " + elapsedSeconds + " seconds");
            }
        }

        System.out.println("Finished processing " + numberOfQueries + " queries.");

        // Measure memory usage
        long usedMemoryAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.println("Memory used: " + (usedMemoryAfter - usedMemoryBefore) + " MB");
    }

    private static void prepopulateTables(Random random) {
        System.out.println("Prepopulating users");
        // Insert initial users
        for (int i = 0; i < 50; i++) {
            String name = "User" + i;
            int age = 20 + (i % 41); // Ages between 20 and 60
            String city = getRandomCity(random);
            String insertCommand = String.format("INSERT INTO users VALUES (%d, '%s', %d, '%s')", i, name, age, city);
            dbEngine.executeSQL(insertCommand);
        }
        System.out.println("Prepopulating products");
        // Insert initial products
        for (int i = 0; i < 50; i++) {
            String productName = "Product" + i;
            double price = 10 + (i % 990); // Prices between $10 and $1000
            String category = getRandomCategory(random);
            String insertCommand = String.format("INSERT INTO products VALUES (%d, '%s', %.2f, '%s')", i, productName, price, category);
            dbEngine.executeSQL(insertCommand);
        }
        System.out.println("Prepopulating orders");
        // Insert initial orders
        for (int i = 0; i < 50; i++) {
            int user_id = random.nextInt(9999);
            int product_id = random.nextInt(9999);
            int quantity = random.nextInt(1, 100);
            String category = getRandomCategory(random);
            String insertCommand = String.format("INSERT INTO orders VALUES (%d, %d, %d, %d)", i, user_id, product_id, quantity);
            dbEngine.executeSQL(insertCommand);
        }
    }

    // Helper method to insert random data into users, products, or orders table
    private static void insertRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Insert into users table
                int id = random.nextInt(10000) + 10000;
                String name = "User" + id;
                int age = random.nextInt(60) + 20;
                String city = getRandomCity(random);
                String insertUserQuery = "INSERT INTO users VALUES (" + id + ", '" + name + "', " + age + ", '" + city + "')";
                dbEngine.executeSQL(insertUserQuery);
                break;
            case 1: // Insert into products table
                int productId = random.nextInt(1000) + 10000;
                String productName = "Product" + productId;
                double price = 50 + (random.nextDouble() * 1000);
                String category = getRandomCategory(random);
                String insertProductQuery = "INSERT INTO products VALUES (" + productId + ", '" + productName + "', " + price + ", '" + category + "')";
                dbEngine.executeSQL(insertProductQuery);
                break;
            case 2: // Insert into orders table
                int orderId = random.nextInt(10000) + 1;
                int userId = random.nextInt(10000) + 1;
                int productIdRef = random.nextInt(1000) + 1;
                int quantity = random.nextInt(10) + 1;
                String insertOrderQuery = "INSERT INTO orders VALUES (" + orderId + ", " + userId + ", " + productIdRef + ", " + quantity + ")";
                dbEngine.executeSQL(insertOrderQuery);
                break;
        }
    }

    // Helper method to randomly select data from tables
    private static void selectRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        String selectQuery;
        switch (tableChoice) {
            case 0:
                selectQuery = "SELECT * FROM users";
                break;
            case 1:
                selectQuery = "SELECT * FROM products";
                break;
            case 2:
                selectQuery = "SELECT * FROM orders";
                break;
            default:
                selectQuery = "SELECT * FROM users";
        }
        dbEngine.executeSQL(selectQuery);
    }

    // Helper method to update random data in the tables
    private static void updateRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Update users table
                int id = random.nextInt(10000) + 1;
                int newAge = random.nextInt(60) + 20;
                String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE id = " + id;
                dbEngine.executeSQL(updateUserQuery);
                break;
            case 1: // Update products table
                int productId = random.nextInt(1000) + 1;
                double newPrice = 50 + (random.nextDouble() * 1000);
                String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE id = " + productId;
                dbEngine.executeSQL(updateProductQuery);
                break;
            case 2: // Update orders table
                int orderId = random.nextInt(10000) + 1;
                int newQuantity = random.nextInt(10) + 1;
                String updateOrderQuery = "UPDATE orders SET quantity = " + newQuantity + " WHERE id = " + orderId;
                dbEngine.executeSQL(updateOrderQuery);
                break;
        }
    }

    // Helper method to delete random data from tables
    private static void deleteRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Delete from users table
                int userId = random.nextInt(10000) + 1;
                String deleteUserQuery = "DELETE FROM users WHERE id = " + userId;
                dbEngine.executeSQL(deleteUserQuery);
                break;
            case 1: // Delete from products table
                int productId = random.nextInt(1000) + 1;
                String deleteProductQuery = "DELETE FROM products WHERE id = " + productId;
                dbEngine.executeSQL(deleteProductQuery);
                break;
            case 2: // Delete from orders table
                int orderId = random.nextInt(10000) + 1;
                String deleteOrderQuery = "DELETE FROM orders WHERE id = " + orderId;
                dbEngine.executeSQL(deleteOrderQuery);
                break;
        }
    }

    // Helper method to execute a complex SELECT query with WHERE, AND, OR, >, <, LIKE
    private static void complexSelectQuery(Random random) {
        int tableChoice = random.nextInt(2);  // Complex queries only on users and products for now
        String complexSelectQuery;
        switch (tableChoice) {
            case 0: // Complex SELECT on users
                int minAge = random.nextInt(20) + 20;
                int maxAge = minAge + random.nextInt(30);
                String city = getRandomCity(random);
                complexSelectQuery = "SELECT * FROM users WHERE age > " + minAge + " AND age < " + maxAge;
                break;
            case 1: // Complex SELECT on products
                double minPrice = 50 + (random.nextDouble() * 200);
                double maxPrice = minPrice + random.nextDouble() * 500;
                complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice + " AND price < " + maxPrice;
                break;
            case 2: // Complex SELECT on products
                double minPrice2 = 50 + (random.nextDouble() * 200);
                String category = getRandomCategory(random);
                complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice2 + " AND category = " + category;
                break;
            default:
                complexSelectQuery = "SELECT * FROM users";
        }
        dbEngine.executeSQL(complexSelectQuery);
    }

    // Helper method to execute a complex UPDATE query with WHERE
    private static void complexUpdateQuery(Random random) {
        int tableChoice = random.nextInt(2);  // Complex updates only on users and products for now
        switch (tableChoice) {
            case 0: // Complex UPDATE on users
                int newAge = random.nextInt(60) + 20;
                String city = getRandomCity(random);
                String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE city = '" + city + "'";
                dbEngine.executeSQL(updateUserQuery);
                break;
            case 1: // Complex UPDATE on products
                double newPrice = 50 + (random.nextDouble() * 1000);
                String category = getRandomCategory(random);
                String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE category = '" + category + "'";
                dbEngine.executeSQL(updateProductQuery);
                break;
        }
    }

    // Helper method to return a random city
    private static String getRandomCity(Random random) {
        String[] cities = {"New York", "Los Angeles", "Chicago", "Boston", "Miami", "Seattle", "Austin", "Dallas", "Atlanta", "Denver"};
        return cities[random.nextInt(cities.length)];
    }

    // Helper method to return a random category for products
    private static String getRandomCategory(Random random) {
        String[] categories = {"Electronics", "Appliances", "Clothing", "Furniture", "Toys", "Sports", "Books", "Beauty", "Garden"};
        return categories[random.nextInt(categories.length)];
    }
}
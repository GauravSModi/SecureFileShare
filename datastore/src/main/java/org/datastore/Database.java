package org.datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

/*
    CREATE TABLE files (
        file_id STRING PRIMARY KEY,
        file_name TEXT NOT NULL,
        user_id TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE (user_id, file_name)
    );
*/

public class Database {

    private static Database instance;
    private static Connection conn;

    private Database() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Create a connection to the database
            // If the database doesn't exist, it will be created automatically
            String dbURL = "jdbc:sqlite:sample.db"; // Relative path
            // String dbURL = "jdbc:sqlite:/full/path/to/sample.db"; // Absolute path

            conn = DriverManager.getConnection(dbURL);

            if (conn != null) {
                System.out.println("Connected to database.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found.");
        } catch (SQLException e) {
            System.out.println("Error connecting to the database.");
        }
    }

    public static Database getInstance() {
        try {
            if (instance == null) {
                instance = new Database();
            } else if (instance.getConnection().isClosed()) {
                instance = new Database();
            }
        } catch (SQLException e) {
            System.err.println("Couldn't get db instance: " + e.getMessage());
            return null;
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }


    public static void closeDatabaseInstance() {
        try {
            if (conn != null) {
                System.out.println("Closing db...");
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Couldn't close db (maybe already closed)");
        }
    }
}

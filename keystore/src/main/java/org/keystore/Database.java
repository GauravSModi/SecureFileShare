package org.keystore;

import javax.xml.crypto.Data;
import java.sql.*;


    /* Tables:

            CREATE TABLE users (
                user_id TEXT PRIMARY KEY,
                public_key TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );


            CREATE TABLE files (
                file_id TEXT PRIMARY KEY,
                file_name TEXT NOT NULL,
                encrypted_fek BLOB NOT NULL,
                owner_id TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (owner_id) REFERENCES users(user_id)
            );


            CREATE TABLE file_access (
                file_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                encrypted_fek_for_user BLOB NOT NULL,
                granted_by TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (file_id, user_id),
                FOREIGN KEY (file_id) REFERENCES files(file_id),
                FOREIGN KEY (user_id) REFERENCES users(user_id),
                FOREIGN KEY (granted_by) REFERENCES users(user_id)
            );


    */


public class Database {

    private static Database instance;
    private static Connection conn;

//    private String url = "jdbc:sqlite://localhost:PORT/mydatabase";

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
                System.out.println("Connected to database");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found");
        } catch (SQLException e) {
            System.out.println("Error connecting to the database");
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
        }
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }


    public String registerUser(String userId, String publicKey) {
        String sql = "INSERT INTO users (user_id, public_key) VALUES (?, ?)";

        try {
            PreparedStatement statement = conn.prepareStatement(sql);

            statement.setString(1, userId);
            statement.setString(2, publicKey);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                return "Success";
            }

            return "Write to database failed. User not registered.";
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Write to database failed. User not registered.";
        }
    }

    public boolean checkUserExists(String userId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, userId);

            statement.executeQuery();

            try (ResultSet res = statement.executeQuery()) {
                System.out.println("Result of sql statement: " + res);
                return res.next();
            }
        }
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



package org.datastore;

import java.sql.*;
import java.util.Base64;
import java.util.UUID;

/*
    CREATE TABLE file_metadata (
        file_id STRING PRIMARY KEY,
        file_name TEXT NOT NULL,
        user_id TEXT NOT NULL,
        hmac BLOB NOT NULL,
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


    public void storeFileMetadata(String fileId, String fileName, String userId, byte[] hmac) throws SQLException {
        String sql = "INSERT INTO file_metadata " +
                "(file_id, file_name, user_id, hmac)" +
                "VALUES (?, ?, ?, ?)";

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, fileId);
            statement.setString(2, fileName);
            statement.setString(3, userId);
            statement.setBytes(4, hmac);

            int rowsUpdated = statement.executeUpdate();

//            if (rowsUpdated != 1) {
//                throw new Exception("Error");
//            }
        }
    }

    public byte[] getHmac(String fileId, String userId) throws SQLException {
        String sql = "Select hmac FROM file_metadata WHERE file_id = ? AND user_id = ?";

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, fileId);
            statement.setString(2, userId);

            try (
                    ResultSet res = statement.executeQuery();
            ) {
                return res.getBytes("hmac");
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

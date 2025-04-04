package org.keystore;

import javax.crypto.SecretKey;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.UUID;


    /* Tables:

        Keystore tables:
            users
            file_access

        datastore tables:
            files


            CREATE TABLE users (
                user_id TEXT PRIMARY KEY,
                public_key TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );

            CREATE TABLE files (
                file_id STRING PRIMARY KEY,
                file_name TEXT NOT NULL,
                user_id TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (user_id, file_name)
            );

            CREATE TABLE file_access (
                file_id STRING NOT NULL,
                file_name TEXT NOT NULL,
                user_id TEXT NOT NULL,
                encrypted_fek_for_user BLOB NOT NULL,
                granted_by TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (file_id, user_id),
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

    public String registerUser(String userId, String publicKey) throws SQLException {
        String sql = "INSERT INTO users (user_id, public_key) VALUES (?, ?)";

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {

            statement.setString(1, userId.toLowerCase());
            statement.setString(2, publicKey);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                return "Success";
            }

            return "Write to database failed. User not registered.";
        }
    }

    public boolean checkUserExists(String userId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, userId.toLowerCase());

            try (
                    ResultSet res = statement.executeQuery()
            ) {
                System.out.println("Result of sql statement: " + res);
                return res.next();
            }
        }
    }

    public String getUserPublicKey(String userId) throws SQLException {
        String sql = "SELECT public_key FROM users WHERE user_id = ?";

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            statement.setString(1, userId.toLowerCase());

            try (
                    ResultSet res = statement.executeQuery();
            ) {
                return res.getString("public_key");
            }
        }
    }


    public boolean registerFile(UUID fileId, String fileName, String userId, SecretKey fek, String grantedBy) throws SQLException {
        String sql = "INSERT INTO file_access (file_id, file_name, user_id, encrypted_fek_for_user, granted_by) VALUES (?, ?, ?, ?, ?)";
        byte[] fekBytes = fek.getEncoded();

        try (
                PreparedStatement statement = conn.prepareStatement(sql);
        ) {

            statement.setString(1, fileId.toString());
            statement.setString(2, fileName);
            statement.setString(3, userId.toLowerCase());
//            statement.setBlob(4, fekBytes);
            statement.setBytes(4, fekBytes);
            statement.setString(5, grantedBy.toLowerCase());

            int rowsAffected = statement.executeUpdate();

            return rowsAffected == 1; // True if succeeded
        }
    }


//    CREATE TABLE file_access (
//            file_id STRING NOT NULL,
//            file_name TEXT NOT NULL,
//            user_id TEXT NOT NULL,
//            encrypted_fek_for_user BLOB NOT NULL,
//            granted_by TEXT NOT NULL,
//            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//            PRIMARY KEY (file_id, user_id),
//            FOREIGN KEY (user_id) REFERENCES users(user_id),
//            FOREIGN KEY (granted_by) REFERENCES users(user_id)
//            );


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



package org.keystore;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    Connection conn;

    Database() {
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

                // Execute SQL commands here

            }
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found");
//            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error connecting to the database");
//            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Couldn't close (maybe already closed)");
//                e.printStackTrace();
            }
        }
    }

}

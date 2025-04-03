package org.keystore;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable {

    private final ServerSocket serverSocket;
    private final Socket clientSocket;

    public ClientHandler(ServerSocket serverSocket, Socket clientSocket) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
    }

    private String handleCommand(String command) throws IOException {
        String[] commandArgs = command.split(" ");
        String method = commandArgs[0];

        switch (method.toLowerCase()) {
            case "checkuserexists":
                if (commandArgs.length != 2) {
                    return "Error: Incorrect number of arguments provided. Could not register user.\n";
                }
                return checkUserExists(commandArgs[1]);
            case "register":
                if (commandArgs.length != 2) {
                    return "Error: Incorrect number of arguments provided. Could not register user.\n";
                }
                return registerNewUser();

            case "stop":
                Main.cont = false;
                serverSocket.close();
                return "Keystore Server exiting\n";
            default:
                return "Error: Unknown Command";
        }
    }

    private String checkUserExists(String userId) {
        try{
            if (Database.getInstance().checkUserExists(userId)){
                return "user exists\n";
            } else {
                return "user does not exist\n";
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while checking if user exists: " + e.getMessage());
            return "error\n";
        }
    }

    private String registerNewUser() {
        // Get user public key from client



        // Try the database

        // return result
//        return Database.getInstance().registerUser(userId, publicKey);
        return "";
    }

    @Override
    public void run() {

        // Create input buffer to get user command
        try (
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();
        ) {
            // Get user command
            String command = new BufferedReader(new InputStreamReader(in)).readLine();

            System.out.println(command);

            // Reply to user
            String response = handleCommand(command);
            out.write(response.getBytes());

        } catch (IOException e) {
            // Should return something to client probably
            System.err.println(e.getMessage());
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

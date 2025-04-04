package org.keystore;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

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
            case "generatefek":
                if (commandArgs.length != 4) {
                    return "Incorrect number of arguments provided. Could not generate FEK.\n";
                }
                return generateFek(commandArgs);

            case "checkuserexists":
                if (commandArgs.length != 2) {
                    return "Incorrect number of arguments provided. Could not check if user exists.\n";
                }
                return checkUserExists(commandArgs[1]);
            case "getuserpublickey":
                if (commandArgs.length != 2) {
                    return "Incorrect number of arguments provided. Could not get user's public key.\n";
                }
                return getUserPublicKey(commandArgs);
            case "register":
                if (commandArgs.length <= 2) {
                    return "Incorrect number of arguments provided. Could not register user.\n";
                }
                return registerNewUser(command);

            case "stop":
                Main.cont = false;
                serverSocket.close();
                return "Keystore Server exiting\n";
            default:
                return "Unknown Command";
        }
    }

    private String generateFek(String[] commandArgs) {

        UUID fileId = UUID.fromString(commandArgs[1]);
        String fileName = commandArgs[2];
        String userId = commandArgs[3];

        // Generate an FEK
        SecretKey fek = null;
        try {
            fek = KeyManager.generateAESKey();
        } catch (NoSuchAlgorithmException e) {
            return e.getMessage();
        }

        // Encrypt the fek with the user's public key
        byte[] encryptedFek = null;
        PublicKey userPublicKey = null;

        if (Database.getInstance() == null) {
            return "Error with keystore database.";
        }

        try {
            userPublicKey = KeyManager.parsePublicKey(Database.getInstance().getUserPublicKey(userId));
        } catch (Exception e) {
            return "Error getting user public key.";
        }

        try {
            encryptedFek = KeyManager.encryptFEK(fek, userPublicKey);
        } catch (Exception e) {
            return "Error encrypting FEK.";
        }

        // Register the file into the db
        try {
            if (Database.getInstance().registerFile(fileId, fileName, userId, fek, userId)) {
                System.out.println("DB Success");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return "Error registering the file into keystore.";
        }

        // Return the FEK for the client to encrypt file

        return "";
    }

    private String checkUserExists(String userId) {
        try {
            if (Database.getInstance().checkUserExists(userId.toLowerCase())) {
                return "user exists\n";
            } else {
                return "user does not exist\n";
            }
        } catch (SQLException e) {
            System.err.println("Error occurred while checking if user exists: " + e.getMessage());
            return "error\n";
        }
    }

    private String registerNewUser(String command) {
        String[] commandArgs = command.split(" ", 3);

        // Get user public key from client
        String userId = commandArgs[1].toLowerCase();
        String flattenedPublicKey = commandArgs[2];

        String publicKey = flattenedPublicKey.replace("[newline]", "\n");

//        publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
//                             .replace("-----END PUBLIC KEY-----", "")
//                             .replace("\\s", "")
//                             .replace("[newline]", "");

        // Try the database
        try {
            return Database.getInstance().registerUser(userId, publicKey);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "Write to database failed. User not registered.";
        }
    }

    private String getUserPublicKey(String[] commandArgs) {
        String userId = commandArgs[1].toLowerCase();
        String flattenedPublicKey = null;

        try {
            String publicKey = Database.getInstance().getUserPublicKey(userId);

            flattenedPublicKey = publicKey.replace("\n", "[newline]");

//            byte[] temp = Base64.getDecoder().decode(res);
//            String publicKey = "-----BEGIN PUBLIC KEY-----\n"
//                    + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
//                    + "\n-----END PUBLIC KEY-----\n";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return flattenedPublicKey;
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

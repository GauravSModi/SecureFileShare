package org.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler {

    public static void handleCommand(String command) {
        String[] commandArgs = command.split(" ");

        if (commandArgs.length == 0) {
            System.err.println("No arguments provided.");
            return;
        }

        switch (commandArgs[0].toLowerCase()) {
            case "upload":
                uploadFile(commandArgs);
                return;
            case "user":
                checkUserLoggedIn();
                return;
            case "register":
                registerUser(commandArgs);
                return;
            case "login":
                loginUser(commandArgs);
                return;
            case "logout":
                logoutUser();
                return;
            case "help":
                printHelpMessage();
                return;
            case "quit":
                Main.cont = false;
                return;
            default:
                System.err.println("Incorrect command.");
        }
    }

    private static void uploadFile(String[] commandArgs) {
        if (commandArgs.length != 2 && !commandArgs[1].isEmpty()) {
            System.err.println("Incorrect number of arguments provided. Please seek help.");
            return;
        }

        // Make sure user is "logged in"
        if (!KeyManager.getInstance().checkUserLoggedIn()) {
            System.err.println("Please log in first with your key.");
            return;
        }

        String fileName = commandArgs[1];
        File f = new File(fileName);
        if (!f.exists()) {
            System.err.println("No such file. Please make sure file name and path are correct.");
            return;
        }
        if (!f.isFile()) {
            System.err.println("No folders, only files.");
            return;
        }

        // Create UUID
        UUID fileUUID = EncryptionUtil.generateUUID();

        // Send keystore a request to generate encryption keys, and store the file name/uuid


        // Encrypt the file using the given keys

        // Send the encrypted file to datastore
    }

    private static void checkUserLoggedIn() {
        if (KeyManager.getInstance().checkUserLoggedIn()) {
            System.out.println(KeyManager.getInstance().getUser() + " logged in.");
        } else {
            System.out.println("There is no user logged in currently.");
        }
    }

    private static void registerUser(String[] commandArgs) {
        if (commandArgs.length != 2 && !commandArgs[1].isEmpty()) {
            System.err.println("Incorrect number of arguments provided. Please seek help.");
            return;
        }

        if (!commandArgs[1].matches("^[a-zA-Z0-9]+$")) {
            System.err.println("Username can only contain alphanumeric characters.");
            return;
        }

        // Check if there's already a user logged in
        if (KeyManager.getInstance().checkUserLoggedIn()) {
            System.out.println("User already logged in.");
            return;
        }

        String userId = commandArgs[1].toLowerCase();

        // Have to check if the user already exists in keystore db
        // TODO: Should move this check to the keystore db so I don't have to make 2 calls
        try {
            if (Networking.checkUserExists(userId)) {
                System.out.println("Username already taken. Please choose a different username.");
                return;
            }
        } catch (IOException e) {
            System.err.println("Error checking if user already exists while registering user: " + e.getMessage());
            return;
        }

        String publicKey = null;
        String privateKey = null;

        // If unique, create RSA keypair
        try {
            String[] keys = KeyManager.getInstance().createRSAKeyPair(userId);
            publicKey = keys[0];
            privateKey = keys[1];
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Error creating RSA key pair while registering user: " + e.getMessage());
            return;
        }

        // Send keystore server the public_key.pem file
        if (publicKey != null) {
            try {
                if (Networking.sendPublicKeyAndReceiveConfirmationOfUserCreation(userId, publicKey)) {
                    KeyManager.getInstance().setUserAndPrivateKey(userId, privateKey);
                    System.out.println("Registered successfully! Please keep your private key safe.");
                } else {
                    System.out.println("Uh oh. Something went wrong. Please try again later.");
                    // TODO: but if everything fails at the last second, should delete the rsa keypair that was created?
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error registering user: " + e.getMessage());
                return;
            }
        }
    }

    private static void loginUser(String[] commandArgs) {
        if (commandArgs.length != 2 && !commandArgs[1].isEmpty()) {
            System.err.println("Incorrect number of arguments provided. Please seek help.");
            return;
        }

        if (!commandArgs[1].matches("^[a-zA-Z0-9]+$")) {
            System.err.println("Username can only contain alphanumeric characters, so that username's obviously incorrect. Please seek help.");
            return;
        }

        // TODO: Can also have a different message for when the same user is logged in
        if (KeyManager.getInstance().checkUserLoggedIn()) {
            System.err.println("A user is already logged in. Please log out first.");
            return;
        }

        String userId = commandArgs[1].toLowerCase();

        // Step 1: get both keys
        String userPrivateKey = null;
        String userPublicKey = null;


        // Step 1a: Get the private key file
        // Assume the key is in the current directory, and is called userId+"_private_key.pem" for now
        Path privateKeyPath = Paths.get("./" + userId + "_private_key.pem");

        if (!Files.exists(privateKeyPath) || !Files.isRegularFile(privateKeyPath)) {
            System.err.println("Couldn't find the private key associated with your user.");
            return;
        }

        // Step 1b: Read the privateKey file into string
        try (
                Stream<String> privateKeyStream = Files.lines(privateKeyPath);
        ) {
            userPrivateKey = privateKeyStream
                    .collect(Collectors.joining("\n"));

            if (!userPrivateKey.contains("-----BEGIN PRIVATE KEY-----") ||
                    !userPrivateKey.contains("-----END PRIVATE KEY-----")) {
                throw new IllegalArgumentException("Invalid PEM format: missing headers/footers");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Step 1c: Get the publicKey string from the keystore
        // Send request to keystore for public key associated with user
        try {
            userPublicKey = Networking.getUserPublicKey(userId);
            if (userPublicKey.equalsIgnoreCase("error")) {
                throw new Exception("Something went wrong in keystore server.");
            }

        } catch (Exception e) {
            System.err.println("Had trouble getting public key from keystore: " + e.getMessage());
            return;
        }

        // Step 1d: Parse both key strings to Key objects
        // Parse public and private keys
        PublicKey publicKey = null;
        PrivateKey privateKey = null;
        try {
            publicKey = KeyManager.parsePublicKey(userPublicKey);
            privateKey = KeyManager.parsePrivateKey(userPrivateKey);
        } catch (GeneralSecurityException e) {
            System.err.println("Trouble parsing keys while logging in user: " + e.getMessage());
            return;
        }

        // Step 2: Verify private key using public key
        boolean success = EncryptionUtil.isKeyPairValid(privateKey, publicKey);

        if (success) {
            KeyManager.getInstance().setUserAndPrivateKey(userId, userPrivateKey);
            System.out.println("Successfully logged in!");
        } else {
            System.out.println("Login not successful. Make sure you have the correct private key and username.");
        }
    }

    private static void logoutUser() {
        KeyManager.getInstance().logoutUser();
        System.out.println("User logged out.");
    }

    private static void printHelpMessage() {
        System.out.println("Usage:");
        System.out.println("    register <userId>               - Register a new user");
        System.out.println("    login <userId>                  - Login as an existing user");
        System.out.println("    logout                          - Logout current user");
        System.out.println("    user                            - Check current login status");
//        System.out.println("    upload <filePath>               - Upload and encrypt a file");
//        System.out.println("    download <fileId>               - Download and decrypt a file");
//        System.out.println("    share <fileId> <userId>         - Share file with another user");
//        System.out.println("    revoke <fileId> <userId>        - Revoke file access from user");
//        System.out.println("    list                            - List all accessible files");
        System.out.println("    help                            - Show this help message");
        System.out.println("    quit                            - Exit the application");
//        System.out.println("  Help:   help");
    }
}

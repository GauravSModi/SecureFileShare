package org.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class CommandHandler {

    public static void handleCommand(String command) {
        String[] commandArgs = command.split(" ");

        if (commandArgs.length == 0) {
            System.err.println("No arguments provided.");
            return;
        }

        switch (commandArgs[0].toLowerCase()) {
            case "register":
                registerUser(commandArgs);
                return;
            case "login":
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

    private static void registerUser(String[] commandArgs) {
        if (commandArgs.length != 2 && !commandArgs[1].isEmpty() && commandArgs[1].matches("^[a-zA-Z0-9]+$")) {
            System.err.println("Incorrect number of arguments provided. Please seek help.");
            return;
        }

        // Check if there's already a user logged in
        if (KeyManager.getInstance().checkUserLoggedIn()) {
            System.out.println("User already logged in.");
            return;
        }

        String userId = commandArgs[1];

        // Have to check if the user already exists in keystore db
        try {
            Networking.checkUserExists(userId);
        } catch (IOException e) {
            System.err.println("Error checking if user already exists while registering user: " + e.getMessage());
            return;
        }

        // If unique, create RSA keypair
        try {
            KeyManager.getInstance().createRSAKeyPair(userId);
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Error creating RSA key pair while registering user: " + e.getMessage());
            return;
        }

        // Send keystore server the public_key.pem file

    }

    private static void logoutUser() {
        KeyManager.getInstance().logoutUser();
    }

    private static void printHelpMessage() {
        System.out.println("Usage:");
        System.out.println("    Register new user: register <userId>");
//        System.out.println("          (default port: 12345)");
//        System.out.println("  Client: client <host> <port>");
//        System.out.println("  Help:   help");
    }
}

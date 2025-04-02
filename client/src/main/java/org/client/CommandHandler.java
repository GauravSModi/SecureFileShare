package org.client;

public class CommandHandler {

    public static void handleCommand(String command) {
        String[] commandArgs = command.split(" ");

        if (commandArgs.length == 0) {
            System.err.println("No arguments provided.");
            return;
        }

        switch (commandArgs[0].toLowerCase()) {
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

    private static void printHelpMessage() {
        System.out.println("Usage:");
        System.out.println("    Register new user: register <userId>");
//        System.out.println("          (default port: 12345)");
//        System.out.println("  Client: client <host> <port>");
//        System.out.println("  Help:   help");
    }
}

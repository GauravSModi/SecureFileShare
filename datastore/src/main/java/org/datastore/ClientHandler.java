package org.datastore;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final ServerSocket serverSocket;
    private final Socket clientSocket;

    public ClientHandler(ServerSocket serverSocket, Socket clientSocket) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
    }

    private String handleCommand(String command) throws IOException {
        String[] commandArgs = command.split(" ");
        String method = commandArgs[0];

//        System.out.println(command);

        switch (method.toLowerCase()) {
            case "store":
                // store <fileId> <fileName> <userId> <fileContent>
                // TODO: how to store file and file metadata together?
                if (commandArgs.length != 5) {
                    return "Error: Incorrect number of arguments provided. Could not store file.\n";
                }
                storeFile(commandArgs);
                return "\n";

            case "stop":
                Main.cont = false;
                serverSocket.close();
                return "Datastore Server exiting\n";

            default:
                return "Unknown Command";
        }
    }

    private void storeFile(String[] commandArgs) {
        // fileId, fileName, userId
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

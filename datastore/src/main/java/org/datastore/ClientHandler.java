package org.datastore;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

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

        System.out.println(command);

        switch (method.toLowerCase()) {
            case "store":
                // store <fileId> <fileName> <userId> <hmac> <fileContent>
                // TODO: Store file in filesystem and file metadata in sqlite db
                if (commandArgs.length != 6) {
                    return "Error: Incorrect number of arguments provided. Could not store file.\n";
                }
                return storeFile(commandArgs);

            case "get":
                // get <fileId> <fileName> <userId>
                if (commandArgs.length != 2) {
                    return "Error: Incorrect number of arguments provided. Could not get file.\n";
                }
                return getFile(commandArgs);

            case "stop":
                Main.cont = false;
                serverSocket.close();
                return "Datastore Server exiting\n";

            default:
                return "Unknown Command";
        }
    }

    private String getFile(String[] commandArgs) {
        String fileId = commandArgs[1];
        String fileName = commandArgs[2];
        String userId = commandArgs[3];

        // Do I need to get metadata?

        // Retrieve file
        try {
            byte[] encryptedFileContent = FileUtil.readFile(userId, fileName);

            return Base64.getEncoder().encodeToString(encryptedFileContent);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String storeFile(String[] commandArgs) {
        // fileId, fileName, userId
        String fileId = commandArgs[1];
        String fileName = commandArgs[2];
        String userId = commandArgs[3];
        byte[] hmac = Base64.getDecoder().decode(commandArgs[4]);
        byte[] encryptedFileContent = Base64.getDecoder().decode(commandArgs[5]);

        if (Database.getInstance() == null) {
            return "Error with datastore database.";
        }

        try {
            Database.getInstance().storeFileMetadata(fileId, fileName, userId, hmac);

            // Create a file in the "file system" (just local for now)
            FileUtil.saveFile(userId, fileName, encryptedFileContent);

            return "Success";
        } catch (Exception e) {
            // TODO: Rollback any changes that were made
            System.err.println(e.getMessage());
            // e.getErrorCode() == 19 should be primary/unique constraint failed i think?
            return "error: " + e.getMessage();
        }
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

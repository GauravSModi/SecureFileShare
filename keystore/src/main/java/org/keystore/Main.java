package org.keystore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int PORT = 13131;
    public static volatile boolean cont = true;

    public static void main(String[] args) {

        System.out.println("Starting keystore!");

        try (
                ServerSocket keyStoreSocket = new ServerSocket(PORT);
        ) {

            while (cont) {
                // Accept an incoming connection
                Socket clientSocket = keyStoreSocket.accept();
                ClientHandler handler = new ClientHandler(keyStoreSocket, clientSocket);
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
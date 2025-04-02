package org.client;

import java.io.*;
import java.net.Socket;

public class Networking {
    private static int KEYSTORE_PORT = 13131;
    private static int DATASTORE_PORT = 31313;

    public static boolean checkUserExists(String userId) throws IOException {
        return sendMessageToKeystore("CheckUserExists " + userId).equalsIgnoreCase("exists");
    }

    private static String sendMessageToKeystore(String message) throws IOException {

        try (
                Socket sock = new Socket("localhost", KEYSTORE_PORT);
        ) {
            // Create buffer to get reply
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            out.println(message);

            return in.readLine();
        }
    }

}

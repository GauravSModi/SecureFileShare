package org.client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Networking {
    private static int KEYSTORE_PORT = 13131;
    private static int DATASTORE_PORT = 31313;

    public static boolean checkUserExists(String userId) throws IOException {
        return sendMessageToKeystore("CheckUserExists " + userId).equalsIgnoreCase("exists");
    }

    public static boolean sendPublicKeyAndReceiveConfirmationOfUserCreation(String userId, byte[] publicKey) throws Exception {

            String res = sendMessageToKeystore("Register " + userId + " " + Arrays.toString(publicKey));
            if (res.equalsIgnoreCase("success")) {
                return true;
            } else {
                throw new Exception(res);
            }
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

package org.client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.UUID;

public class Networking {
    private static int KEYSTORE_PORT = 13131;
    private static int DATASTORE_PORT = 31313;

    public static boolean checkUserExists(String userId) throws IOException {
        return sendMessageToKeystore("CheckUserExists " + userId).equalsIgnoreCase("user exists");
    }

    public static String getUserPublicKey(String userId) throws IOException {
        String flattenedPublicKey = sendMessageToKeystore("getUserPublicKey " + userId);
        String publicKey = flattenedPublicKey.replace("[newline]", "\n");
        return publicKey;
    }

    public static boolean sendPublicKeyAndReceiveConfirmationOfUserCreation(String userId, String publicKey) throws Exception {

        String flattenedPublicKey = publicKey.replace("\n", "[newline]");

        String res = sendMessageToKeystore("Register " + userId + " " + flattenedPublicKey);
        if (res != null && res.equalsIgnoreCase("success")) {
            return true;
        } else {
            throw new Exception(res);
        }
    }

    public static String generateFileEncryptionKey(UUID fileId, String fileName, String userId) throws IOException {
        return sendMessageToKeystore("generateFek " + fileId.toString() + " " + fileName + " " + userId);
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

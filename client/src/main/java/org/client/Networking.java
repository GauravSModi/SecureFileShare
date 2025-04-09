package org.client;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.UUID;

public class Networking {
    private static int KEYSTORE_PORT = 13131;
    private static int DATASTORE_PORT = 31313;

    public static boolean checkUserExists(String userId) throws IOException {
        return sendMessage("CheckUserExists " + userId, KEYSTORE_PORT).equalsIgnoreCase("user exists");
    }

    public static String getUserPublicKey(String userId) throws IOException {
        String flattenedPublicKey = sendMessage("getUserPublicKey " + userId, KEYSTORE_PORT);
        String publicKey = flattenedPublicKey.replace("[newline]", "\n");
        return publicKey;
    }

    public static boolean sendPublicKeyAndReceiveConfirmationOfUserCreation(String userId, String publicKey) throws Exception {

        String flattenedPublicKey = publicKey.replace("\n", "[newline]");

        String res = sendMessage("Register " + userId + " " + flattenedPublicKey, KEYSTORE_PORT);
        if (res != null && res.equalsIgnoreCase("success")) {
            return true;
        } else {
            throw new Exception(res);
        }
    }

    public static String generateFileEncryptionKey(UUID fileId, String fileName, String userId) throws IOException {
        return sendMessage("generateFek " + fileId.toString() + " " + fileName + " " + userId, KEYSTORE_PORT);
    }

    public static String sendEncryptedFileToDatastore(UUID fileId,
                                                      String fileName,
                                                      String userId,
                                                      byte[] hmac,
                                                      byte[] encryptedContentByteArray
    ) throws IOException {
        // store <fileId> <fileName> <userId> <hmac> <fileContent>
        String hmacString = Base64.getEncoder().encodeToString(hmac);
        String encryptedContentString = Base64.getEncoder().encodeToString(encryptedContentByteArray);

        String messageString = "store " +
                fileId.toString() + " " +
                fileName + " " +
                userId + " " +
                hmacString + " " +
                encryptedContentString;

        return sendMessage(messageString, DATASTORE_PORT);
    }

    public static String getEncryptedFek(String fileName, String userId) throws IOException {
        String messageString = "retrievefek " + fileName + " " + userId;

        return sendMessage(messageString, KEYSTORE_PORT);
    }


    private static String sendMessage(String message, int port) throws IOException {

        try (
                Socket sock = new Socket("localhost", port);
        ) {
            // Create buffer to get reply
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            out.println(message);

            return in.readLine();
        }
    }
}

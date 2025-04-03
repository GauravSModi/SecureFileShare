package org.client;

/*
 *  Singleton KeyPairManager class
 *
 *   - Create RSA key pair
 *   - Store the user one
 *   - Transmit the keystore one to the server
 *       - via a networking class singleton?
 *           - or should a new socket be created each time? wouldn't that be a new port each time?
 */


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyManager {

    private static KeyManager instance;

    private String userId;
    private byte[] privateKey;

    private KeyManager() {
        this.userId = null;
        this.privateKey = null;
    }

    private KeyManager(String userId, byte[] privateKey) {
        this.userId = userId;
        this.privateKey = privateKey;
    }

    public static KeyManager getInstance() {
        if (instance == null) {
            instance = new KeyManager();
        }
        return instance;
    }

    public String getUser() {
        return userId;
    }

    public boolean checkUserLoggedIn() {
        return this.userId != null && this.privateKey != null;
    }

    public void logoutUser() {
        this.userId = null;
        this.privateKey = null;
    }

    public byte[] createRSAKeyPair(String userId) throws NoSuchAlgorithmException, IOException {
        // Make sure there's not already a privatekey

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Save keys to files

        String publicKey = "-----BEGIN PUBLIC KEY-----\n"
                         + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
                         + "\n-----END PUBLIC KEY-----\n";

        String privateKey = "-----BEGIN PRIVATE KEY-----\n"
                         + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                         + "\n-----END PRIVATE KEY-----\n";

        Files.write(Paths.get(userId + "_private_key.pem"), privateKey.getBytes());
//        Files.write(Paths.get(userId + "_public_key.pem"), publicKey.getBytes());d

        this.userId = userId;
        this.privateKey = privateKey.getBytes();

        return publicKey.getBytes();
    }


}

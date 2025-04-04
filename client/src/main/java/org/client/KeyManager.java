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
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyManager {

    private static KeyManager instance;

    private String userId;
    private String privateKey;

    private KeyManager() {
        this.userId = null;
        this.privateKey = null;
    }

    private KeyManager(String userId, String privateKey) {
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
        return this.userId;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public boolean checkUserLoggedIn() {
        return this.userId != null && this.privateKey != null;
    }

    public void setUserAndPrivateKey(String userId, String privateKey){
        this.userId = userId;
        this.privateKey = privateKey;
    }

    // Todo: Implement
    public boolean loginUser(String userId) {

        return false;
    }

    public void logoutUser() {
        this.userId = null;
        this.privateKey = null;
    }


    public String[] createRSAKeyPair(String userId) throws NoSuchAlgorithmException, IOException {
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

        return new String[] {publicKey, privateKey};
    }

    public static PublicKey parsePublicKey(String publicKeyPem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKey = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encodedKey = Base64.getDecoder().decode(publicKey);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    public static PrivateKey parsePrivateKey(String privateKeyPem) throws GeneralSecurityException {
        String privateKey = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encodedKey = Base64.getDecoder().decode(privateKey);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }
}

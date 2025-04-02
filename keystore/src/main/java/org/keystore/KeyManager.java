package org.keystore;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyManager {


    public static void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

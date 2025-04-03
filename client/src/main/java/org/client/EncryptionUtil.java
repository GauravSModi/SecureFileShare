package org.client;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

public class EncryptionUtil {

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }

    public static boolean isKeyPairValid(PrivateKey privateKey, PublicKey publicKey) {

        try {
            // Step 1: Encrypt a test message with the public key
            String testMessage = "KeyPairValidationTest";
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(testMessage.getBytes());

            // Step 2: Decrypt with the private key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(encrypted);

            // Step 3: Verify the decrypted message matches the original
            return testMessage.equals(new String(decrypted));
        } catch (GeneralSecurityException e) {
            // Keys are not a valid pair or algorithm mismatch
//            System.err.println("Something went wrong when checking if key pair is valid: " + e.getMessage());
            return false;
        }
    }

}

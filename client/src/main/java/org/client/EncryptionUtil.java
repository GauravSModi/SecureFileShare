package org.client;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
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

    public static SecretKey decryptFEK(byte[] encryptedFEK, PrivateKey userPrivateKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, userPrivateKey);
        byte[] decryptedKeyBytes = rsaCipher.doFinal(encryptedFEK);
        return new SecretKeySpec(decryptedKeyBytes, "AES");
    }

    public static void encryptFile(byte[] content, SecretKey fek, String encryptedFileName) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, fek, spec);
        byte[] encryptedFileContent = aesCipher.doFinal(content);

        writeEncryptedFile(encryptedFileContent, aesCipher, encryptedFileName);
    }
    public static void writeEncryptedFile(byte[] encryptedFile, Cipher cipher, String outputPath) throws Exception {
        // Extract IV and tag from the cipher (GCM-specific)
        byte[] iv = cipher.getIV(); // 12 bytes

        // Combine IV + encrypted data + tag into a single byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);          // Write IV (12 bytes)
        outputStream.write(encryptedFile); // Write encrypted data

//        System.out.println(outputStream.size());

        // Write to disk
        Files.write(Paths.get(outputPath), outputStream.toByteArray());
    }
}

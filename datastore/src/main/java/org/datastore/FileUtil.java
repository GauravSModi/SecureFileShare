package org.datastore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static String generateEncryptedFileName(String fileName) {
        return fileName.replaceAll("[.]", "_") + "otherway" + ".enc";
    }

    public static void saveEncryptedFile(String userId, String fileName, byte[] encryptedFileContent) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(encryptedFileContent);

        // TODO: Technically should make sure userId can be a valid folder name

        Path dir = Paths.get("encrypted_files/" + userId);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            Files.createDirectory(dir);
        }

        Path path = dir.resolve(generateEncryptedFileName(fileName));
        Files.write(path, encryptedFileContent);
    }
}

package org.keystore.types;

public class FileIdAndFek {
    private String fileId;
    private byte[] fek;

    public FileIdAndFek(String fileId, byte[] fek) {
        this.fileId = fileId;
        this.fek = fek;
    }

    public String getFileId() {
        return this.fileId;
    }

    public byte[] getFek() {
        return this.fek;
    }
}

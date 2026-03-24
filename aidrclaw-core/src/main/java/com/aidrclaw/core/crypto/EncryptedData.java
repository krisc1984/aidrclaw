package com.aidrclaw.core.crypto;

import java.util.Base64;

public class EncryptedData {
    private final byte[] ciphertext;
    private final byte[] iv;

    public EncryptedData(byte[] ciphertext, byte[] iv) {
        this.ciphertext = ciphertext;
        this.iv = iv;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public byte[] getIv() {
        return iv;
    }

    public String getCiphertextBase64() {
        return Base64.getEncoder().encodeToString(ciphertext);
    }

    public String getIvBase64() {
        return Base64.getEncoder().encodeToString(iv);
    }

    public static EncryptedData fromBase64(String ciphertextBase64, String ivBase64) {
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        return new EncryptedData(ciphertext, iv);
    }

    public int getSize() {
        return ciphertext.length + iv.length;
    }
}

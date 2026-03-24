package com.aidrclaw.core.crypto;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
public class EncryptionUtils {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
        return keyGenerator.generateKey();
    }

    public static EncryptedData encrypt(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] ciphertext = cipher.doFinal(data);

        return new EncryptedData(ciphertext, iv);
    }

    public static byte[] decrypt(EncryptedData encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.getIv());
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        return cipher.doFinal(encryptedData.getCiphertext());
    }

    public static SecretKey generateKeyFromString(String keyString) throws Exception {
        byte[] keyBytes = keyString.getBytes();
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("密钥字符串必须是 32 字节（256 位）");
        }
        return new javax.crypto.spec.SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    public static String keyToString(SecretKey key) {
        return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey stringToKey(String keyString) throws Exception {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(keyString);
        return new javax.crypto.spec.SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
}

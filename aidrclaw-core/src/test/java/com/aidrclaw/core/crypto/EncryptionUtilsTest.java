package com.aidrclaw.core.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilsTest {

    @Test
    void testGenerateKey() throws Exception {
        SecretKey key = EncryptionUtils.generateKey();
        
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length);
    }

    @Test
    void testEncryptAndDecrypt() throws Exception {
        SecretKey key = EncryptionUtils.generateKey();
        byte[] originalData = "Hello, World!".getBytes(StandardCharsets.UTF_8);

        EncryptedData encryptedData = EncryptionUtils.encrypt(originalData, key);
        
        assertNotNull(encryptedData.getCiphertext());
        assertNotNull(encryptedData.getIv());
        assertNotEquals(originalData.length, encryptedData.getCiphertext().length);

        byte[] decryptedData = EncryptionUtils.decrypt(encryptedData, key);
        
        assertArrayEquals(originalData, decryptedData);
    }

    @Test
    void testDifferentIvForEachEncryption() throws Exception {
        SecretKey key = EncryptionUtils.generateKey();
        byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

        EncryptedData encrypted1 = EncryptionUtils.encrypt(data, key);
        EncryptedData encrypted2 = EncryptionUtils.encrypt(data, key);

        assertArrayEquals(encrypted1.getCiphertext(), encrypted2.getCiphertext());
        assertFalse(java.util.Arrays.equals(encrypted1.getIv(), encrypted2.getIv()));
    }

    @Test
    void testDecryptWithWrongKey() throws Exception {
        SecretKey key1 = EncryptionUtils.generateKey();
        SecretKey key2 = EncryptionUtils.generateKey();
        byte[] data = "Secret data".getBytes(StandardCharsets.UTF_8);

        EncryptedData encryptedData = EncryptionUtils.encrypt(data, key1);

        assertThrows(Exception.class, () -> {
            EncryptionUtils.decrypt(encryptedData, key2);
        });
    }

    @Test
    void testTamperedDataDetection() throws Exception {
        SecretKey key = EncryptionUtils.generateKey();
        byte[] data = "Important data".getBytes(StandardCharsets.UTF_8);

        EncryptedData encryptedData = EncryptionUtils.encrypt(data, key);
        
        byte[] tamperedCiphertext = encryptedData.getCiphertext().clone();
        tamperedCiphertext[0] ^= 0xFF;
        EncryptedData tamperedData = new EncryptedData(tamperedCiphertext, encryptedData.getIv());

        assertThrows(Exception.class, () -> {
            EncryptionUtils.decrypt(tamperedData, key);
        });
    }

    @Test
    void testKeyToStringConversion() throws Exception {
        SecretKey originalKey = EncryptionUtils.generateKey();
        
        String keyString = EncryptionUtils.keyToString(originalKey);
        SecretKey restoredKey = EncryptionUtils.stringToKey(keyString);
        
        assertArrayEquals(originalKey.getEncoded(), restoredKey.getEncoded());
    }

    @Test
    void testGenerateKeyFromString() throws Exception {
        String keyString = "0123456789abcdef0123456789abcdef";
        
        SecretKey key = EncryptionUtils.generateKeyFromString(keyString);
        
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length);
    }

    @Test
    void testGenerateKeyFromStringInvalidLength() {
        String invalidKeyString = "too short";
        
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptionUtils.generateKeyFromString(invalidKeyString);
        });
    }

    @Test
    void testEncryptedDataBase64Conversion() throws Exception {
        SecretKey key = EncryptionUtils.generateKey();
        byte[] data = "Test data for Base64".getBytes(StandardCharsets.UTF_8);

        EncryptedData original = EncryptionUtils.encrypt(data, key);

        EncryptedData fromBase64 = EncryptedData.fromBase64(
            original.getCiphertextBase64(),
            original.getIvBase64()
        );

        byte[] decrypted = EncryptionUtils.decrypt(fromBase64, key);
        
        assertArrayEquals(data, decrypted);
    }

    @Test
    void testEncryptedDataSize() throws Exception {
        byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
        SecretKey key = EncryptionUtils.generateKey();

        EncryptedData encryptedData = EncryptionUtils.encrypt(data, key);

        assertEquals(data.length + 12, encryptedData.getSize());
    }
}

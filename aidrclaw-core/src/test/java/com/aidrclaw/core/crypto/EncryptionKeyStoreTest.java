package com.aidrclaw.core.crypto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionKeyStoreTest {

    private EncryptionKeyStore keyStore;
    private String testKeystorePath;
    private char[] testPassword;

    @BeforeEach
    void setUp() throws Exception {
        keyStore = new EncryptionKeyStore();
        testKeystorePath = Files.createTempFile("test-keystore", ".jceks").toString();
        testPassword = "test-password".toCharArray();
        
        keyStore.initialize(testKeystorePath, testPassword);
    }

    @AfterEach
    void tearDown() {
        keyStore.destroy();
        new File(testKeystorePath).delete();
    }

    @Test
    void testInitialize() {
        assertNotNull(keyStore.getMasterKey());
    }

    @Test
    void testGenerateKeyId() {
        String keyId1 = keyStore.generateKeyId();
        String keyId2 = keyStore.generateKeyId();
        
        assertNotNull(keyId1);
        assertNotNull(keyId2);
        assertNotEquals(keyId1, keyId2);
    }

    @Test
    void testStoreAndGetKey() throws Exception {
        String keyId = keyStore.generateKeyId();
        SecretKey key = EncryptionUtils.generateKey();
        
        keyStore.storeKey(keyId, key);
        
        SecretKey retrievedKey = keyStore.getKey(keyId);
        
        assertNotNull(retrievedKey);
        assertArrayEquals(key.getEncoded(), retrievedKey.getEncoded());
    }

    @Test
    void testContainsKey() throws Exception {
        String keyId = keyStore.generateKeyId();
        SecretKey key = EncryptionUtils.generateKey();
        
        assertFalse(keyStore.containsKey(keyId));
        
        keyStore.storeKey(keyId, key);
        
        assertTrue(keyStore.containsKey(keyId));
    }

    @Test
    void testGetMasterKey() {
        SecretKey masterKey = keyStore.getMasterKey();
        
        assertNotNull(masterKey);
        assertEquals("AES", masterKey.getAlgorithm());
    }

    @Test
    void testGetNonExistentKey() {
        SecretKey key = keyStore.getKey("non-existent-key");
        
        assertNull(key);
    }

    @Test
    void testKeystorePersistence() throws Exception {
        String keyId = keyStore.generateKeyId();
        SecretKey key = EncryptionUtils.generateKey();
        
        keyStore.storeKey(keyId, key);
        keyStore.destroy();

        EncryptionKeyStore keyStore2 = new EncryptionKeyStore();
        keyStore2.initialize(testKeystorePath, testPassword);
        
        SecretKey retrievedKey = keyStore2.getKey(keyId);
        
        assertNotNull(retrievedKey);
        assertArrayEquals(key.getEncoded(), retrievedKey.getEncoded());
        
        keyStore2.destroy();
    }

    @Test
    void testDestroy() {
        keyStore.destroy();
        
        assertThrows(NullPointerException.class, () -> {
            keyStore.getMasterKey();
        });
    }
}

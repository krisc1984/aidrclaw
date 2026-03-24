package com.aidrclaw.core.crypto;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.UUID;

@Slf4j
public class EncryptionKeyStore {

    private static final String KEYSTORE_TYPE = "JCEKS";
    private static final String MASTER_KEY_ALIAS = "master-key";

    private KeyStore keyStore;
    private String keystorePath;
    private char[] password;

    public void initialize(String keystorePath, char[] password) {
        this.keystorePath = keystorePath;
        this.password = password;

        try {
            keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

            File keystoreFile = new File(keystorePath);
            if (keystoreFile.exists()) {
                try (FileInputStream fis = new FileInputStream(keystoreFile)) {
                    keyStore.load(fis, password);
                }
                log.info("Keystore 加载成功：{}", keystorePath);
            } else {
                keyStore.load(null, password);
                log.info("创建新的 Keystore: {}", keystorePath);
            }

            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                SecretKey masterKey = EncryptionUtils.generateKey();
                keyStore.setEntry(
                    MASTER_KEY_ALIAS,
                    new KeyStore.SecretKeyEntry(masterKey),
                    new KeyStore.PasswordProtection(password)
                );
                save();
                log.info("主密钥生成并保存");
            }

        } catch (Exception e) {
            log.error("Keystore 初始化失败", e);
            throw new IllegalStateException("Keystore 初始化失败：" + e.getMessage(), e);
        }
    }

    public void save() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, password);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IOException("Keystore 保存失败", e);
        }
    }

    public SecretKey getKey(String keyId) {
        try {
            KeyStore.Entry entry = keyStore.getEntry(keyId, new KeyStore.PasswordProtection(password));
            if (entry instanceof KeyStore.SecretKeyEntry) {
                return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
            }
            return null;
        } catch (Exception e) {
            log.error("获取密钥失败：{}", keyId, e);
            return null;
        }
    }

    public String generateKeyId() {
        return UUID.randomUUID().toString();
    }

    public void storeKey(String keyId, SecretKey key) {
        try {
            keyStore.setEntry(
                keyId,
                new KeyStore.SecretKeyEntry(key),
                new KeyStore.PasswordProtection(password)
            );
            save();
            log.info("密钥保存成功：{}", keyId);
        } catch (Exception e) {
            log.error("保存密钥失败：{}", keyId, e);
            throw new RuntimeException("保存密钥失败：" + e.getMessage(), e);
        }
    }

    public SecretKey getMasterKey() {
        return getKey(MASTER_KEY_ALIAS);
    }

    public boolean containsKey(String keyId) {
        try {
            return keyStore.containsAlias(keyId);
        } catch (KeyStoreException e) {
            log.error("检查密钥存在失败：{}", keyId, e);
            return false;
        }
    }

    public void destroy() {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = 0;
            }
            password = null;
        }
        keyStore = null;
        log.info("Keystore 已销毁");
    }
}

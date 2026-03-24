package com.aidrclaw.plugin.storage;

import com.aidrclaw.core.crypto.EncryptedData;
import com.aidrclaw.core.crypto.EncryptionKeyStore;
import com.aidrclaw.core.crypto.EncryptionUtils;
import com.aidrclaw.core.plugin.PluginManager;
import com.aidrclaw.plugin.*;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class EncryptionArchivePlugin implements Plugin {

    private static final String PLUGIN_ID = "encryption-archive";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_NAME = "加密归档插件";
    private static final String PLUGIN_DESCRIPTION = "AES-256-GCM 加密存储插件";

    @Autowired
    private EncryptionKeyStore encryptionKeyStore;

    @Autowired
    private PluginManager pluginManager;

    private String storagePluginId;

    @Override
    public void init(PluginContext context) {
        this.storagePluginId = context.getConfigString("storage-plugin-id");
        if (this.storagePluginId == null) {
            this.storagePluginId = "local-storage";
        }
        log.info("EncryptionArchivePlugin 初始化完成，使用存储插件：{}", storagePluginId);
    }

    @Override
    public PluginResult execute(PluginContext context) {
        String action = context.getInputString("action");

        if (action == null) {
            return PluginResult.error("缺少 action 参数");
        }

        return switch (action) {
            case "saveEncrypted" -> saveEncrypted(context);
            case "loadDecrypted" -> loadDecrypted(context);
            default -> PluginResult.error("未知操作：" + action);
        };
    }

    private PluginResult saveEncrypted(PluginContext context) {
        try {
            InputStream inputStream = context.getInput("inputStream", InputStream.class);
            String businessType = context.getInputString("businessType");
            String businessId = context.getInputString("businessId");

            if (inputStream == null) {
                return PluginResult.error("缺少文件数据");
            }

            byte[] fileBytes = inputStream.readAllBytes();
            String fileHash = Hashing.sha256().hashBytes(fileBytes).toString();

            SecretKey key = encryptionKeyStore.getMasterKey();
            if (key == null) {
                return PluginResult.error("主密钥不存在");
            }

            EncryptedData encryptedData = EncryptionUtils.encrypt(fileBytes, key);
            String keyId = encryptionKeyStore.generateKeyId();
            encryptionKeyStore.storeKey(keyId, key);

            String objectKey = "encrypted/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd/HHmmss")) + "-" + UUID.randomUUID() + ".enc";

            InputStream encryptedStream = new ByteArrayInputStream(encryptedData.getCiphertext());
            PluginContext storageContext = new PluginContext();
            Map<String, Object> storageInput = new HashMap<>();
            storageInput.put("action", "save");
            storageInput.put("inputStream", encryptedStream);
            storageInput.put("objectKey", objectKey);
            storageInput.put("contentType", "application/octet-stream");
            Map<String, String> metadata = new HashMap<>();
            metadata.put("encrypted", "true");
            metadata.put("keyId", keyId);
            metadata.put("ivBase64", encryptedData.getIvBase64());
            metadata.put("originalHash", fileHash);
            metadata.put("businessType", businessType != null ? businessType : "default");
            metadata.put("businessId", businessId);
            storageInput.put("metadata", metadata);
            storageContext.getInput().putAll(storageInput);

            Plugin storagePlugin = pluginManager.getPlugin(storagePluginId);
            if (storagePlugin == null) {
                return PluginResult.error("存储插件不存在：" + storagePluginId);
            }

            PluginResult storageResult = storagePlugin.execute(storageContext);

            if (!storageResult.isSuccess()) {
                return storageResult;
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileId", storageResult.getData("objectKey", String.class));
            responseData.put("encrypted", true);
            responseData.put("keyId", keyId);
            responseData.put("algorithm", "AES-256-GCM");
            responseData.put("originalHash", fileHash);

            log.info("文件加密保存成功：{}", objectKey);

            return PluginResult.success(responseData);

        } catch (Exception e) {
            log.error("加密保存文件失败", e);
            return PluginResult.error("加密保存失败：" + e.getMessage());
        }
    }

    private PluginResult loadDecrypted(PluginContext context) {
        try {
            String fileId = context.getInputString("fileId");

            if (fileId == null) {
                return PluginResult.error("缺少 fileId 参数");
            }

            PluginContext storageContext = new PluginContext();
            Map<String, Object> storageInput = new HashMap<>();
            storageInput.put("action", "load");
            storageInput.put("objectKey", fileId);
            storageContext.getInput().putAll(storageInput);

            Plugin storagePlugin = pluginManager.getPlugin(storagePluginId);
            if (storagePlugin == null) {
                return PluginResult.error("存储插件不存在：" + storagePluginId);
            }

            PluginResult storageResult = storagePlugin.execute(storageContext);

            if (!storageResult.isSuccess()) {
                return storageResult;
            }

            InputStream encryptedStream = storageResult.getData("inputStream", InputStream.class);
            String keyId = storageResult.getData("metadata", Map.class).get("keyId").toString();
            String ivBase64 = storageResult.getData("metadata", Map.class).get("ivBase64").toString();
            String originalHash = storageResult.getData("metadata", Map.class).get("originalHash").toString();

            SecretKey key = encryptionKeyStore.getKey(keyId);
            if (key == null) {
                return PluginResult.error("解密密钥不存在：" + keyId);
            }

            byte[] encryptedBytes = encryptedStream.readAllBytes();
            EncryptedData encryptedData = new EncryptedData(encryptedBytes, java.util.Base64.getDecoder().decode(ivBase64));

            byte[] decryptedBytes = EncryptionUtils.decrypt(encryptedData, key);

            String decryptedHash = Hashing.sha256().hashBytes(decryptedBytes).toString();
            if (!decryptedHash.equals(originalHash)) {
                return PluginResult.error("数据完整性校验失败");
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("inputStream", new ByteArrayInputStream(decryptedBytes));
            responseData.put("originalHash", originalHash);
            responseData.put("verified", true);

            log.info("文件解密加载成功：{}", fileId);

            return PluginResult.success(responseData);

        } catch (Exception e) {
            log.error("解密加载文件失败", e);
            return PluginResult.error("解密加载失败：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        log.info("EncryptionArchivePlugin 销毁");
    }

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata() {
            @Override
            public String getPluginId() {
                return PLUGIN_ID;
            }

            @Override
            public String getVersion() {
                return PLUGIN_VERSION;
            }

            @Override
            public String getName() {
                return PLUGIN_NAME;
            }

            @Override
            public String getDescription() {
                return PLUGIN_DESCRIPTION;
            }
        };
    }
}

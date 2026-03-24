package com.aidrclaw.core.storage;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StorageOrchestrator {

    @Autowired
    private List<StorageStrategy> storageStrategies;

    @Autowired
    private Plugin localStoragePlugin;

    @Autowired
    private Plugin minioStoragePlugin;

    @Autowired
    private Plugin encryptionArchivePlugin;

    public PluginResult saveFile(InputStream inputStream, String businessType, String businessId, boolean encrypt) {
        try {
            StorageStrategy strategy = selectStrategy(businessType, 0L);
            String backend = strategy != null ? strategy.getStorageBackend() : "local";

            Plugin storagePlugin = getStoragePlugin(backend);
            if (storagePlugin == null) {
                return PluginResult.error("存储插件不可用：" + backend);
            }

            if (encrypt) {
                return saveEncrypted(inputStream, businessType, businessId);
            } else {
                return saveToBackend(storagePlugin, inputStream, businessType, businessId, backend);
            }

        } catch (Exception e) {
            log.error("保存文件失败", e);
            return PluginResult.error("保存文件失败：" + e.getMessage());
        }
    }

    private PluginResult saveEncrypted(InputStream inputStream, String businessType, String businessId) {
        PluginContext context = new PluginContext();
        Map<String, Object> input = new HashMap<>();
        input.put("action", "saveEncrypted");
        input.put("inputStream", inputStream);
        input.put("businessType", businessType);
        input.put("businessId", businessId);
        context.getInput().putAll(input);

        log.info("使用加密归档插件保存文件");
        return encryptionArchivePlugin.execute(context);
    }

    private PluginResult saveToBackend(Plugin plugin, InputStream inputStream, String businessType, String businessId, String backend) {
        PluginContext context = new PluginContext();
        Map<String, Object> input = new HashMap<>();
        input.put("action", "save");
        input.put("inputStream", inputStream);
        input.put("businessType", businessType);
        input.put("businessId", businessId);
        context.getInput().putAll(input);

        log.info("使用 {} 存储后端保存文件", backend);
        return plugin.execute(context);
    }

    private StorageStrategy selectStrategy(String businessType, Long fileSize) {
        for (StorageStrategy strategy : storageStrategies) {
            if (strategy.shouldUse(businessType, fileSize)) {
                return strategy;
            }
        }
        return null;
    }

    private Plugin getStoragePlugin(String backend) {
        return switch (backend) {
            case "minio" -> minioStoragePlugin;
            case "local" -> localStoragePlugin;
            default -> localStoragePlugin;
        };
    }
}

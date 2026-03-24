package com.aidrclaw.plugin.storage;

import com.aidrclaw.core.entity.StorageFile;
import com.aidrclaw.core.mapper.StorageFileMapper;
import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class LocalStoragePlugin implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(LocalStoragePlugin.class);

    @Autowired
    private StorageFileMapper storageFileMapper;

    private String basePath;

    @Override
    public void init(PluginContext context) {
        basePath = context.getConfigString("base-path");
        if (basePath == null || basePath.isEmpty()) {
            basePath = "./storage";
        }
        
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        
        logger.info("LocalStoragePlugin 初始化完成，存储路径：{}", basePath);
    }

    @Override
    public PluginResult execute(PluginContext context) {
        String action = context.getInputString("action");
        
        if (action == null) {
            return PluginResult.error("缺少 action 参数");
        }
        
        return switch (action) {
            case "save" -> saveFile(context);
            case "load" -> loadFile(context);
            case "delete" -> deleteFile(context);
            default -> PluginResult.error("未知操作：" + action);
        };
    }

    private PluginResult saveFile(PluginContext context) {
        try {
            InputStream data = context.getInput("data", InputStream.class);
            String businessType = context.getInputString("businessType");
            String businessId = context.getInputString("businessId");
            String mimeType = context.getInputString("mimeType");
            
            if (data == null) {
                return PluginResult.error("缺少文件数据");
            }
            
            byte[] fileBytes = data.readAllBytes();
            String fileHash = com.google.common.hash.Hashing.sha256()
                    .hashBytes(fileBytes)
                    .toString();
            
            String subDir = businessType != null ? businessType : "default";
            Path storagePath = Paths.get(basePath, subDir, fileHash);
            File storageFile = storagePath.toFile();
            
            storageFile.getParentFile().mkdirs();
            Files.write(fileBytes, storageFile);
            
            StorageFile entity = new StorageFile();
            entity.setFilePath(storagePath.toString());
            entity.setFileSize((long) fileBytes.length);
            entity.setFileHash(fileHash);
            entity.setMimeType(mimeType);
            entity.setBusinessType(businessType);
            entity.setBusinessId(businessId);
            entity.setCreatedAt(LocalDateTime.now());
            
            storageFileMapper.insert(entity);
            
            logger.info("文件保存成功：{}, 大小：{} bytes", fileHash, fileBytes.length);
            
            return PluginResult.success(Map.of(
                "fileId", entity.getId(),
                "filePath", entity.getFilePath(),
                "fileHash", fileHash,
                "fileSize", entity.getFileSize()
            ));
            
        } catch (IOException e) {
            logger.error("保存文件失败", e);
            return PluginResult.error("保存文件失败：" + e.getMessage());
        }
    }

    private PluginResult loadFile(PluginContext context) {
        try {
            Long fileId = context.getInput("fileId", Long.class);
            String fileHash = context.getInputString("fileHash");
            
            StorageFile entity;
            if (fileId != null) {
                entity = storageFileMapper.selectById(fileId);
            } else if (fileHash != null) {
                entity = storageFileMapper.selectById(fileId);
            } else {
                return PluginResult.error("需要 fileId 或 fileHash 参数");
            }
            
            if (entity == null) {
                return PluginResult.error(404, "文件不存在");
            }
            
            File file = new File(entity.getFilePath());
            if (!file.exists()) {
                return PluginResult.error(404, "物理文件不存在");
            }
            
            byte[] fileBytes = Files.toByteArray(file);
            
            return PluginResult.success(Map.of(
                "data", fileBytes,
                "mimeType", entity.getMimeType(),
                "fileName", entity.getFilePath()
            ));
            
        } catch (IOException e) {
            logger.error("读取文件失败", e);
            return PluginResult.error("读取文件失败：" + e.getMessage());
        }
    }

    private PluginResult deleteFile(PluginContext context) {
        try {
            Long fileId = context.getInput("fileId", Long.class);
            
            if (fileId == null) {
                return PluginResult.error("缺少 fileId 参数");
            }
            
            StorageFile entity = storageFileMapper.selectById(fileId);
            if (entity == null) {
                return PluginResult.error(404, "文件不存在");
            }
            
            File file = new File(entity.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            
            storageFileMapper.delete(fileId);
            
            logger.info("文件删除成功：{}", fileId);
            return PluginResult.success();
            
        } catch (Exception e) {
            logger.error("删除文件失败", e);
            return PluginResult.error("删除文件失败：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        logger.info("LocalStoragePlugin 销毁");
    }

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata() {
            @Override
            public String getPluginId() {
                return "local-storage";
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String getName() {
                return "本地存储插件";
            }

            @Override
            public String getDescription() {
                return "基于本地文件系统的存储插件，支持 SHA-256 哈希和元数据管理";
            }
        };
    }
}

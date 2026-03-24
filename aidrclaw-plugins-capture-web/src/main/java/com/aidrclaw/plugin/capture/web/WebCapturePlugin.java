package com.aidrclaw.plugin.capture.web;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class WebCapturePlugin implements Plugin {

    private static final String PLUGIN_ID = "web-capture";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_NAME = "Web 音视频采集插件";
    private static final String PLUGIN_DESCRIPTION = "基于 WebRTC 的浏览器端音视频采集插件，支持零安装录制";

    private PluginContext context;
    private String uploadDirectory;

    @Override
    public void init(PluginContext context) {
        this.context = context;
        this.uploadDirectory = context.getConfigString("upload.directory");
        if (this.uploadDirectory == null) {
            this.uploadDirectory = "uploads/web-capture";
        }
        log.info("WebCapturePlugin 初始化完成，上传目录：{}", uploadDirectory);
    }

    @Override
    public PluginResult execute(PluginContext context) {
        log.info("WebCapturePlugin 执行开始");
        
        try {
            MultipartFile file = context.getInput("file", MultipartFile.class);
            if (file == null || file.isEmpty()) {
                return PluginResult.error("未找到上传文件");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".webm";
            String filename = UUID.randomUUID().toString() + extension;
            
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
            
            log.info("文件保存成功：{}", filePath);

            Map<String, Object> data = new HashMap<>();
            data.put("fileId", filename);
            data.put("filePath", filePath.toString());
            data.put("originalFilename", originalFilename);
            data.put("size", file.getSize());
            data.put("contentType", file.getContentType());
            data.put("uploadTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return PluginResult.success(data);

        } catch (IOException e) {
            log.error("文件保存失败", e);
            return PluginResult.error("文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("插件执行异常", e);
            return PluginResult.error("插件执行异常：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        log.info("WebCapturePlugin 销毁");
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

    public String getUploadDirectory() {
        return uploadDirectory;
    }
}

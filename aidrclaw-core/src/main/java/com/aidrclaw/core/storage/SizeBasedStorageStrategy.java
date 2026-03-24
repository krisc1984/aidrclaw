package com.aidrclaw.core.storage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class SizeBasedStorageStrategy implements StorageStrategy {

    private final String storageBackend;
    private final Long sizeThreshold;
    private final String defaultBucket;
    private final String largeFileBucket;

    public SizeBasedStorageStrategy() {
        this.storageBackend = "hybrid";
        this.sizeThreshold = 104857600L;
        this.defaultBucket = "dual-recording";
        this.largeFileBucket = "dual-recording-large";
    }

    @Override
    public String getStorageBackend() {
        return storageBackend;
    }

    @Override
    public boolean shouldUse(String businessType, Long fileSize) {
        if ("temporary".equals(businessType)) {
            return false;
        }
        return true;
    }

    @Override
    public String getTargetBucket(String businessType) {
        return defaultBucket;
    }

    public String getTargetBucket(String businessType, Long fileSize) {
        if (fileSize != null && fileSize > sizeThreshold) {
            log.info("文件大于 {} bytes，使用大文件 bucket: {}", sizeThreshold, largeFileBucket);
            return largeFileBucket;
        }
        return defaultBucket;
    }
}

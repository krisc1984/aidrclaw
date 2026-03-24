package com.aidrclaw.core.storage;

public interface StorageStrategy {

    String getStorageBackend();

    boolean shouldUse(String businessType, Long fileSize);

    String getTargetBucket(String businessType);
}

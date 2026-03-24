package com.aidrclaw.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucket = "dual-recording";

    private String region = "us-east-1";

    public boolean isValid() {
        return endpoint != null && !endpoint.isEmpty() 
            && accessKey != null && !accessKey.isEmpty()
            && secretKey != null && !secretKey.isEmpty();
    }
}

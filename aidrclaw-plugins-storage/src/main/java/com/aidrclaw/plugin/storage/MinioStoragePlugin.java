package com.aidrclaw.plugin.storage;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioStoragePlugin implements Plugin {

    private static final String PLUGIN_ID = "minio-storage";
    private static final String PLUGIN_VERSION = "1.0.0";
    private static final String PLUGIN_NAME = "MinIO 对象存储插件";
    private static final String PLUGIN_DESCRIPTION = "支持 S3 兼容协议的对象存储插件";

    private MinioClient minioClient;
    private String bucket;

    @Override
    public void init(PluginContext context) {
        String endpoint = context.getConfigString("endpoint");
        String accessKey = context.getConfigString("access-key");
        String secretKey = context.getConfigString("secret-key");
        this.bucket = context.getConfigString("bucket");
        String region = context.getConfigString("region");

        if (endpoint == null || accessKey == null || secretKey == null) {
            log.error("MinIO 配置缺失：endpoint, access-key, secret-key 必须配置");
            throw new IllegalStateException("MinIO 配置缺失");
        }

        if (this.bucket == null) {
            this.bucket = "dual-recording";
        }
        if (region == null) {
            region = "us-east-1";
        }

        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucket)
                        .region(region)
                        .build());
                log.info("MinIO bucket 创建成功：{}", bucket);
            }

            log.info("MinioStoragePlugin 初始化完成，endpoint: {}, bucket: {}", endpoint, bucket);
        } catch (Exception e) {
            log.error("MinIO 初始化失败", e);
            throw new IllegalStateException("MinIO 初始化失败：" + e.getMessage(), e);
        }
    }

    @Override
    public PluginResult execute(PluginContext context) {
        String action = context.getInputString("action");

        if (action == null) {
            return PluginResult.error("缺少 action 参数");
        }

        return switch (action) {
            case "save" -> saveObject(context);
            case "load" -> loadObject(context);
            case "delete" -> deleteObject(context);
            case "presignedUrl" -> generatePresignedUrl(context);
            default -> PluginResult.error("未知操作：" + action);
        };
    }

    private PluginResult saveObject(PluginContext context) {
        try {
            InputStream inputStream = context.getInput("inputStream", InputStream.class);
            String objectKey = context.getInputString("objectKey");
            String contentType = context.getInputString("contentType");
            Map<String, String> metadata = context.getInput("metadata", Map.class);

            if (inputStream == null || objectKey == null) {
                return PluginResult.error("缺少必要参数：inputStream, objectKey");
            }

            long size = inputStream.available();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .userMetadata(metadata)
                    .build());

            log.info("文件上传成功到 MinIO: {}/{}", bucket, objectKey);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("objectKey", objectKey);
            responseData.put("bucket", bucket);
            responseData.put("size", size);

            return PluginResult.success(responseData);

        } catch (Exception e) {
            log.error("保存文件到 MinIO 失败", e);
            return PluginResult.error("保存文件失败：" + e.getMessage());
        }
    }

    private PluginResult loadObject(PluginContext context) {
        try {
            String objectKey = context.getInputString("objectKey");

            if (objectKey == null) {
                return PluginResult.error("缺少 objectKey 参数");
            }

            InputStream inputStream = minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );

            log.info("文件下载成功从 MinIO: {}/{}", bucket, objectKey);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("inputStream", inputStream);
            responseData.put("contentType", stat.contentType());
            responseData.put("size", stat.size());
            responseData.put("metadata", stat.userMetadata());

            return PluginResult.success(responseData);

        } catch (Exception e) {
            log.error("从 MinIO 加载文件失败", e);
            return PluginResult.error("加载文件失败：" + e.getMessage());
        }
    }

    private PluginResult deleteObject(PluginContext context) {
        try {
            String objectKey = context.getInputString("objectKey");

            if (objectKey == null) {
                return PluginResult.error("缺少 objectKey 参数");
            }

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());

            log.info("文件删除成功从 MinIO: {}/{}", bucket, objectKey);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("deleted", true);
            responseData.put("objectKey", objectKey);

            return PluginResult.success(responseData);

        } catch (Exception e) {
            log.error("从 MinIO 删除文件失败", e);
            return PluginResult.error("删除文件失败：" + e.getMessage());
        }
    }

    private PluginResult generatePresignedUrl(PluginContext context) {
        try {
            String objectKey = context.getInputString("objectKey");
            Integer expirySeconds = context.getInput("expirySeconds", Integer.class);

            if (objectKey == null) {
                return PluginResult.error("缺少 objectKey 参数");
            }

            if (expirySeconds == null) {
                expirySeconds = 3600;
            }

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );

            log.info("生成 MinIO 预签名 URL: {}", url);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", url);
            responseData.put("expiry", expirySeconds);
            responseData.put("objectKey", objectKey);

            return PluginResult.success(responseData);

        } catch (Exception e) {
            log.error("生成 MinIO 预签名 URL 失败", e);
            return PluginResult.error("生成预签名 URL 失败：" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        log.info("MinioStoragePlugin 销毁");
        minioClient = null;
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

    public MinioClient getMinioClient() {
        return minioClient;
    }

    public String getBucket() {
        return bucket;
    }
}

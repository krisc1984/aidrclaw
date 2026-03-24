package com.aidrclaw.plugin.storage;

import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MinioStoragePluginTest {

    private MinioStoragePlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new MinioStoragePlugin();
    }

    @Test
    void testPluginMetadata() {
        PluginMetadata metadata = plugin.getMetadata();
        
        assertEquals("minio-storage", metadata.getPluginId());
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("MinIO 对象存储插件", metadata.getName());
        assertNotNull(metadata.getDescription());
    }

    @Test
    void testInitWithMissingConfig() {
        PluginContext context = new PluginContext();
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            plugin.init(context);
        });
        
        assertTrue(exception.getMessage().contains("MinIO 配置缺失"));
    }

    @Test
    void testInitWithValidConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        config.put("region", "us-east-1");
        
        PluginContext context = new PluginContext(config, new HashMap<>());
        
        assertDoesNotThrow(() -> {
            plugin.init(context);
        });
        
        assertEquals("test-bucket", plugin.getBucket());
        assertNotNull(plugin.getMinioClient());
    }

    @Test
    void testExecuteWithMissingAction() {
        PluginContext context = new PluginContext();
        
        PluginResult result = plugin.execute(context);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("缺少 action 参数"));
    }

    @Test
    void testExecuteWithUnknownAction() {
        Map<String, Object> input = new HashMap<>();
        input.put("action", "unknown");
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("未知操作"));
    }

    @Test
    void testSaveWithMissingParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        
        PluginContext initContext = new PluginContext(config, new HashMap<>());
        plugin.init(initContext);
        
        Map<String, Object> input = new HashMap<>();
        input.put("action", "save");
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("缺少必要参数"));
    }

    @Test
    void testSaveWithValidParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        
        PluginContext initContext = new PluginContext(config, new HashMap<>());
        plugin.init(initContext);
        
        String testData = "test file content";
        InputStream inputStream = new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8));
        
        Map<String, Object> input = new HashMap<>();
        input.put("action", "save");
        input.put("inputStream", inputStream);
        input.put("objectKey", "test/test-file.txt");
        input.put("contentType", "text/plain");
        
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertNotNull(result);
    }

    @Test
    void testLoadWithMissingParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        
        PluginContext initContext = new PluginContext(config, new HashMap<>());
        plugin.init(initContext);
        
        Map<String, Object> input = new HashMap<>();
        input.put("action", "load");
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("缺少 objectKey 参数"));
    }

    @Test
    void testDeleteWithMissingParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        
        PluginContext initContext = new PluginContext(config, new HashMap<>());
        plugin.init(initContext);
        
        Map<String, Object> input = new HashMap<>();
        input.put("action", "delete");
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("缺少 objectKey 参数"));
    }

    @Test
    void testPresignedUrlWithMissingParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        
        PluginContext initContext = new PluginContext(config, new HashMap<>());
        plugin.init(initContext);
        
        Map<String, Object> input = new HashMap<>();
        input.put("action", "presignedUrl");
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("缺少 objectKey 参数"));
    }

    @Test
    void testDestroy() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "http://localhost:9000");
        config.put("access-key", "minioadmin");
        config.put("secret-key", "minioadmin");
        config.put("bucket", "test-bucket");
        
        PluginContext initContext = new PluginContext(config, new HashMap<>());
        plugin.init(initContext);
        
        assertDoesNotThrow(() -> {
            plugin.destroy();
        });
    }
}

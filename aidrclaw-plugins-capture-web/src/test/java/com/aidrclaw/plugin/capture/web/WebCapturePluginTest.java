package com.aidrclaw.plugin.capture.web;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

class WebCapturePluginTest {

    private WebCapturePlugin plugin;
    private Path testUploadDir;

    @BeforeEach
    void setUp() throws IOException {
        plugin = new WebCapturePlugin();
        testUploadDir = Files.createTempDirectory("web-capture-test");
        
        Map<String, Object> config = new HashMap<>();
        config.put("upload.directory", testUploadDir.toString());
        
        PluginContext context = new PluginContext(config, new HashMap<>());
        
        plugin.init(context);
    }

    @Test
    void testPluginImplementsPluginInterface() {
        assertTrue(plugin instanceof Plugin);
    }

    @Test
    void testGetMetadata() {
        PluginMetadata metadata = plugin.getMetadata();
        
        assertEquals("web-capture", metadata.getPluginId());
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Web 音视频采集插件", metadata.getName());
        assertNotNull(metadata.getDescription());
    }

    @Test
    void testExecuteWithValidFile() throws IOException {
        Map<String, Object> input = new HashMap<>();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-recording.webm",
            "video/webm",
            "test video content".getBytes()
        );
        input.put("file", file);
        
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertEquals(200, result.getStatusCode());
        assertTrue(result.isSuccess());
        
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("fileId"));
        assertNotNull(data.get("filePath"));
        assertEquals("test-recording.webm", data.get("originalFilename"));
        
        Path savedFile = Paths.get((String) data.get("filePath"));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void testExecuteWithNullFile() {
        PluginContext context = new PluginContext();
        
        PluginResult result = plugin.execute(context);
        
        assertEquals(500, result.getStatusCode());
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("未找到上传文件"));
    }

    @Test
    void testExecuteWithEmptyFile() {
        Map<String, Object> input = new HashMap<>();
        input.put("file", new MockMultipartFile("file", new byte[0]));
        
        PluginContext context = new PluginContext(new HashMap<>(), input);
        
        PluginResult result = plugin.execute(context);
        
        assertEquals(500, result.getStatusCode());
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("未找到上传文件"));
    }

    @Test
    void testPluginCanBeDiscoveredBySPI() {
        ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);
        boolean found = false;
        
        for (Plugin plugin : loader) {
            if (plugin instanceof WebCapturePlugin) {
                found = true;
                break;
            }
        }
        
        assertTrue(found, "WebCapturePlugin should be discovered by SPI");
    }
}

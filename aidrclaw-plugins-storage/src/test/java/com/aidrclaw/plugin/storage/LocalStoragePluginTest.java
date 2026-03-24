package com.aidrclaw.plugin.storage;

import com.aidrclaw.core.mapper.StorageFileMapper;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "storage.local.base-path=./test-storage"
})
class LocalStoragePluginTest {

    @Autowired
    private LocalStoragePlugin storagePlugin;

    @Autowired
    private StorageFileMapper storageFileMapper;

    private PluginContext context;

    @BeforeEach
    void setUp() {
        context = new PluginContext(
            Map.of("base-path", "./test-storage"),
            Map.of()
        );
        storagePlugin.init(context);
    }

    @Test
    void saveFile_shouldSaveSuccessfully() {
        String testContent = "Test file content for storage plugin";
        byte[] testData = testContent.getBytes(StandardCharsets.UTF_8);
        
        PluginContext saveContext = new PluginContext(
            Map.of("base-path", "./test-storage"),
            Map.of(
                "action", "save",
                "data", new ByteArrayInputStream(testData),
                "businessType", "test",
                "businessId", "test-1",
                "mimeType", "text/plain"
            )
        );
        
        PluginResult result = storagePlugin.execute(saveContext);
        
        assertTrue(result.isSuccess(), "文件保存应该成功");
        assertNotNull(result.getData("fileId", Long.class));
        assertNotNull(result.getData("fileHash", String.class));
    }

    @Test
    void getMetadata_shouldReturnCorrectInfo() {
        PluginMetadata metadata = storagePlugin.getMetadata();
        
        assertEquals("local-storage", metadata.getPluginId());
        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("本地存储插件", metadata.getName());
    }
}

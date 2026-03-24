package com.aidrclaw.core.plugin;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PluginLoaderTest {

    @Autowired
    private PluginLoader pluginLoader;

    @Test
    void loadAllPlugins_shouldLoadMockPlugin() {
        pluginLoader.loadAllPlugins();
        
        assertTrue(pluginLoader.getLoadedPluginCount() > 0, "应该至少加载一个插件");
        assertTrue(pluginLoader.isPluginLoaded("mock-plugin"), "应该加载 Mock 插件");
    }

    @Test
    void getPlugin_shouldReturnPlugin() {
        pluginLoader.loadAllPlugins();
        
        Plugin plugin = pluginLoader.getPlugin("mock-plugin");
        assertNotNull(plugin);
        assertEquals("mock-plugin", plugin.getMetadata().getPluginId());
    }

    @Test
    void listPluginIds_shouldContainMockPlugin() {
        pluginLoader.loadAllPlugins();
        
        List<String> pluginIds = pluginLoader.listPluginIds();
        assertTrue(pluginIds.contains("mock-plugin"));
    }
}

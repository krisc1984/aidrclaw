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
    void loadAllPlugins_shouldLoadLocalStoragePlugin() {
        pluginLoader.loadAllPlugins();
        
        assertTrue(pluginLoader.getLoadedPluginCount() > 0, "应该至少加载一个插件");
        assertTrue(pluginLoader.isPluginLoaded("local-storage"), "应该加载本地存储插件");
    }

    @Test
    void getPlugin_shouldReturnPlugin() {
        pluginLoader.loadAllPlugins();
        
        Plugin plugin = pluginLoader.getPlugin("local-storage");
        assertNotNull(plugin);
        assertEquals("local-storage", plugin.getMetadata().getPluginId());
    }

    @Test
    void listPluginIds_shouldContainLocalStorage() {
        pluginLoader.loadAllPlugins();
        
        List<String> pluginIds = pluginLoader.listPluginIds();
        assertTrue(pluginIds.contains("local-storage"));
    }
}

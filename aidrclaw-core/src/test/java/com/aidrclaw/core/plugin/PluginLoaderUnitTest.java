package com.aidrclaw.core.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginLoaderUnitTest {

    @Test
    void pluginLoader_shouldInstantiate() {
        PluginLoader pluginLoader = new PluginLoader();
        assertNotNull(pluginLoader);
    }

    @Test
    void getLoadedPluginCount_shouldBeZeroInitially() {
        PluginLoader pluginLoader = new PluginLoader();
        assertEquals(0, pluginLoader.getLoadedPluginCount());
    }
}

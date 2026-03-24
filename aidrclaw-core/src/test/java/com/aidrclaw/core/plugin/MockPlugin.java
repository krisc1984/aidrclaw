package com.aidrclaw.core.plugin;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;

/**
 * Mock 插件 - 仅用于测试
 */
public class MockPlugin implements Plugin {

    private static final String PLUGIN_ID = "mock-plugin";
    private boolean initialized = false;
    private boolean destroyed = false;

    @Override
    public void init(PluginContext context) {
        initialized = true;
    }

    @Override
    public PluginResult execute(PluginContext context) {
        return PluginResult.success();
    }

    @Override
    public void destroy() {
        destroyed = true;
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
                return "1.0.0";
            }

            @Override
            public String getName() {
                return "Mock 插件";
            }

            @Override
            public String getDescription() {
                return "用于测试的 Mock 插件";
            }
        };
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}

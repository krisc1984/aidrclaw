package com.aidrclaw.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件上下文 - 传递配置和输入参数给插件
 */
public class PluginContext {

    private final Map<String, Object> config;
    private final Map<String, Object> input;

    public PluginContext() {
        this.config = new HashMap<>();
        this.input = new HashMap<>();
    }

    public PluginContext(Map<String, Object> config, Map<String, Object> input) {
        this.config = config != null ? new HashMap<>(config) : new HashMap<>();
        this.input = input != null ? new HashMap<>(input) : new HashMap<>();
    }

    public <T> T getConfig(String key, Class<T> type) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException(
            "Config value for key '" + key + "' is not of type " + type.getName()
        );
    }

    public String getConfigString(String key) {
        return getConfig(key, String.class);
    }

    public Integer getConfigInteger(String key) {
        return getConfig(key, Integer.class);
    }

    public Boolean getConfigBoolean(String key) {
        return getConfig(key, Boolean.class);
    }

    public <T> T getInput(String key, Class<T> type) {
        Object value = input.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException(
            "Input value for key '" + key + "' is not of type " + type.getName()
        );
    }

    public String getInputString(String key) {
        return getInput(key, String.class);
    }

    public Map<String, Object> getConfig() {
        return Collections.unmodifiableMap(config);
    }

    public Map<String, Object> getInput() {
        return Collections.unmodifiableMap(input);
    }
}

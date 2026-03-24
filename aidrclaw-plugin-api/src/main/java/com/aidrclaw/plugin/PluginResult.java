package com.aidrclaw.plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件执行结果
 */
public class PluginResult {

    private final boolean success;
    private final int statusCode;
    private final Map<String, Object> data;
    private final String errorMessage;

    private PluginResult(boolean success, int statusCode, Map<String, Object> data, String errorMessage) {
        this.success = success;
        this.statusCode = statusCode;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.errorMessage = errorMessage;
    }

    public static PluginResult success(Map<String, Object> data) {
        return new PluginResult(true, 200, data, null);
    }

    public static PluginResult success() {
        return new PluginResult(true, 200, new HashMap<>(), null);
    }

    public static PluginResult error(String errorMessage) {
        return new PluginResult(false, 500, new HashMap<>(), errorMessage);
    }

    public static PluginResult error(int statusCode, String errorMessage) {
        return new PluginResult(false, statusCode, new HashMap<>(), errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }

    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException(
            "Data value for key '" + key + "' is not of type " + type.getName()
        );
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

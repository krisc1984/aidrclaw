package com.aidrclaw.plugin;

/**
 * 插件元数据接口
 */
public interface PluginMetadata {

    String getPluginId();

    String getVersion();

    String getName();

    String getDescription();
}

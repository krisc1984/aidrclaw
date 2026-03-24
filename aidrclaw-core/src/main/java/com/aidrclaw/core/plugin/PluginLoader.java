package com.aidrclaw.core.plugin;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginMetadata;
import com.aidrclaw.plugin.PluginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 插件加载器 - 使用 ServiceLoader 发现和加载插件
 */
@Component
public class PluginLoader {

    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);

    private final Map<String, Plugin> loadedPlugins = new ConcurrentHashMap<>();
    private final Map<String, PluginMetadata> pluginMetadata = new ConcurrentHashMap<>();

    /**
     * 使用 ServiceLoader 发现并加载所有可用插件
     */
    public void loadAllPlugins() {
        logger.info("开始加载插件...");
        
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class);
        int loadedCount = 0;
        
        for (Plugin plugin : serviceLoader) {
            try {
                String pluginId = loadPlugin(plugin);
                if (pluginId != null) {
                    loadedCount++;
                }
            } catch (Exception e) {
                logger.error("加载插件失败：{}", plugin.getClass().getName(), e);
            }
        }
        
        logger.info("成功加载 {} 个插件", loadedCount);
    }

    /**
     * 加载单个插件
     * 
     * @param plugin 插件实例
     * @return 插件 ID，加载失败返回 null
     */
    public String loadPlugin(Plugin plugin) {
        try {
            PluginMetadata metadata = plugin.getMetadata();
            String pluginId = metadata.getPluginId();
            
            if (loadedPlugins.containsKey(pluginId)) {
                logger.warn("插件 {} 已存在，跳过加载", pluginId);
                return null;
            }
            
            PluginContext context = new PluginContext();
            plugin.init(context);
            
            loadedPlugins.put(pluginId, plugin);
            pluginMetadata.put(pluginId, metadata);
            
            logger.info("插件加载成功：{} v{}", metadata.getName(), metadata.getVersion());
            return pluginId;
            
        } catch (Exception e) {
            logger.error("初始化插件失败", e);
            return null;
        }
    }

    /**
     * 卸载插件
     * 
     * @param pluginId 插件 ID
     */
    public void unloadPlugin(String pluginId) {
        Plugin plugin = loadedPlugins.remove(pluginId);
        if (plugin != null) {
            try {
                plugin.destroy();
                pluginMetadata.remove(pluginId);
                logger.info("插件已卸载：{}", pluginId);
            } catch (Exception e) {
                logger.error("卸载插件失败：{}", pluginId, e);
            }
        }
    }

    /**
     * 获取已加载的插件
     * 
     * @param pluginId 插件 ID
     * @return 插件实例，不存在返回 null
     */
    public Plugin getPlugin(String pluginId) {
        return loadedPlugins.get(pluginId);
    }

    /**
     * 获取所有已加载的插件
     * 
     * @return 插件列表
     */
    public List<Plugin> getAllPlugins() {
        return new ArrayList<>(loadedPlugins.values());
    }

    /**
     * 获取所有插件元数据
     * 
     * @return 元数据列表
     */
    public List<PluginMetadata> getAllPluginMetadata() {
        return new ArrayList<>(pluginMetadata.values());
    }

    /**
     * 检查插件是否已加载
     * 
     * @param pluginId 插件 ID
     * @return true 如果已加载
     */
    public boolean isPluginLoaded(String pluginId) {
        return loadedPlugins.containsKey(pluginId);
    }

    /**
     * 获取已加载插件数量
     * 
     * @return 插件数量
     */
    public int getLoadedPluginCount() {
        return loadedPlugins.size();
    }

    /**
     * 获取插件元数据
     * 
     * @param pluginId 插件 ID
     * @return 元数据，不存在返回 null
     */
    public PluginMetadata getPluginMetadata(String pluginId) {
        return pluginMetadata.get(pluginId);
    }

    /**
     * 列出所有已加载插件的 ID
     * 
     * @return 插件 ID 列表
     */
    public List<String> listPluginIds() {
        return new ArrayList<>(loadedPlugins.keySet());
    }
}

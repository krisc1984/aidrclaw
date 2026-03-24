package com.aidrclaw.core.plugin;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 插件管理器 - 由 Spring 管理的 Service
 */
@Service
public class PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    @Autowired
    private PluginLoader pluginLoader;

    /**
     * 执行插件
     * 
     * @param pluginId 插件 ID
     * @param input 输入参数
     * @return 执行结果
     */
    public PluginResult executePlugin(String pluginId, Map<String, Object> input) {
        Plugin plugin = pluginLoader.getPlugin(pluginId);
        if (plugin == null) {
            return PluginResult.error(404, "插件不存在：" + pluginId);
        }

        try {
            PluginContext context = new PluginContext(Map.of(), input != null ? input : Map.of());
            PluginResult result = plugin.execute(context);
            
            if (result.isSuccess()) {
                logger.debug("插件执行成功：{}", pluginId);
            } else {
                logger.warn("插件执行失败：{} - {}", pluginId, result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("执行插件异常：{}", pluginId, e);
            return PluginResult.error("执行异常：" + e.getMessage());
        }
    }

    /**
     * 执行插件（无参数）
     * 
     * @param pluginId 插件 ID
     * @return 执行结果
     */
    public PluginResult executePlugin(String pluginId) {
        return executePlugin(pluginId, null);
    }

    /**
     * 获取插件
     * 
     * @param pluginId 插件 ID
     * @return 插件 Optional
     */
    public Optional<Plugin> getPlugin(String pluginId) {
        return Optional.ofNullable(pluginLoader.getPlugin(pluginId));
    }

    /**
     * 获取所有插件
     * 
     * @return 插件列表
     */
    public List<Plugin> getAllPlugins() {
        return pluginLoader.getAllPlugins();
    }

    /**
     * 检查插件是否可用
     * 
     * @param pluginId 插件 ID
     * @return true 如果已加载
     */
    public boolean isPluginAvailable(String pluginId) {
        return pluginLoader.isPluginLoaded(pluginId);
    }

    /**
     * 获取已加载插件数量
     * 
     * @return 插件数量
     */
    public int getPluginCount() {
        return pluginLoader.getLoadedPluginCount();
    }

    /**
     * 列出所有插件 ID
     * 
     * @return 插件 ID 列表
     */
    public List<String> listPluginIds() {
        return pluginLoader.listPluginIds();
    }
}

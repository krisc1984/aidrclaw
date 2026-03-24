package com.aidrclaw.plugin;

/**
 * 插件接口 - 所有插件必须实现此接口
 * 
 * <p>插件生命周期：
 * <ol>
 *     <li>{@link #init(PluginContext)} - 初始化插件</li>
 *     <li>{@link #execute(PluginContext)} - 执行插件功能</li>
 *     <li>{@link #destroy()} - 销毁插件资源</li>
 * </ol>
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public interface Plugin {

    /**
     * 初始化插件
     * 
     * @param context 插件上下文，包含配置和输入参数
     */
    void init(PluginContext context);

    /**
     * 执行插件功能
     * 
     * @param context 插件上下文
     * @return 执行结果
     */
    PluginResult execute(PluginContext context);

    /**
     * 销毁插件，释放资源
     */
    void destroy();

    /**
     * 获取插件元数据
     * 
     * @return 插件元数据
     */
    PluginMetadata getMetadata();
}

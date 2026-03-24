package com.aidrclaw.core.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 插件加载器 - 应用启动时自动加载所有插件
 */
@Component
public class PluginLoaderRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PluginLoaderRunner.class);

    @Autowired
    private PluginLoader pluginLoader;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("正在加载插件...");
        pluginLoader.loadAllPlugins();
        logger.info("插件加载完成，共加载 {} 个插件", pluginLoader.getLoadedPluginCount());
    }
}

package com.aidrclaw.core.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 流程异常处理器
 * 
 * 统一处理流程执行中的各类异常
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessExceptionHandler.class);
    
    /**
     * 处理异常
     * 
     * @param instance 流程实例
     * @param e 异常
     */
    public void handleException(ProcessInstance instance, Exception e) {
        logger.error("Exception in process instance {}: {}", 
                instance.getSessionId(), e.getMessage(), e);
        
        // 更新状态为异常
        instance.setState(ProcessInstance.ProcessState.EXCEPTION);
        
        // 记录异常到上下文（供后续分析）
        instance.getContext().setVariable("lastException", e.getClass().getName());
        instance.getContext().setVariable("lastExceptionMessage", e.getMessage());
        
        // 分类处理
        if (e instanceof PluginException) {
            handlePluginException(instance, (PluginException) e);
        } else if (e instanceof java.util.concurrent.TimeoutException) {
            handleTimeoutException(instance, (java.util.concurrent.TimeoutException) e);
        } else {
            handleUnknownException(instance, e);
        }
    }
    
    /**
     * 处理插件异常
     */
    private void handlePluginException(ProcessInstance instance, PluginException e) {
        logger.error("Plugin exception for session {}: plugin={}, error={}", 
                instance.getSessionId(), e.getPluginId(), e.getMessage());
    }
    
    /**
     * 处理超时异常
     */
    private void handleTimeoutException(ProcessInstance instance, java.util.concurrent.TimeoutException e) {
        logger.error("Timeout exception for session {}: {}", 
                instance.getSessionId(), e.getMessage());
    }
    
    /**
     * 处理未知异常
     */
    private void handleUnknownException(ProcessInstance instance, Exception e) {
        logger.error("Unknown exception for session {}: {}", 
                instance.getSessionId(), e.getMessage());
    }
    
    /**
     * 插件执行异常
     */
    public static class PluginException extends RuntimeException {
        private final String pluginId;
        
        public PluginException(String pluginId, String message) {
            super(message);
            this.pluginId = pluginId;
        }
        
        public String getPluginId() {
            return pluginId;
        }
    }
}

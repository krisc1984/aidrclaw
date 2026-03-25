package com.aidrclaw.core.process;

import com.aidrclaw.plugin.PluginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 重试管理器
 * 
 * 管理节点执行失败后的重试逻辑
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class RetryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryManager.class);
    
    private final ScheduledExecutorService scheduler;
    private final ProcessEngine processEngine;
    
    public RetryManager(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "retry-manager");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * 判断是否应该重试
     */
    public boolean shouldRetry(ProcessInstance instance, ProcessNode node) {
        int retryCount = instance.getRetryCount(node.getNodeId());
        int maxRetries = node.getMaxRetries();
        
        boolean shouldRetry = retryCount < maxRetries;
        logger.debug("Retry check for node {}: retryCount={}, maxRetries={}, shouldRetry={}", 
                node.getNodeId(), retryCount, maxRetries, shouldRetry);
        
        return shouldRetry;
    }
    
    /**
     * 调度延迟重试
     */
    public void scheduleRetry(ProcessInstance instance, ProcessNode node, int delayMs) {
        String nodeId = node.getNodeId();
        int retryCount = instance.getRetryCount(nodeId);
        
        logger.info("Scheduling retry for node {} (attempt {}/{}) in {} ms", 
                nodeId, retryCount + 1, node.getMaxRetries(), delayMs);
        
        scheduler.schedule(() -> {
            // 增加重试次数
            instance.incrementRetryCount(nodeId);
            
            // 重新执行节点
            logger.info("Retrying node {} (attempt {})", nodeId, instance.getRetryCount(nodeId));
            PluginResult result = processEngine.executeNode(instance);
            
            if (!result.isSuccess() && shouldRetry(instance, node)) {
                // 再次失败且仍可重试
                scheduleRetry(instance, node, node.getRetryDelay());
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 关闭调度器
     */
    public void shutdown() {
        logger.info("Shutting down retry manager");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

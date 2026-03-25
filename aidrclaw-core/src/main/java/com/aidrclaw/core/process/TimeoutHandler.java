package com.aidrclaw.core.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 超时处理器
 * 
 * 为节点执行调度超时定时器，超时后触发回调
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class TimeoutHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeoutHandler.class);
    
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTimeouts;
    
    public TimeoutHandler() {
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "timeout-handler");
            t.setDaemon(true);
            return t;
        });
        this.scheduledTimeouts = new ConcurrentHashMap<>();
    }
    
    /**
     * 调度超时定时器
     * 
     * @param sessionId 会话 ID
     * @param nodeId 节点 ID
     * @param timeoutSeconds 超时时间（秒）
     * @param onTimeout 超时回调
     * @return ScheduledFuture 用于取消定时器
     */
    public ScheduledFuture<?> scheduleTimeout(String sessionId, String nodeId, 
                                               int timeoutSeconds, Runnable onTimeout) {
        if (timeoutSeconds <= 0) {
            logger.debug("No timeout configured for node {}", nodeId);
            return null;
        }
        
        String key = sessionId + ":" + nodeId;
        
        logger.info("Scheduling timeout for session {} node {} ({} seconds)", 
                sessionId, nodeId, timeoutSeconds);
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            logger.warn("Timeout triggered for session {} node {}", sessionId, nodeId);
            scheduledTimeouts.remove(key);
            onTimeout.run();
        }, timeoutSeconds, TimeUnit.SECONDS);
        
        scheduledTimeouts.put(key, future);
        return future;
    }
    
    /**
     * 取消超时定时器
     * 
     * @param future ScheduledFuture
     */
    public void cancelTimeout(ScheduledFuture<?> future) {
        if (future != null && !future.isDone()) {
            future.cancel(false);
            logger.debug("Timeout cancelled");
        }
    }
    
    /**
     * 取消指定会话的超时定时器
     * 
     * @param sessionId 会话 ID
     */
    public void cancelTimeoutForSession(String sessionId) {
        scheduledTimeouts.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(sessionId + ":")) {
                entry.getValue().cancel(false);
                return true;
            }
            return false;
        });
        logger.debug("Cancelled all timeouts for session {}", sessionId);
    }
    
    /**
     * 关闭调度器
     */
    public void shutdown() {
        logger.info("Shutting down timeout handler");
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

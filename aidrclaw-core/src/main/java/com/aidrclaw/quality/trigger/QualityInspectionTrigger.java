package com.aidrclaw.quality.trigger;

import com.aidrclaw.quality.agent.QualityInspectionAgent;
import com.aidrclaw.quality.model.QualityReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 质检触发器
 * 
 * 在录制完成后自动触发质检流程
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class QualityInspectionTrigger {
    
    private static final Logger logger = LoggerFactory.getLogger(QualityInspectionTrigger.class);
    
    /**
     * 默认超时时间（3 分钟）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 180;
    
    @Autowired
    private QualityInspectionAgent qualityInspectionAgent;
    
    /**
     * 触发质检（异步执行）
     * 
     * @param sessionId 会话 ID
     */
    @Async
    public void triggerInspection(String sessionId) {
        logger.info("Triggering quality inspection for session: {}", sessionId);
        
        try {
            QualityReport report = qualityInspectionAgent.inspectWithTimeout(
                    sessionId, DEFAULT_TIMEOUT_SECONDS);
            
            if (report.getPassed()) {
                logger.info("Quality inspection PASSED for session {}: score={}", 
                        sessionId, report.getScore());
            } else {
                logger.warn("Quality inspection FAILED for session {}: score={}, reason={}", 
                        sessionId, report.getScore(), report.getReason());
            }
            
            // TODO: 保存质检报告到数据库
            // TODO: 发送通知给管理员
            
        } catch (Exception e) {
            logger.error("Quality inspection trigger failed for session {}: {}", sessionId, e.getMessage());
        }
    }
    
    /**
     * 触发质检（同步执行，用于测试）
     * 
     * @param sessionId 会话 ID
     * @return 质检报告
     */
    public QualityReport triggerInspectionSync(String sessionId) {
        logger.info("Triggering synchronous quality inspection for session: {}", sessionId);
        return qualityInspectionAgent.inspectWithTimeout(sessionId, DEFAULT_TIMEOUT_SECONDS);
    }
}

package com.aidrclaw.quality.agent;

import com.aidrclaw.agent.state.DialogState;
import com.aidrclaw.agent.dialog.DialogTreeManager;
import com.aidrclaw.quality.model.QualityReport;
import com.aidrclaw.quality.model.ScriptCompletionResult;
import com.aidrclaw.quality.model.ViolationRecord;
import com.aidrclaw.quality.rules.QualityScoringRule;
import com.aidrclaw.quality.rules.ScriptCompletenessRule;
import com.aidrclaw.quality.rules.ViolationKeywordsRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 质检 Agent
 * 
 * 执行完整的质检流程，包括话术完整性检测、违规词检测、评分计算
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class QualityInspectionAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(QualityInspectionAgent.class);
    
    @Autowired
    private DialogTreeManager dialogTreeManager;
    
    @Autowired
    private ScriptCompletenessRule scriptCompletenessRule;
    
    @Autowired
    private ViolationKeywordsRule violationKeywordsRule;
    
    @Autowired
    private QualityScoringRule qualityScoringRule;
    
    /**
     * 执行质检
     * 
     * @param sessionId 会话 ID
     * @return 质检报告
     */
    public QualityReport inspect(String sessionId) {
        logger.info("Starting quality inspection for session: {}", sessionId);
        
        // 获取对话状态
        DialogState state = dialogTreeManager.getCurrentState(sessionId);
        if (state == null) {
            logger.warn("No dialog state found for session: {}", sessionId);
            return createTimeoutReport(sessionId, "未找到对话状态");
        }
        
        try {
            // 1. 话术完整性检测
            ScriptCompletionResult scriptResult = scriptCompletenessRule.check(state);
            logger.info("Script completeness: {}% (passed: {})", 
                    scriptResult.getCompletionRate(), scriptResult.getPassed());
            
            // 2. 违规词检测
            List<ViolationRecord> violations = violationKeywordsRule.detectFromHistory(
                    state.getHistory() != null ? state.getHistory() : List.of());
            logger.info("Detected {} violations", violations.size());
            
            // 3. 计算评分
            QualityReport report = qualityScoringRule.calculate(scriptResult, violations);
            report.setSessionId(sessionId);
            
            logger.info("Quality inspection completed for session {}: score={}, passed={}", 
                    sessionId, report.getScore(), report.getPassed());
            
            return report;
            
        } catch (Exception e) {
            logger.error("Quality inspection failed for session {}: {}", sessionId, e.getMessage());
            return createTimeoutReport(sessionId, "质检异常：" + e.getMessage());
        }
    }
    
    /**
     * 创建超时报告
     */
    private QualityReport createTimeoutReport(String sessionId, String reason) {
        return QualityReport.builder()
                .sessionId(sessionId)
                .score(0)
                .passed(false)
                .reason(reason)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 异步执行质检（带超时）
     * 
     * @param sessionId 会话 ID
     * @param timeoutSeconds 超时时间（秒）
     * @return 质检报告
     */
    public QualityReport inspectWithTimeout(String sessionId, int timeoutSeconds) {
        try {
            return java.util.concurrent.CompletableFuture
                .supplyAsync(() -> inspect(sessionId))
                .orTimeout(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)
                .join();
        } catch (java.util.concurrent.CompletionException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                logger.warn("Quality inspection timeout for session {} after {} seconds", sessionId, timeoutSeconds);
                return createTimeoutReport(sessionId, "质检超时（" + timeoutSeconds + "秒）");
            }
            logger.error("Quality inspection failed for session {}: {}", sessionId, e.getMessage());
            return createTimeoutReport(sessionId, "质检异常：" + e.getMessage());
        } catch (Exception e) {
            logger.error("Quality inspection failed for session {}: {}", sessionId, e.getMessage());
            return createTimeoutReport(sessionId, "质检异常：" + e.getMessage());
        }
    }
}

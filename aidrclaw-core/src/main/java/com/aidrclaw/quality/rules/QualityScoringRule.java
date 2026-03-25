package com.aidrclaw.quality.rules;

import com.aidrclaw.quality.model.QualityReport;
import com.aidrclaw.quality.model.ScriptCompletionResult;
import com.aidrclaw.quality.model.ViolationRecord;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 质检评分计算规则
 * 
 * 根据话术完整性和违规词计算最终质检分数
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class QualityScoringRule {
    
    /**
     * 话术完整性通过阈值（80%）
     */
    private static final double SCRIPT_COMPLETENESS_THRESHOLD = 80.0;
    
    /**
     * 质检通过阈值（70 分）
     */
    private static final int PASS_SCORE_THRESHOLD = 70;
    
    /**
     * 话术完整性不达标扣分（20 分）
     */
    private static final int SCRIPT_INCOMPLETE_DEDUCTION = 20;
    
    /**
     * 计算质检分数
     * 
     * @param scriptResult 话术完成结果
     * @param violations 违规记录列表
     * @return 质检报告
     */
    public QualityReport calculate(ScriptCompletionResult scriptResult, List<ViolationRecord> violations) {
        QualityReport report = QualityReport.builder()
                .score(100)
                .passed(true)
                .violations(violations)
                .scriptCompletion(scriptResult)
                .timestamp(System.currentTimeMillis())
                .build();
        
        int score = 100;
        StringBuilder reasonBuilder = new StringBuilder();
        
        // 话术完整性扣分
        if (scriptResult != null && !scriptResult.getPassed()) {
            score -= SCRIPT_INCOMPLETE_DEDUCTION;
            reasonBuilder.append("话术完整性不足 (")
                    .append(String.format("%.1f", scriptResult.getCompletionRate()))
                    .append("%)<80%");
        }
        
        // 违规词扣分
        if (violations != null && !violations.isEmpty()) {
            int violationDeduction = violations.stream()
                    .mapToInt(ViolationRecord::getDeduction)
                    .sum();
            score -= violationDeduction;
            
            if (reasonBuilder.length() > 0) {
                reasonBuilder.append("; ");
            }
            reasonBuilder.append("检测到 ")
                    .append(violations.size())
                    .append(" 个违规词");
        }
        
        // 确保分数不低于 0
        score = Math.max(0, score);
        report.setScore(score);
        
        // 判断是否通过
        boolean passed = score >= PASS_SCORE_THRESHOLD;
        report.setPassed(passed);
        
        // 设置不通过原因
        if (!passed) {
            report.setFailed(reasonBuilder.length() > 0 
                ? reasonBuilder.append(" - 得分").append(score).append("<70").toString()
                : "质检得分低于 70 分");
        }
        
        return report;
    }
}

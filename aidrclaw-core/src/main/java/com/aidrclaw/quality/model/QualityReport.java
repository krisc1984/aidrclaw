package com.aidrclaw.quality.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 质检报告
 * 
 * 录制完成后生成的质检结果，包含评分、通过状态、违规记录等
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityReport {
    
    /**
     * 质检分数（0-100 分）
     */
    @Builder.Default
    private Integer score = 100;
    
    /**
     * 是否通过（>= 70 分）
     */
    @Builder.Default
    private Boolean passed = true;
    
    /**
     * 不通过原因
     */
    private String reason;
    
    /**
     * 违规记录列表
     */
    @Builder.Default
    private List<ViolationRecord> violations = new ArrayList<>();
    
    /**
     * 话术完成结果
     */
    private ScriptCompletionResult scriptCompletion;
    
    /**
     * 质检时间戳
     */
    private Long timestamp;
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 设置不通过
     */
    public void setFailed(String reason) {
        this.passed = false;
        this.reason = reason;
    }
    
    /**
     * 添加违规记录
     */
    public void addViolation(ViolationRecord violation) {
        if (this.violations == null) {
            this.violations = new ArrayList<>();
        }
        this.violations.add(violation);
    }
}

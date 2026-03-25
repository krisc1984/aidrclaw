package com.aidrclaw.quality.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 话术完成结果
 * 
 * 记录双录流程中话术环节的完成情况
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptCompletionResult {
    
    /**
     * 完成率（0-100%）
     */
    @Builder.Default
    private Double completionRate = 0.0;
    
    /**
     * 是否通过（>= 80%）
     */
    @Builder.Default
    private Boolean passed = true;
    
    /**
     * 必需节点总数
     */
    private Integer totalRequiredNodes;
    
    /**
     * 已完成的必需节点数
     */
    private Integer completedRequiredNodes;
    
    /**
     * 未完成的节点 ID 列表
     */
    private java.util.List<String> missingNodes;
    
    /**
     * 计算完成率
     */
    public static ScriptCompletionResult calculate(
            int totalRequired, 
            int completed, 
            java.util.List<String> missing) {
        
        double rate = totalRequired > 0 
            ? (double) completed / totalRequired * 100 
            : 0.0;
        
        boolean passed = rate >= 80.0;
        
        return ScriptCompletionResult.builder()
                .completionRate(rate)
                .passed(passed)
                .totalRequiredNodes(totalRequired)
                .completedRequiredNodes(completed)
                .missingNodes(missing)
                .build();
    }
}

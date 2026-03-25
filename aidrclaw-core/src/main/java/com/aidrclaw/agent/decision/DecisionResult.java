package com.aidrclaw.agent.decision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 决策结果
 * 
 * 表示流程决策 Agent 的决策结果
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResult {
    
    /**
     * 是否调整了流程
     */
    @Builder.Default
    private boolean adjusted = false;
    
    /**
     * 调整后的流程 ID
     */
    private String adjustedFlow;
    
    /**
     * 调整参数
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * 决策原因说明
     */
    private String reason;
    
    /**
     * 无调整
     */
    public static DecisionResult noAdjustment() {
        return DecisionResult.builder()
                .adjusted(false)
                .build();
    }
    
    /**
     * 调整流程
     */
    public static DecisionResult adjustFlow(String flowId, String reason) {
        return DecisionResult.builder()
                .adjusted(true)
                .adjustedFlow(flowId)
                .reason(reason)
                .build();
    }
    
    /**
     * 设置参数
     */
    public DecisionResult setParameter(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }
}

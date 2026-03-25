package com.aidrclaw.agent.decision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程调整
 * 
 * 表示流程调整的类型和参数
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowAdjustment {
    
    /**
     * 调整类型
     */
    private AdjustmentType type;
    
    /**
     * 目标节点 ID
     */
    private String nodeId;
    
    /**
     * 插入位置（BEFORE/AFTER）
     */
    private String position;
    
    /**
     * 新流程 ID
     */
    private String newFlowId;
    
    /**
     * 调整类型枚举
     */
    public enum AdjustmentType {
        /**
         * 插入节点
         */
        INSERT_NODE,
        /**
         * 跳过节点
         */
        SKIP_NODE,
        /**
         * 修改节点
         */
        MODIFY_NODE,
        /**
         * 切换流程
         */
        CHANGE_FLOW
    }
}

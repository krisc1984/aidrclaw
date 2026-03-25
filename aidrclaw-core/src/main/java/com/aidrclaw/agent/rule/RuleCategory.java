package com.aidrclaw.agent.rule;

/**
 * 规则分类枚举
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public enum RuleCategory {
    
    /**
     * 流程调整规则（增加/跳过节点）
     */
    FLOW_ADJUSTMENT,
    
    /**
     * 话术适配规则（语速、措辞调整）
     */
    SCRIPT_ADAPTATION,
    
    /**
     * 条件跳转规则（基于条件分支）
     */
    CONDITIONAL_JUMP,
    
    /**
     * 异常处理规则（转人工、终止流程）
     */
    EXCEPTION
}

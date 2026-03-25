package com.aidrclaw.agent.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务规则数据模型
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRule {
    
    /**
     * 规则 ID
     */
    private String id;
    
    /**
     * 规则名称
     */
    private String name;
    
    /**
     * 规则分类
     */
    private RuleCategory category;
    
    /**
     * SpEL 条件表达式
     */
    private String conditionExpression;
    
    /**
     * JSON 动作定义
     */
    private String actionJson;
    
    /**
     * 优先级（数字越大优先级越高）
     */
    @Builder.Default
    private Integer priority = 0;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;
}

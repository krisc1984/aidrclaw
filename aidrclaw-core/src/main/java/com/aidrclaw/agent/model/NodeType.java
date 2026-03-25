package com.aidrclaw.agent.model;

/**
 * 对话节点类型枚举
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public enum NodeType {
    
    /**
     * 消息节点 - 显示消息
     */
    MESSAGE,
    
    /**
     * 提问节点 - 提问并分支
     */
    QUESTION,
    
    /**
     * 动作节点 - 执行动作
     */
    ACTION,
    
    /**
     * 汇聚节点 - 汇聚点
     */
    MERGE,
    
    /**
     * 终端节点 - 对话结束
     */
    TERMINAL
}

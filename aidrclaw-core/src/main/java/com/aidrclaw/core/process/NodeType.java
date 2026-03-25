package com.aidrclaw.core.process;

/**
 * 节点类型枚举
 * 
 * 定义流程中可用的节点类型：
 * - AI_NODE: AI 能力节点（如人脸识别、语音识别）
 * - AGENT_NODE: Agent 对话节点（虚拟坐席对话）
 * - MANUAL_NODE: 人工操作节点（如签字确认）
 * - CONDITION_NODE: 条件判断节点（基于规则跳转）
 * - END_NODE: 流程结束节点
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public enum NodeType {
    
    /**
     * AI 能力节点
     * 调用 AI 插件执行特定能力（人脸比对、ASR、TTS 等）
     */
    AI_NODE("AI 能力节点"),
    
    /**
     * Agent 对话节点
     * 虚拟坐席与客户对话，执行预设话术脚本
     */
    AGENT_NODE("Agent 对话节点"),
    
    /**
     * 人工操作节点
     * 需要人工介入的操作（如签字确认、人工复核）
     */
    MANUAL_NODE("人工操作节点"),
    
    /**
     * 条件判断节点
     * 基于规则表达式进行条件判断，决定跳转路径
     */
    CONDITION_NODE("条件判断节点"),
    
    /**
     * 流程结束节点
     * 标记流程结束
     */
    END_NODE("流程结束节点");
    
    private final String description;
    
    NodeType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

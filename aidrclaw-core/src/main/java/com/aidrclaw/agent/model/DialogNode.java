package com.aidrclaw.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话节点
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogNode {
    
    /**
     * 节点唯一标识
     */
    private String id;
    
    /**
     * 节点类型
     */
    private NodeType type;
    
    /**
     * TTS 播报内容
     */
    private String content;
    
    /**
     * 提问配置（QUESTION 类型使用）
     */
    private QuestionConfig question;
    
    /**
     * 动作配置（ACTION 类型使用）
     */
    private ActionConfig action;
    
    /**
     * 意图到子节点 ID 的映射
     */
    @Builder.Default
    private Map<String, String> children = new HashMap<>();
    
    /**
     * 父节点 ID（用于返回）
     */
    private String parent;
    
    /**
     * 期望回答列表（用于意图识别）
     */
    @Builder.Default
    private Map<String, String> expectedAnswers = new HashMap<>();
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
}

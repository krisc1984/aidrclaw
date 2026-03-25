package com.aidrclaw.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 提问配置
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionConfig {
    
    /**
     * 期望的回答意图列表
     */
    private List<String> expectedIntents;
    
    /**
     * 意图映射：关键词→意图
     */
    private java.util.Map<String, String> intentMappings;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    /**
     * 超时后的意图
     */
    private String timeoutIntent;
    
    /**
     * 默认意图（无匹配时使用）
     */
    private String defaultIntent;
}

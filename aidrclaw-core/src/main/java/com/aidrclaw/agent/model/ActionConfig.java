package com.aidrclaw.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 动作配置
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfig {
    
    /**
     * 插件名称
     */
    private String pluginName;
    
    /**
     * 方法名
     */
    private String methodName;
    
    /**
     * 参数
     */
    private Map<String, Object> parameters;
}

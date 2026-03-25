package com.aidrclaw.agent.decision;

/**
 * 脚本参数
 * 
 * 表示话术参数调整
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ScriptParameter {
    
    /**
     * 参数类型
     */
    public enum ParameterType {
        /**
         * 语速
         */
        SPEED,
        /**
         * 音调
         */
        TONE,
        /**
         * 音量
         */
        VOLUME,
        /**
         * 复杂度
         */
        COMPLEXITY
    }
    
    private ParameterType parameterType;
    private Object value;
    private String description;
    
    public ScriptParameter() {
    }
    
    public ScriptParameter(ParameterType parameterType, Object value, String description) {
        this.parameterType = parameterType;
        this.value = value;
        this.description = description;
    }
    
    public ParameterType getParameterType() {
        return parameterType;
    }
    
    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

package com.aidrclaw.core.process;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 节点跳转规则
 * 
 * 定义节点执行完成后的跳转逻辑：
 * - condition: 条件表达式（JavaScript 脚本）
 * - next: 下一节点 ID
 * - action: 可选的执行动作（如重试计数）
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class Transition {
    
    /**
     * 条件表达式
     * 示例："result.success == true" 或 "retryCount < maxRetries"
     */
    @JsonProperty("condition")
    private String condition;
    
    /**
     * 下一节点 ID
     */
    @JsonProperty("next")
    private String next;
    
    /**
     * 可选的执行动作
     * 示例："retryCount++" 或 "log('timeout')"
     */
    @JsonProperty("action")
    private String action;
    
    public Transition() {
    }
    
    public Transition(String condition, String next, String action) {
        this.condition = condition;
        this.next = next;
        this.action = action;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getNext() {
        return next;
    }
    
    public void setNext(String next) {
        this.next = next;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    @Override
    public String toString() {
        return "Transition{" +
                "condition='" + condition + '\'' +
                ", next='" + next + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}

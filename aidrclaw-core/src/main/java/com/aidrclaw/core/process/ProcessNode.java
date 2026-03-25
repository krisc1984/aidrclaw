package com.aidrclaw.core.process;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 流程节点定义
 * 
 * 表示流程中的一个执行节点，包含：
 * - 节点基本信息（ID、名称、类型）
 * - 插件配置（AI_NODE 类型需要）
 * - 超时和重试配置
 * - 跳转规则
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessNode {
    
    /**
     * 节点 ID（流程内唯一）
     */
    @JsonProperty("nodeId")
    private String nodeId;
    
    /**
     * 节点名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * 节点类型
     */
    @JsonProperty("nodeType")
    private NodeType nodeType;
    
    /**
     * 插件 ID（AI_NODE 类型需要）
     */
    @JsonProperty("plugin")
    private String plugin;
    
    /**
     * 节点脚本（AGENT_NODE 类型需要）
     */
    @JsonProperty("script")
    private String script;
    
    /**
     * 超时时间（秒）
     */
    @JsonProperty("timeout")
    private int timeout = 0;
    
    /**
     * 最大重试次数
     */
    @JsonProperty("maxRetries")
    private int maxRetries = 0;
    
    /**
     * 重试延迟（毫秒）
     */
    @JsonProperty("retryDelay")
    private int retryDelay = 0;
    
    /**
     * 跳转规则列表
     */
    @JsonProperty("transitions")
    private List<Transition> transitions;
    
    public ProcessNode() {
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public NodeType getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
    
    public String getPlugin() {
        return plugin;
    }
    
    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
    
    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public int getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public List<Transition> getTransitions() {
        return transitions;
    }
    
    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }
    
    @Override
    public String toString() {
        return "ProcessNode{" +
                "nodeId='" + nodeId + '\'' +
                ", name='" + name + '\'' +
                ", nodeType=" + nodeType +
                ", plugin='" + plugin + '\'' +
                ", timeout=" + timeout +
                ", maxRetries=" + maxRetries +
                '}';
    }
}

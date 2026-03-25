package com.aidrclaw.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程执行上下文
 * 
 * 保存流程执行过程中的会话数据、节点执行结果和流程变量
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessExecutionContext {
    
    /**
     * 会话数据（客户信息、产品信息等）
     */
    private final Map<String, Object> sessionData;
    
    /**
     * 节点执行结果
     */
    private final Map<String, com.aidrclaw.plugin.PluginResult> nodeResults;
    
    /**
     * 流程变量（可在脚本中访问）
     */
    private final Map<String, Object> variables;
    
    public ProcessExecutionContext() {
        this.sessionData = new ConcurrentHashMap<>();
        this.nodeResults = new ConcurrentHashMap<>();
        this.variables = new ConcurrentHashMap<>();
    }
    
    /**
     * 设置会话数据
     */
    public void setSessionData(Map<String, Object> data) {
        if (data != null) {
            this.sessionData.putAll(data);
        }
    }
    
    /**
     * 获取会话数据
     */
    public Map<String, Object> getSessionData() {
        return sessionData;
    }
    
    /**
     * 设置流程变量
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    /**
     * 获取流程变量
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * 设置节点执行结果
     */
    public void setNodeResult(String nodeId, com.aidrclaw.plugin.PluginResult result) {
        nodeResults.put(nodeId, result);
    }
    
    /**
     * 获取节点执行结果
     */
    public com.aidrclaw.plugin.PluginResult getNodeResult(String nodeId) {
        return nodeResults.get(nodeId);
    }
    
    /**
     * 获取所有节点执行结果
     */
    public Map<String, com.aidrclaw.plugin.PluginResult> getAllNodeResults() {
        return new ConcurrentHashMap<>(nodeResults);
    }
    
    /**
     * 获取所有流程变量
     */
    public Map<String, Object> getAllVariables() {
        return new ConcurrentHashMap<>(variables);
    }
    
    /**
     * 清除所有数据
     */
    public void clear() {
        sessionData.clear();
        nodeResults.clear();
        variables.clear();
    }
}

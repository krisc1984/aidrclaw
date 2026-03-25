package com.aidrclaw.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程实例
 * 
 * 表示一个会话的流程执行实例，包含：
 * - 会话 ID
 * - 流程定义
 * - 当前执行节点
 * - 执行状态
 * - 执行上下文
 * 
 * 每个会话有独立的流程实例，状态隔离
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessInstance {
    
    /**
     * 会话 ID
     */
    private final String sessionId;
    
    /**
     * 流程定义
     */
    private final ProcessDefinition processDefinition;
    
    /**
     * 当前节点 ID
     */
    private String currentNodeId;
    
    /**
     * 流程执行状态
     */
    private ProcessState state;
    
    /**
     * 执行上下文
     */
    private final ProcessExecutionContext context;
    
    /**
     * 节点重试次数记录
     */
    private final Map<String, Integer> retryCounts;
    
    /**
     * 执行历史（已执行的节点 ID 列表）
     */
    private final List<String> history;
    
    /**
     * 流程执行状态枚举
     */
    public enum ProcessState {
        /**
         * 运行中
         */
        RUNNING,
        /**
         * 已暂停（等待人工操作）
         */
        PAUSED,
        /**
         * 已完成
         */
        COMPLETED,
        /**
         * 异常
         */
        EXCEPTION
    }
    
    public ProcessInstance(String sessionId, ProcessDefinition processDefinition) {
        this.sessionId = sessionId;
        this.processDefinition = processDefinition;
        this.state = ProcessState.RUNNING;
        this.context = new ProcessExecutionContext();
        this.retryCounts = new ConcurrentHashMap<>();
        this.history = new ArrayList<>();
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }
    
    public String getCurrentNodeId() {
        return currentNodeId;
    }
    
    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }
    
    public ProcessState getState() {
        return state;
    }
    
    public void setState(ProcessState state) {
        this.state = state;
    }
    
    public ProcessExecutionContext getContext() {
        return context;
    }
    
    /**
     * 获取当前节点
     */
    public ProcessNode getCurrentNode() {
        if (currentNodeId == null) {
            return null;
        }
        return processDefinition.getNodeById(currentNodeId);
    }
    
    /**
     * 获取下一节点
     * 
     * @param nextNodeId 下一节点 ID
     * @return 下一节点对象，不存在返回 null
     */
    public ProcessNode getNextNode(String nextNodeId) {
        if (nextNodeId == null) {
            return null;
        }
        return processDefinition.getNodeById(nextNodeId);
    }
    
    /**
     * 标记节点已完成
     * 
     * @param nodeId 节点 ID
     */
    public void markNodeCompleted(String nodeId) {
        history.add(nodeId);
    }
    
    /**
     * 增加节点重试次数
     * 
     * @param nodeId 节点 ID
     */
    public void incrementRetryCount(String nodeId) {
        retryCounts.merge(nodeId, 1, Integer::sum);
    }
    
    /**
     * 获取节点重试次数
     * 
     * @param nodeId 节点 ID
     * @return 重试次数
     */
    public int getRetryCount(String nodeId) {
        return retryCounts.getOrDefault(nodeId, 0);
    }
    
    /**
     * 重置节点重试次数
     * 
     * @param nodeId 节点 ID
     */
    public void resetRetryCount(String nodeId) {
        retryCounts.remove(nodeId);
    }
    
    /**
     * 获取执行历史
     * 
     * @return 已执行的节点 ID 列表
     */
    public List<String> getHistory() {
        return new java.util.ArrayList<>(history);
    }
    
    /**
     * 获取所有节点重试次数
     */
    public Map<String, Integer> getAllRetryCounts() {
        return new ConcurrentHashMap<>(retryCounts);
    }
    
    @Override
    public String toString() {
        return "ProcessInstance{" +
                "sessionId='" + sessionId + '\'' +
                ", processId='" + processDefinition.getProcessId() + '\'' +
                ", currentNodeId='" + currentNodeId + '\'' +
                ", state=" + state +
                '}';
    }
}

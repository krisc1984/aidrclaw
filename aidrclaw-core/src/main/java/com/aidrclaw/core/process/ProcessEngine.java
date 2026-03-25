package com.aidrclaw.core.process;

import com.aidrclaw.plugin.PluginResult;
import com.aidrclaw.core.plugin.PluginManager;
import com.aidrclaw.core.rule.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程引擎
 * 
 * 核心流程执行引擎，负责：
 * - 启动流程
 * - 执行节点
 * - 管理节点跳转
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessEngine.class);
    
    private final ProcessDefinitionLoader definitionLoader;
    private final NodeExecutor nodeExecutor;
    private final TransitionManager transitionManager;
    
    /**
     * 会话流程实例缓存
     */
    private final Map<String, ProcessInstance> instances;
    
    public ProcessEngine(ProcessDefinitionLoader definitionLoader,
                         PluginManager pluginManager,
                         RuleEngine ruleEngine) {
        this.definitionLoader = definitionLoader;
        this.nodeExecutor = new NodeExecutor(pluginManager, ruleEngine);
        this.transitionManager = new TransitionManager(ruleEngine);
        this.instances = new ConcurrentHashMap<>();
    }
    
    /**
     * 启动流程
     * 
     * @param processId 流程 ID
     * @param sessionId 会话 ID
     * @param initialData 初始数据
     * @return 流程实例
     * @throws Exception 流程启动失败
     */
    public ProcessInstance startProcess(String processId, String sessionId, 
                                        Map<String, Object> initialData) throws Exception {
        logger.info("Starting process {} for session {}", processId, sessionId);
        
        // 加载流程定义
        ProcessDefinition definition = definitionLoader.loadFromClasspath(processId);
        
        // 创建流程实例
        ProcessInstance instance = new ProcessInstance(sessionId, definition);
        
        // 设置初始数据
        if (initialData != null) {
            instance.getContext().setSessionData(initialData);
        }
        
        // 设置第一个节点
        ProcessNode firstNode = definition.getFirstNode();
        if (firstNode == null) {
            throw new IllegalStateException("Process must have at least one node");
        }
        instance.setCurrentNodeId(firstNode.getNodeId());
        
        // 缓存实例
        instances.put(sessionId, instance);
        
        logger.info("Process started: {} -> {} (first node: {})", 
                sessionId, processId, firstNode.getNodeId());
        
        return instance;
    }
    
    /**
     * 执行当前节点
     * 
     * @param instance 流程实例
     * @return 节点执行结果
     */
    public PluginResult executeNode(ProcessInstance instance) {
        ProcessNode currentNode = instance.getCurrentNode();
        if (currentNode == null) {
            logger.warn("No current node for session {}", instance.getSessionId());
            return PluginResult.error("No current node");
        }
        
        logger.debug("Executing node {} for session {}", 
                currentNode.getNodeId(), instance.getSessionId());
        
        // 执行节点
        PluginResult result = nodeExecutor.executeNode(currentNode, instance);
        
        // 保存节点结果
        instance.getContext().setNodeResult(currentNode.getNodeId(), result);
        
        // 标记节点完成
        instance.markNodeCompleted(currentNode.getNodeId());
        
        // 查找下一节点
        String nextNodeId = transitionManager.findNextNode(currentNode, result, instance);
        
        if (nextNodeId != null) {
            // 跳转到下一节点
            transitionTo(instance, nextNodeId);
            
            // 如果下一节点不是 END_NODE，继续执行
            ProcessNode nextNode = instance.getNextNode(nextNodeId);
            if (nextNode != null && nextNode.getNodeType() != NodeType.END_NODE) {
                // 递归执行下一节点（如果是自动节点）
                if (nextNode.getNodeType() == NodeType.AI_NODE || 
                    nextNode.getNodeType() == NodeType.CONDITION_NODE) {
                    return executeNode(instance);
                }
            }
        } else {
            logger.info("No next node found for session {} - process paused", 
                    instance.getSessionId());
        }
        
        return result;
    }
    
    /**
     * 跳转到指定节点
     * 
     * @param instance 流程实例
     * @param nextNodeId 下一节点 ID
     */
    public void transitionTo(ProcessInstance instance, String nextNodeId) {
        logger.debug("Transitioning session {} to node {}", 
                instance.getSessionId(), nextNodeId);
        
        // 更新当前节点 ID
        instance.setCurrentNodeId(nextNodeId);
        
        // 更新会话状态机（如果集成）
        // sessionStateMachine.transition(mapToSessionState(instance.getState()));
        
        logger.info("Session {} now at node {}", 
                instance.getSessionId(), nextNodeId);
    }
    
    /**
     * 获取流程实例
     * 
     * @param sessionId 会话 ID
     * @return 流程实例，不存在返回 null
     */
    public ProcessInstance getInstance(String sessionId) {
        return instances.get(sessionId);
    }
    
    /**
     * 移除流程实例
     * 
     * @param sessionId 会话 ID
     */
    public void removeInstance(String sessionId) {
        instances.remove(sessionId);
        logger.info("Removed process instance for session {}", sessionId);
    }
    
    /**
     * 获取所有活跃的流桯实例
     */
    public Map<String, ProcessInstance> getAllInstances() {
        return new ConcurrentHashMap<>(instances);
    }
    
    /**
     * 清理已完成的实例
     */
    public void cleanupCompletedInstances() {
        instances.entrySet().removeIf(entry -> 
            entry.getValue().getState() == ProcessInstance.ProcessState.COMPLETED ||
            entry.getValue().getState() == ProcessInstance.ProcessState.EXCEPTION
        );
        logger.info("Cleaned up completed/exception process instances");
    }
}

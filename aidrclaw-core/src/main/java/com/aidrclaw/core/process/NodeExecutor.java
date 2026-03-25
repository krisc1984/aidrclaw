package com.aidrclaw.core.process;

import com.aidrclaw.plugin.Plugin;
import com.aidrclaw.plugin.PluginContext;
import com.aidrclaw.plugin.PluginResult;
import com.aidrclaw.core.plugin.PluginManager;
import com.aidrclaw.core.rule.RuleEngine;
import com.aidrclaw.core.rule.RuleScriptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 节点执行器
 * 
 * 根据节点类型执行不同的逻辑：
 * - AI_NODE: 调用插件执行
 * - AGENT_NODE: 执行脚本（预留）
 * - MANUAL_NODE: 等待人工操作
 * - END_NODE: 标记结束
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class NodeExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(NodeExecutor.class);
    
    private final PluginManager pluginManager;
    private final RuleEngine ruleEngine;
    
    public NodeExecutor(PluginManager pluginManager, RuleEngine ruleEngine) {
        this.pluginManager = pluginManager;
        this.ruleEngine = ruleEngine;
    }
    
    /**
     * 执行节点
     * 
     * @param node 流程节点
     * @param instance 流程实例
     * @return 执行结果
     */
    public PluginResult executeNode(ProcessNode node, ProcessInstance instance) {
        logger.info("Executing node: {} ({})", node.getNodeId(), node.getNodeType());
        
        return switch (node.getNodeType()) {
            case AI_NODE -> executeAiNode(node, instance);
            case AGENT_NODE -> executeAgentNode(node, instance);
            case MANUAL_NODE -> executeManualNode(node, instance);
            case END_NODE -> executeEndNode(node, instance);
            case CONDITION_NODE -> executeConditionNode(node, instance);
        };
    }
    
    /**
     * 执行 AI 节点
     */
    private PluginResult executeAiNode(ProcessNode node, ProcessInstance instance) {
        String pluginId = node.getPlugin();
        if (pluginId == null) {
            return PluginResult.error("AI_NODE must have a plugin configured");
        }
        
        // 构建插件上下文
        PluginContext pluginContext = new PluginContext();
        pluginContext.setParameters(instance.getContext().getSessionData());
        
        // 执行插件
        try {
            Plugin plugin = pluginManager.getPlugin(pluginId);
            if (plugin == null) {
                return PluginResult.error("Plugin not found: " + pluginId);
            }
            
            plugin.init(pluginContext);
            PluginResult result = plugin.execute(pluginContext);
            plugin.destroy();
            
            // 保存结果到上下文
            instance.getContext().setNodeResult(node.getNodeId(), result);
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to execute AI node {}: {}", node.getNodeId(), e.getMessage());
            return PluginResult.error("Plugin execution failed: " + e.getMessage());
        }
    }
    
    /**
     * 执行 Agent 节点（预留实现）
     */
    private PluginResult executeAgentNode(ProcessNode node, ProcessInstance instance) {
        logger.info("Agent node execution (placeholder): {}", node.getNodeId());
        // Phase 6 实现完整的 Agent 对话逻辑
        return PluginResult.success();
    }
    
    /**
     * 执行人工节点
     */
    private PluginResult executeManualNode(ProcessNode node, ProcessInstance instance) {
        logger.info("Manual node execution: {}", node.getNodeId());
        // 暂停流程，等待人工操作
        instance.setState(ProcessInstance.ProcessState.PAUSED);
        return PluginResult.success();
    }
    
    /**
     * 执行结束节点
     */
    private PluginResult executeEndNode(ProcessNode node, ProcessInstance instance) {
        logger.info("Process completed for session: {}", instance.getSessionId());
        instance.setState(ProcessInstance.ProcessState.COMPLETED);
        instance.markNodeCompleted(node.getNodeId());
        return PluginResult.success();
    }
    
    /**
     * 执行条件节点
     */
    private PluginResult executeConditionNode(ProcessNode node, ProcessInstance instance) {
        logger.info("Condition node evaluation: {}", node.getNodeId());
        // 条件节点本身不执行操作，只是评估条件
        return PluginResult.success();
    }
}

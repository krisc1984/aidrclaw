package com.aidrclaw.core.process;

import com.aidrclaw.plugin.PluginResult;
import com.aidrclaw.core.rule.RuleEngine;
import com.aidrclaw.core.rule.RuleScriptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.List;

/**
 * 跳转管理器
 * 
 * 负责根据节点执行结果和条件表达式决定下一节点
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class TransitionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TransitionManager.class);
    
    private final RuleEngine ruleEngine;
    
    public TransitionManager(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }
    
    /**
     * 查找下一节点
     * 
     * @param currentNode 当前节点
     * @param result 节点执行结果
     * @param instance 流程实例
     * @return 下一节点 ID，无匹配返回 null
     */
    public String findNextNode(ProcessNode currentNode, PluginResult result, 
                               ProcessInstance instance) {
        List<Transition> transitions = currentNode.getTransitions();
        
        if (transitions == null || transitions.isEmpty()) {
            logger.debug("No transitions defined for node {}", currentNode.getNodeId());
            return null;
        }
        
        // 构建脚本上下文
        RuleScriptContext context = buildScriptContext(result, instance);
        
        // 遍历所有跳转规则，找到第一个匹配条件的
        for (Transition transition : transitions) {
            try {
                boolean matches = evaluateTransition(transition, context);
                
                if (matches) {
                    logger.info("Transition matched for node {}: {} -> {}", 
                            currentNode.getNodeId(), transition.getCondition(), transition.getNext());
                    
                    // 执行跳转动作（如果有）
                    if (transition.getAction() != null && !transition.getAction().isEmpty()) {
                        executeTransitionAction(transition, context);
                    }
                    
                    return transition.getNext();
                }
            } catch (ScriptException e) {
                logger.warn("Failed to evaluate transition condition '{}': {}", 
                        transition.getCondition(), e.getMessage());
            }
        }
        
        logger.debug("No matching transition found for node {}", currentNode.getNodeId());
        return null;
    }
    
    /**
     * 评估跳转条件
     * 
     * @param transition 跳转规则
     * @param context 脚本上下文
     * @return 是否匹配
     * @throws ScriptException 脚本执行异常
     */
    private boolean evaluateTransition(Transition transition, RuleScriptContext context) 
            throws ScriptException {
        String condition = transition.getCondition();
        
        // 特殊处理：恒真条件
        if ("true".equals(condition)) {
            return true;
        }
        
        return ruleEngine.evaluateCondition(condition, context);
    }
    
    /**
     * 执行跳转动作
     * 
     * @param transition 跳转规则
     * @param context 脚本上下文
     */
    public void executeTransitionAction(Transition transition, RuleScriptContext context) {
        String action = transition.getAction();
        
        if (action == null || action.isEmpty()) {
            return;
        }
        
        try {
            logger.debug("Executing transition action: {}", action);
            ruleEngine.executeAction(action, context);
        } catch (ScriptException e) {
            logger.warn("Failed to execute transition action '{}': {}", 
                    action, e.getMessage());
        }
    }
    
    /**
     * 构建脚本上下文
     * 
     * @param result 节点执行结果
     * @param instance 流程实例
     * @return 脚本上下文
     */
    private RuleScriptContext buildScriptContext(PluginResult result, ProcessInstance instance) {
        RuleScriptContext context = new RuleScriptContext();
        
        // 添加节点执行结果
        context.setBinding("result", result != null ? result.getData() : null);
        
        // 添加重试次数
        String currentNodeId = instance.getCurrentNodeId();
        int retryCount = instance.getRetryCount(currentNodeId);
        context.setBinding("retryCount", retryCount);
        
        // 添加最大重试次数
        ProcessNode currentNode = instance.getCurrentNode();
        if (currentNode != null) {
            context.setBinding("maxRetries", currentNode.getMaxRetries());
            context.setBinding("timeout", currentNode.getTimeout());
        }
        
        // 添加流程变量
        context.setBinding("session", instance.getContext().getSessionData());
        context.setBinding("variables", instance.getContext().getAllVariables());
        
        return context;
    }
}

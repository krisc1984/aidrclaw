package com.aidrclaw.core.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 规则引擎
 * 
 * 使用 JavaScript 引擎（Nashorn/GraalVM）解析和执行条件表达式
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class RuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);
    
    private final ScriptEngine scriptEngine;
    
    public RuleEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        // 优先使用 Nashorn，如果不可用则使用默认 JavaScript 引擎
        this.scriptEngine = manager.getEngineByName("Nashorn") != null 
            ? manager.getEngineByName("Nashorn")
            : manager.getEngineByName("JavaScript");
        
        if (this.scriptEngine == null) {
            logger.warn("No JavaScript engine found. Rule evaluation will be limited.");
        } else {
            logger.info("Initialized JavaScript engine: {}", this.scriptEngine.getFactory().getEngineName());
        }
    }
    
    /**
     * 评估条件表达式
     * 
     * @param condition 条件表达式（JavaScript 语法）
     * @param context 脚本上下文
     * @return 评估结果
     * @throws ScriptException 脚本执行异常
     */
    public boolean evaluateCondition(String condition, RuleScriptContext context) throws ScriptException {
        if (scriptEngine == null) {
            logger.error("Script engine not available");
            throw new ScriptException("JavaScript engine not available");
        }
        
        if (condition == null || condition.trim().isEmpty()) {
            return false;
        }
        
        logger.debug("Evaluating condition: {}", condition);
        
        // 设置上下文绑定
        if (context != null) {
            scriptEngine.setBindings((javax.script.Bindings) context.getBindings(), javax.script.ScriptContext.ENGINE_SCOPE);
        }
        
        // 执行评估
        Object result = scriptEngine.eval(condition);
        
        // 转换为 boolean
        boolean evalResult = Boolean.TRUE.equals(result);
        logger.debug("Condition result: {}", evalResult);
        
        return evalResult;
    }
    
    /**
     * 执行动作脚本
     * 
     * @param action 动作脚本（JavaScript 语法）
     * @param context 脚本上下文
     * @throws ScriptException 脚本执行异常
     */
    public void executeAction(String action, RuleScriptContext context) throws ScriptException {
        if (scriptEngine == null) {
            logger.error("Script engine not available");
            throw new ScriptException("JavaScript engine not available");
        }
        
        if (action == null || action.trim().isEmpty()) {
            return;
        }
        
        logger.debug("Executing action: {}", action);
        
        // 设置上下文绑定
        if (context != null) {
            scriptEngine.setBindings((javax.script.Bindings) context.getBindings(), javax.script.ScriptContext.ENGINE_SCOPE);
        }
        
        // 执行动作
        scriptEngine.eval(action);
        
        logger.debug("Action executed successfully");
    }
    
    /**
     * 安全评估条件（捕获异常并返回默认值）
     * 
     * @param condition 条件表达式
     * @param context 脚本上下文
     * @param defaultValue 异常时的默认值
     * @return 评估结果
     */
    public boolean safeEvaluateCondition(String condition, RuleScriptContext context, boolean defaultValue) {
        try {
            return evaluateCondition(condition, context);
        } catch (ScriptException e) {
            logger.warn("Failed to evaluate condition '{}': {}", condition, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * 安全执行动作（捕获异常并记录）
     * 
     * @param action 动作脚本
     * @param context 脚本上下文
     */
    public void safeExecuteAction(String action, RuleScriptContext context) {
        try {
            executeAction(action, context);
        } catch (ScriptException e) {
            logger.warn("Failed to execute action '{}': {}", action, e.getMessage());
        }
    }
}

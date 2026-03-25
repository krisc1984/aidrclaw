package com.aidrclaw.core.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则引擎测试
 */
class RuleEngineTest {

    private RuleEngine ruleEngine;
    private RuleScriptContext scriptContext;

    @BeforeEach
    void setUp() {
        ruleEngine = new RuleEngine();
        scriptContext = new RuleScriptContext();
    }

    @Test
    void testEvaluateCondition_SimpleBoolean() throws ScriptException {
        // 设置上下文
        scriptContext.setBinding("result", java.util.Map.of("success", true));
        
        // 评估条件
        boolean eval = ruleEngine.evaluateCondition("result.success == true", scriptContext);
        
        // 验证
        assertTrue(eval);
    }

    @Test
    void testEvaluateCondition_Comparison() throws ScriptException {
        // 设置上下文
        scriptContext.setBinding("retryCount", 2);
        scriptContext.setBinding("maxRetries", 3);
        
        // 评估条件
        boolean eval = ruleEngine.evaluateCondition("retryCount < maxRetries", scriptContext);
        
        // 验证
        assertTrue(eval);
    }

    @Test
    void testEvaluateCondition_ComplexExpression() throws ScriptException {
        // 设置上下文
        scriptContext.setBinding("result", java.util.Map.of("success", false));
        scriptContext.setBinding("retryCount", 1);
        scriptContext.setBinding("maxRetries", 3);
        
        // 评估复杂条件
        boolean eval = ruleEngine.evaluateCondition(
            "result.success == false && retryCount < maxRetries", 
            scriptContext
        );
        
        // 验证
        assertTrue(eval);
    }

    @Test
    void testEvaluateCondition_AlwaysTrue() throws ScriptException {
        // 评估恒真条件
        boolean eval = ruleEngine.evaluateCondition("true", scriptContext);
        
        // 验证
        assertTrue(eval);
    }

    @Test
    void testEvaluateCondition_WithNullContext() throws ScriptException {
        // 评估简单条件（无上下文）
        boolean eval = ruleEngine.evaluateCondition("true", null);
        
        // 验证
        assertTrue(eval);
    }

    @Test
    void testExecuteAction_Increment() throws ScriptException {
        // 设置初始值
        scriptContext.setBinding("retryCount", 0);
        
        // 执行动作
        ruleEngine.executeAction("retryCount = retryCount + 1", scriptContext);
        
        // 验证
        assertEquals(1, scriptContext.getBinding("retryCount"));
    }

    @Test
    void testExecuteAction_SetVariable() throws ScriptException {
        // 执行动作
        ruleEngine.executeAction("status = 'completed'", scriptContext);
        
        // 验证
        assertEquals("completed", scriptContext.getBinding("status"));
    }

    @Test
    void testEvaluateCondition_InvalidSyntax() {
        // 验证语法错误抛出异常
        assertThrows(ScriptException.class, () -> {
            ruleEngine.evaluateCondition("invalid syntax +++", scriptContext);
        });
    }

    @Test
    void testScriptContext_BasicOperations() {
        // 设置绑定
        scriptContext.setBinding("customerName", "张三");
        scriptContext.setBinding("riskLevel", "R3");
        
        // 获取绑定
        assertEquals("张三", scriptContext.getBinding("customerName"));
        assertEquals("R3", scriptContext.getBinding("riskLevel"));
        
        // 获取不存在的绑定
        assertNull(scriptContext.getBinding("nonExistent"));
    }

    @Test
    void testScriptContext_WithObject() {
        // 设置复杂对象
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", true);
        result.put("score", 95);
        scriptContext.setBinding("result", result);
        
        // 获取对象
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> retrieved = 
            (java.util.Map<String, Object>) scriptContext.getBinding("result");
        
        assertNotNull(retrieved);
        assertEquals(true, retrieved.get("success"));
        assertEquals(95, retrieved.get("score"));
    }
}

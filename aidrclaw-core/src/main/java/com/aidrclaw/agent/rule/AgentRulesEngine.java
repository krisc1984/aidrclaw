package com.aidrclaw.agent.rule;

import com.aidrclaw.agent.state.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Agent 规则引擎
 * 
 * 基于 Spring Expression Language (SpEL) 实现规则评估
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class AgentRulesEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentRulesEngine.class);
    
    private final ExpressionParser parser;
    private final List<BusinessRule> registeredRules;
    
    public AgentRulesEngine() {
        this.parser = new SpelExpressionParser();
        this.registeredRules = new ArrayList<>();
        
        // 注册默认规则
        registerDefaultRules();
    }
    
    /**
     * 注册默认规则
     */
    private void registerDefaultRules() {
        // 意图识别规则
        BusinessRule intentRule = BusinessRule.builder()
                .id("intent-recognition")
                .name("意图识别规则")
                .category(RuleCategory.CONDITIONAL_JUMP)
                .conditionExpression("input != null and input != ''")
                .actionJson("{\"type\":\"RECOGNIZE_INTENT\"}")
                .priority(200)
                .enabled(true)
                .build();
        registeredRules.add(intentRule);
        
        logger.info("Registered {} default rules", registeredRules.size());
    }
    
    /**
     * 注册规则
     */
    public void registerRule(BusinessRule rule) {
        registeredRules.add(rule);
        logger.info("Registered rule: {}", rule.getName());
    }
    
    /**
     * 执行规则
     */
    public void fireRules(DialogState state, String input) {
        if (state == null) {
            logger.warn("Cannot fire rules with null state");
            return;
        }
        
        StandardEvaluationContext context = buildContext(state, input);
        
        List<BusinessRule> sortedRules = new ArrayList<>(registeredRules);
        sortedRules.sort(Comparator.comparingInt(BusinessRule::getPriority).reversed());
        
        for (BusinessRule rule : sortedRules) {
            if (!rule.getEnabled()) {
                continue;
            }
            
            try {
                boolean matches = evaluateCondition(rule.getConditionExpression(), context);
                if (matches) {
                    logger.debug("Rule matched: {}", rule.getName());
                    executeAction(rule, state, context);
                }
            } catch (Exception e) {
                logger.warn("Failed to evaluate rule '{}': {}", rule.getName(), e.getMessage());
            }
        }
    }
    
    private StandardEvaluationContext buildContext(DialogState state, String input) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("state", state);
        context.setVariable("input", input);
        context.setVariable("session", state);
        context.setVariable("currentNodeId", state.getCurrentNodeId());
        context.setVariable("intent", state.getCurrentIntent());
        if (state.getFacts() != null) {
            state.getFacts().forEach(context::setVariable);
        }
        return context;
    }
    
    private boolean evaluateCondition(String expression, StandardEvaluationContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        Object result = parser.parseExpression(expression).getValue(context);
        return result instanceof Boolean && (Boolean) result;
    }
    
    private void executeAction(BusinessRule rule, DialogState state, StandardEvaluationContext context) {
        String actionJson = rule.getActionJson();
        if (actionJson == null || actionJson.isEmpty()) {
            return;
        }
        
        if (actionJson.contains("RECOGNIZE_INTENT")) {
            Object inputObj = context.lookupVariable("input");
            if (inputObj instanceof String) {
                String input = (String) inputObj;
                String intent = recognizeIntent(input);
                state.setCurrentIntent(intent);
                context.setVariable("intent", intent);
            }
        }
    }
    
    private String recognizeIntent(String input) {
        String normalizedInput = input.trim().toLowerCase();
        
        String[] confirmKeywords = {"是的", "对", "好的", "明白", "理解", "确认"};
        for (String keyword : confirmKeywords) {
            if (normalizedInput.contains(keyword.toLowerCase())) {
                return "CONFIRM";
            }
        }
        
        String[] denyKeywords = {"不是", "不对", "没有", "不理解", "不明白"};
        for (String keyword : denyKeywords) {
            if (normalizedInput.contains(keyword.toLowerCase())) {
                return "DENY";
            }
        }
        
        return "QUESTION";
    }
    
    public List<BusinessRule> getRulesForState(DialogState state) {
        return new ArrayList<>(registeredRules);
    }
}

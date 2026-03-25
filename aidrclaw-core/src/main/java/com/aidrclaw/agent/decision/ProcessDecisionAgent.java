package com.aidrclaw.agent.decision;

import com.aidrclaw.agent.rule.AgentRulesEngine;
import com.aidrclaw.agent.rule.BusinessRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 流程决策 Agent
 * 
 * 根据业务规则动态调整双录流程
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class ProcessDecisionAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessDecisionAgent.class);
    
    private final ExpressionParser parser;
    private final List<BusinessRule> businessRules;
    
    public ProcessDecisionAgent() {
        this.parser = new SpelExpressionParser();
        this.businessRules = new ArrayList<>();
        initializeDefaultRules();
    }
    
    /**
     * 初始化默认规则
     */
    private void initializeDefaultRules() {
        // 高风险产品增加风险揭示规则
        BusinessRule highRiskRule = BusinessRule.builder()
                .id("high-risk-warning")
                .name("高风险产品增加风险揭示")
                .conditionExpression("#product.riskLevel == 'R4' || #product.riskLevel == 'R5'")
                .actionJson("{\"type\":\"INSERT_NODE\",\"nodeId\":\"EXTRA_RISK_WARNING\"}")
                .priority(100)
                .enabled(true)
                .build();
        businessRules.add(highRiskRule);
        
        // 老年客户语速降低规则
        BusinessRule elderlyRule = BusinessRule.builder()
                .id("elderly-slow-speech")
                .name("老年客户语速降低")
                .conditionExpression("#customer.age >= 60")
                .actionJson("{\"type\":\"MODIFY_NODE\",\"parameters\":{\"speed\":0.8}}")
                .priority(90)
                .enabled(true)
                .build();
        businessRules.add(elderlyRule);
        
        // 失败 3 次转人工规则
        BusinessRule failureRule = BusinessRule.builder()
                .id("failure-handoff")
                .name("失败 3 次转人工")
                .conditionExpression("#session.failureCount >= 3")
                .actionJson("{\"type\":\"CHANGE_FLOW\",\"newFlowId\":\"manual-review\"}")
                .priority(200)
                .enabled(true)
                .build();
        businessRules.add(failureRule);
        
        logger.info("Initialized {} default business rules", businessRules.size());
    }
    
    /**
     * 评估上下文并返回决策结果
     */
    public DecisionResult evaluateContext(DecisionContext context) {
        if (context == null) {
            logger.warn("Cannot evaluate null context");
            return DecisionResult.noAdjustment();
        }
        
        // 构建 SpEL 上下文
        StandardEvaluationContext spelContext = buildSpelContext(context);
        
        // 按优先级排序规则
        List<BusinessRule> sortedRules = new ArrayList<>(businessRules);
        sortedRules.sort(Comparator.comparingInt(BusinessRule::getPriority).reversed());
        
        // 评估规则
        DecisionResult result = DecisionResult.noAdjustment();
        for (BusinessRule rule : sortedRules) {
            if (!rule.getEnabled()) {
                continue;
            }
            
            try {
                boolean matches = evaluateCondition(rule.getConditionExpression(), spelContext);
                if (matches) {
                    logger.info("Business rule matched: {}", rule.getName());
                    result = executeAction(rule, context, spelContext);
                    if (result.isAdjusted()) {
                        break; // 只执行第一个匹配的规则
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to evaluate rule '{}': {}", rule.getName(), e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 构建 SpEL 上下文
     */
    private StandardEvaluationContext buildSpelContext(DecisionContext context) {
        StandardEvaluationContext spelContext = new StandardEvaluationContext();
        
        // 添加快捷变量
        if (context.getProduct() != null) {
            spelContext.setVariable("product", context.getProduct());
        }
        if (context.getCustomer() != null) {
            spelContext.setVariable("customer", context.getCustomer());
        }
        if (context.getSession() != null) {
            spelContext.setVariable("session", context.getSession());
        }
        
        // 添加上下文变量
        if (context.getVariables() != null) {
            context.getVariables().forEach(spelContext::setVariable);
        }
        
        return spelContext;
    }
    
    /**
     * 评估条件
     */
    private boolean evaluateCondition(String expression, StandardEvaluationContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        Object result = parser.parseExpression(expression).getValue(context);
        return result instanceof Boolean && (Boolean) result;
    }
    
    /**
     * 执行动作
     */
    private DecisionResult executeAction(BusinessRule rule, DecisionContext context, StandardEvaluationContext spelContext) {
        String actionJson = rule.getActionJson();
        if (actionJson == null || actionJson.isEmpty()) {
            return DecisionResult.noAdjustment();
        }
        
        // TODO: 解析 JSON 并执行动作
        // 目前仅支持简单的流程调整
        
        if (actionJson.contains("INSERT_NODE")) {
            return DecisionResult.adjustFlow(rule.getId(), "插入节点：" + rule.getName());
        } else if (actionJson.contains("CHANGE_FLOW")) {
            return DecisionResult.adjustFlow("manual-review", "转人工审核");
        } else if (actionJson.contains("MODIFY_NODE")) {
            DecisionResult result = DecisionResult.noAdjustment();
            result.setParameter("speed", 0.8);
            result.setReason("老年客户语速降低");
            return result;
        }
        
        return DecisionResult.noAdjustment();
    }
    
    /**
     * 注册业务规则
     */
    public void registerRule(BusinessRule rule) {
        businessRules.add(rule);
        logger.info("Registered business rule: {}", rule.getName());
    }
    
    /**
     * 获取决策原因
     */
    public String getDecisionReason(DecisionResult result) {
        return result != null ? result.getReason() : "无决策";
    }
}

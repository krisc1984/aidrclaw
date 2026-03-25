package com.aidrclaw.agent.decision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 决策上下文
 * 
 * 封装会话、产品、客户信息，供决策 Agent 使用
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionContext {
    
    /**
     * 会话对象
     */
    private Object session;
    
    /**
     * 产品信息
     */
    private ProductInfo product;
    
    /**
     * 客户信息
     */
    private CustomerInfo customer;
    
    /**
     * 上下文变量
     */
    @Builder.Default
    private Map<String, Object> variables = new java.util.HashMap<>();
    
    /**
     * 获取产品风险等级
     */
    public String getProductRiskLevel() {
        return product != null ? product.getRiskLevel() : null;
    }
    
    /**
     * 获取客户年龄
     */
    public Integer getCustomerAge() {
        return customer != null ? customer.getAge() : null;
    }
    
    /**
     * 是否有投资经验
     */
    public Boolean hasInvestmentExperience() {
        return customer != null ? customer.getHasInvestmentExperience() : false;
    }
    
    /**
     * 获取上下文变量
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * 产品信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String productId;
        private String productName;
        private String riskLevel; // R1, R2, R3, R4, R5
    }
    
    /**
     * 客户信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String customerId;
        private String customerName;
        private Integer age;
        private Boolean hasInvestmentExperience;
    }
}

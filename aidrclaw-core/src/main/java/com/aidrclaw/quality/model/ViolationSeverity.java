package com.aidrclaw.quality.model;

/**
 * 违规严重程度枚举
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public enum ViolationSeverity {
    
    /**
     * 一般违规（扣 5 分）
     */
    GENERAL(5),
    
    /**
     * 严重违规（扣 20 分）
     */
    SEVERE(20);
    
    private final int deduction;
    
    ViolationSeverity(int deduction) {
        this.deduction = deduction;
    }
    
    /**
     * 获取扣分值
     */
    public int getDeduction() {
        return deduction;
    }
}

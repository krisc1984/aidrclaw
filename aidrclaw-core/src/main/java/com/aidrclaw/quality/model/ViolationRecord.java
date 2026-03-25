package com.aidrclaw.quality.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 违规记录
 * 
 * 记录检测到的违规词汇或行为
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationRecord {
    
    /**
     * 违规词或行为描述
     */
    private String word;
    
    /**
     * 严重程度
     */
    private Severity severity;
    
    /**
     * 出现时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 出现位置（如对话历史中的索引）
     */
    private Integer position;
    
    /**
     * 扣分分值
     */
    private Integer deduction;
    
    /**
     * 严重程度枚举
     */
    public enum Severity {
        /**
         * 一般违规（扣 5 分）
         */
        NORMAL(5),
        /**
         * 严重违规（扣 20 分）
         */
        SERIOUS(20);
        
        private final int deduction;
        
        Severity(int deduction) {
            this.deduction = deduction;
        }
        
        public int getDeduction() {
            return deduction;
        }
    }
}

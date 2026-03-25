package com.aidrclaw.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话树元数据
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogTreeMetadata {
    
    /**
     * 适用产品列表
     */
    private List<String> applicableProducts;
    
    /**
     * 适用风险等级
     */
    private String riskLevel;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private Long createdAt;
    
    /**
     * 更新时间
     */
    private Long updatedAt;
}

package com.aidrclaw.quality.service;

import com.aidrclaw.quality.config.FinancialViolationWords;
import com.aidrclaw.quality.model.ViolationRecord;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词检测服务
 * 
 * 基于 DFA 算法的敏感词检测，支持金融违规词检测
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Service
public class SensitiveWordService {
    
    @Autowired
    private SensitiveWordBs sensitiveWordBs;
    
    /**
     * 检测文本中的违规词
     * 
     * @param text 待检测文本
     * @return 违规记录列表
     */
    public List<ViolationRecord> detectViolations(String text) {
        List<ViolationRecord> violations = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return violations;
        }
        
        // 检测一般违规词
        for (String word : FinancialViolationWords.NORMAL_VIOLATIONS) {
            if (text.contains(word)) {
                violations.add(ViolationRecord.builder()
                        .word(word)
                        .severity(ViolationRecord.Severity.NORMAL)
                        .deduction(ViolationRecord.Severity.NORMAL.getDeduction())
                        .build());
            }
        }
        
        // 检测严重违规词
        for (String word : FinancialViolationWords.SERIOUS_VIOLATIONS) {
            if (text.contains(word)) {
                violations.add(ViolationRecord.builder()
                        .word(word)
                        .severity(ViolationRecord.Severity.SERIOUS)
                        .deduction(ViolationRecord.Severity.SERIOUS.getDeduction())
                        .build());
            }
        }
        
        return violations;
    }
    
    /**
     * 检查文本是否包含违规词
     * 
     * @param text 待检测文本
     * @return 是否包含违规词
     */
    public boolean containsViolation(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 使用 sensitive-word 库快速检测
        return sensitiveWordBs.contains(text);
    }
    
    /**
     * 替换文本中的违规词为*
     * 
     * @param text 待处理文本
     * @return 替换后的文本
     */
    public String replaceViolations(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return sensitiveWordBs.replace(text);
    }
}

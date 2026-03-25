package com.aidrclaw.quality.rules;

import com.aidrclaw.quality.model.ViolationRecord;
import com.aidrclaw.quality.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 违规词检测规则
 * 
 * 检测对话历史中的违规词汇
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class ViolationKeywordsRule {
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    /**
     * 检测违规词
     * 
     * @param transcript 对话文本
     * @return 违规记录列表
     */
    public List<ViolationRecord> detect(String transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return new ArrayList<>();
        }
        
        return sensitiveWordService.detectViolations(transcript);
    }
    
    /**
     * 检测对话历史中的违规词
     * 
     * @param history 对话历史列表
     * @return 违规记录列表
     */
    public List<ViolationRecord> detectFromHistory(List<String> history) {
        List<ViolationRecord> allViolations = new ArrayList<>();
        
        if (history == null || history.isEmpty()) {
            return allViolations;
        }
        
        for (int i = 0; i < history.size(); i++) {
            String text = history.get(i);
            List<ViolationRecord> violations = detect(text);
            
            // 设置位置信息
            for (ViolationRecord violation : violations) {
                violation.setPosition(i);
                violation.setTimestamp(System.currentTimeMillis());
            }
            
            allViolations.addAll(violations);
        }
        
        return allViolations;
    }
}

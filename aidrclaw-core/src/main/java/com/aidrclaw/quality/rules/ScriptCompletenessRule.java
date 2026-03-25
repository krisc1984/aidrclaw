package com.aidrclaw.quality.rules;

import com.aidrclaw.agent.state.DialogState;
import com.aidrclaw.quality.model.ScriptCompletionResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 话术完整性检测规则
 * 
 * 检查双录流程中必需话术环节的完成情况
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class ScriptCompletenessRule {
    
    /**
     * 必需的话术节点 ID 列表
     */
    private static final Set<String> REQUIRED_NODE_IDS = Set.of(
        "identity-verification",    // 身份核验
        "risk-disclosure",          // 风险揭示
        "product-introduction",     // 产品介绍
        "confirmation"              // 确认签字
    );
    
    /**
     * 检查话术完整性
     * 
     * @param state 对话状态
     * @return 话术完成结果
     */
    public ScriptCompletionResult check(DialogState state) {
        if (state == null || state.getHistory() == null) {
            return ScriptCompletionResult.calculate(0, 0, List.of());
        }
        
        List<String> history = state.getHistory();
        List<String> missingNodes = new ArrayList<>();
        
        // 检查每个必需节点是否完成
        for (String requiredNodeId : REQUIRED_NODE_IDS) {
            if (!history.contains(requiredNodeId)) {
                missingNodes.add(requiredNodeId);
            }
        }
        
        int completed = REQUIRED_NODE_IDS.size() - missingNodes.size();
        int total = REQUIRED_NODE_IDS.size();
        
        return ScriptCompletionResult.calculate(total, completed, missingNodes);
    }
}

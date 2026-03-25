package com.aidrclaw.agent.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对话状态
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogState {
    
    /**
     * 会话 ID
     */
    private String sessionId;
    
    /**
     * 对话树 ID
     */
    private String dialogTreeId;
    
    /**
     * 当前节点 ID
     */
    private String currentNodeId;
    
    /**
     * 历史路径（已访问的节点 ID 列表）
     */
    @Builder.Default
    private List<String> history = new ArrayList<>();
    
    /**
     * 重试次数（当前节点）
     */
    @Builder.Default
    private Map<String, Integer> retryCounts = new HashMap<>();
    
    /**
     * 上下文变量
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();
    
    /**
     * 事实数据（规则执行中间结果）
     */
    @Builder.Default
    private Map<String, Object> facts = new HashMap<>();
    
    /**
     * 下一节点 ID（规则引擎设置）
     */
    private String nextNodeId;
    
    /**
     * 当前意图
     */
    private String currentIntent;
    
    /**
     * 状态时间戳
     */
    private Long timestamp;
    
    /**
     * 添加历史记录
     */
    public void addToHistory(String nodeId) {
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(nodeId);
    }
    
    /**
     * 增加重试次数
     */
    public void incrementRetryCount(String nodeId) {
        if (retryCounts == null) {
            retryCounts = new HashMap<>();
        }
        retryCounts.merge(nodeId, 1, Integer::sum);
    }
    
    /**
     * 获取重试次数
     */
    public int getRetryCount(String nodeId) {
        return retryCounts != null ? retryCounts.getOrDefault(nodeId, 0) : 0;
    }
}

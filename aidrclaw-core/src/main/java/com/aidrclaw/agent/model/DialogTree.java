package com.aidrclaw.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话树
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogTree {
    
    /**
     * 对话树唯一标识
     */
    private String id;
    
    /**
     * 对话树名称
     */
    private String name;
    
    /**
     * 根节点 ID
     */
    private String rootNodeId;
    
    /**
     * 节点映射表
     */
    @Builder.Default
    private Map<String, DialogNode> nodes = new HashMap<>();
    
    /**
     * 元数据
     */
    private DialogTreeMetadata metadata;
    
    /**
     * 获取根节点
     */
    public DialogNode getRootNode() {
        if (rootNodeId == null || nodes == null) {
            return null;
        }
        return nodes.get(rootNodeId);
    }
    
    /**
     * 获取指定节点
     */
    public DialogNode getNode(String nodeId) {
        if (nodes == null || nodeId == null) {
            return null;
        }
        return nodes.get(nodeId);
    }
}

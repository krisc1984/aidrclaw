package com.aidrclaw.core.process;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 流程定义
 * 
 * 表示一个完整的双录流程定义，包含：
 * - 流程基本信息（ID、名称、版本、描述）
 * - 节点列表
 * 
 * 流程定义通过 YAML 配置文件加载，支持热插拔
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessDefinition {
    
    /**
     * 流程 ID（全局唯一）
     */
    @JsonProperty("processId")
    private String processId;
    
    /**
     * 流程名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * 流程版本
     */
    @JsonProperty("version")
    private String version;
    
    /**
     * 流程描述
     */
    @JsonProperty("description")
    private String description;
    
    /**
     * 流程节点列表
     */
    @JsonProperty("nodes")
    private List<ProcessNode> nodes;
    
    public ProcessDefinition() {
    }
    
    public String getProcessId() {
        return processId;
    }
    
    public void setProcessId(String processId) {
        this.processId = processId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<ProcessNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<ProcessNode> nodes) {
        this.nodes = nodes;
    }
    
    /**
     * 根据节点 ID 查找节点
     * 
     * @param nodeId 节点 ID
     * @return 找到的节点，不存在返回 null
     */
    public ProcessNode getNodeById(String nodeId) {
        if (nodes == null || nodeId == null) {
            return null;
        }
        return nodes.stream()
                .filter(node -> node.getNodeId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取第一个节点
     * 
     * @return 第一个节点，没有节点返回 null
     */
    public ProcessNode getFirstNode() {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        return nodes.get(0);
    }
    
    @Override
    public String toString() {
        return "ProcessDefinition{" +
                "processId='" + processId + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", nodes=" + (nodes != null ? nodes.size() : 0) +
                '}';
    }
}

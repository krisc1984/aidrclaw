package com.aidrclaw.core.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程定义加载器
 * 
 * 从 classpath:processes/ 目录加载 YAML 格式的流程定义文件
 * 支持缓存机制，避免重复加载
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class ProcessDefinitionLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionLoader.class);
    
    private static final String PROCESSES_PATH = "processes/";
    private static final String YAML_EXTENSION = ".yml";
    
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ProcessDefinition> cache;
    
    public ProcessDefinitionLoader() {
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        this.cache = new ConcurrentHashMap<>();
    }
    
    /**
     * 从 classpath 加载流程定义
     * 
     * @param resourcePath classpath 资源路径（如：processes/wealth-product-recording.yml）
     * @return 流程定义对象
     * @throws IOException 文件不存在或解析失败时抛出
     */
    public ProcessDefinition loadFromYaml(String resourcePath) throws IOException {
        // 先查缓存
        if (cache.containsKey(resourcePath)) {
            logger.debug("Cache hit for process definition: {}", resourcePath);
            return cache.get(resourcePath);
        }
        
        // 从 classpath 加载
        logger.debug("Loading process definition from: {}", resourcePath);
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            
            if (inputStream == null) {
                throw new IOException("Process definition not found: " + resourcePath);
            }
            
            ProcessDefinition definition = objectMapper.readValue(inputStream, ProcessDefinition.class);
            
            // 验证流程定义
            validateProcessDefinition(definition);
            
            // 放入缓存
            cache.put(resourcePath, definition);
            logger.info("Successfully loaded process definition: {} ({})", 
                    definition.getProcessId(), definition.getName());
            
            return definition;
        }
    }
    
    /**
     * 根据流程 ID 从 classpath 加载流程定义
     * 自动查找 processes/{processId}.yml
     * 
     * @param processId 流程 ID
     * @return 流程定义对象
     * @throws IOException 文件不存在或解析失败时抛出
     */
    public ProcessDefinition loadFromClasspath(String processId) throws IOException {
        String resourcePath = PROCESSES_PATH + processId + YAML_EXTENSION;
        return loadFromYaml(resourcePath);
    }
    
    /**
     * 验证流程定义的有效性
     * 
     * @param definition 流程定义
     * @throws IllegalArgumentException 流程定义无效时抛出
     */
    private void validateProcessDefinition(ProcessDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Process definition cannot be null");
        }
        
        if (definition.getProcessId() == null || definition.getProcessId().isEmpty()) {
            throw new IllegalArgumentException("Process ID is required");
        }
        
        if (definition.getName() == null || definition.getName().isEmpty()) {
            throw new IllegalArgumentException("Process name is required");
        }
        
        if (definition.getNodes() == null || definition.getNodes().isEmpty()) {
            throw new IllegalArgumentException("Process must have at least one node");
        }
        
        // 验证每个节点
        for (ProcessNode node : definition.getNodes()) {
            validateProcessNode(node);
        }
        
        // 验证流程必须有结束节点
        boolean hasEndNode = definition.getNodes().stream()
                .anyMatch(node -> node.getNodeType() == NodeType.END_NODE);
        if (!hasEndNode) {
            throw new IllegalArgumentException("Process must have an END_NODE");
        }
    }
    
    /**
     * 验证流程节点的有效性
     * 
     * @param node 流程节点
     * @throws IllegalArgumentException 节点无效时抛出
     */
    private void validateProcessNode(ProcessNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Process node cannot be null");
        }
        
        if (node.getNodeId() == null || node.getNodeId().isEmpty()) {
            throw new IllegalArgumentException("Node ID is required");
        }
        
        if (node.getName() == null || node.getName().isEmpty()) {
            throw new IllegalArgumentException("Node name is required");
        }
        
        if (node.getNodeType() == null) {
            throw new IllegalArgumentException("Node type is required for node: " + node.getNodeId());
        }
        
        // AI_NODE 必须有插件配置
        if (node.getNodeType() == NodeType.AI_NODE && 
                (node.getPlugin() == null || node.getPlugin().isEmpty())) {
            throw new IllegalArgumentException("AI_NODE must have a plugin configured: " + node.getNodeId());
        }
        
        // 非 END_NODE 必须有跳转规则
        if (node.getNodeType() != NodeType.END_NODE) {
            if (node.getTransitions() == null || node.getTransitions().isEmpty()) {
                throw new IllegalArgumentException("Non-END_NODE must have transitions: " + node.getNodeId());
            }
            
            // 验证每个跳转的 next 节点 ID 不为空
            for (Transition transition : node.getTransitions()) {
                if (transition.getNext() == null || transition.getNext().isEmpty()) {
                    throw new IllegalArgumentException("Transition 'next' is required in node: " + node.getNodeId());
                }
            }
        }
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        cache.clear();
        logger.info("Process definition cache cleared");
    }
    
    /**
     * 从缓存中移除指定流程定义
     * 
     * @param resourcePath 资源路径
     */
    public void removeFromCache(String resourcePath) {
        cache.remove(resourcePath);
        logger.debug("Removed process definition from cache: {}", resourcePath);
    }
}

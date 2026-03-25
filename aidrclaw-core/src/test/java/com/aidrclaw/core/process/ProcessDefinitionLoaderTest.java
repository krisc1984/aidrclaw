package com.aidrclaw.core.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程定义加载器测试
 */
class ProcessDefinitionLoaderTest {
    
    private ProcessDefinitionLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ProcessDefinitionLoader();
    }
    
    @Test
    void testLoadWealthProductRecording() throws IOException {
        // 当：加载理财产品双录流程
        ProcessDefinition definition = loader.loadFromClasspath("wealth-product-recording");
        
        // 那么：流程定义正确
        assertNotNull(definition);
        assertEquals("wealth-product-recording", definition.getProcessId());
        assertEquals("理财产品双录流程", definition.getName());
        assertEquals("1.0", definition.getVersion());
        assertNotNull(definition.getNodes());
        assertTrue(definition.getNodes().size() >= 4);
        
        // 验证第一个节点
        ProcessNode firstNode = definition.getFirstNode();
        assertNotNull(firstNode);
        assertEquals("identity-verification", firstNode.getNodeId());
        assertEquals(NodeType.AI_NODE, firstNode.getNodeType());
        assertEquals("face-plugin", firstNode.getPlugin());
        assertEquals(300, firstNode.getTimeout());
        assertEquals(3, firstNode.getMaxRetries());
    }
    
    @Test
    void testLoadFundProductRecording() throws IOException {
        // 当：加载基金产品双录流程
        ProcessDefinition definition = loader.loadFromClasspath("fund-product-recording");
        
        // 那么：流程定义正确
        assertNotNull(definition);
        assertEquals("fund-product-recording", definition.getProcessId());
        assertEquals("基金产品双录流程", definition.getName());
        
        // 验证包含风险承受能力评估节点
        ProcessNode riskAssessmentNode = definition.getNodeById("risk-assessment");
        assertNotNull(riskAssessmentNode);
        assertEquals(NodeType.AI_NODE, riskAssessmentNode.getNodeType());
        assertEquals("risk-eval-plugin", riskAssessmentNode.getPlugin());
    }
    
    @Test
    void testLoadNonExistentProcess() {
        // 当：加载不存在的流程
        // 那么：抛出 IOException
        assertThrows(IOException.class, () -> {
            loader.loadFromClasspath("non-existent-process");
        });
    }
    
    @Test
    void testCacheMechanism() throws IOException {
        // 第一次加载
        ProcessDefinition def1 = loader.loadFromClasspath("wealth-product-recording");
        
        // 第二次加载（应该命中缓存）
        ProcessDefinition def2 = loader.loadFromClasspath("wealth-product-recording");
        
        // 验证是同一个对象（缓存命中）
        assertSame(def1, def2);
    }
    
    @Test
    void testClearCache() throws IOException {
        // 加载流程到缓存
        loader.loadFromClasspath("wealth-product-recording");
        
        // 清除缓存
        loader.clearCache();
        
        // 重新加载（不应该命中缓存）
        ProcessDefinition definition = loader.loadFromClasspath("wealth-product-recording");
        assertNotNull(definition);
    }
    
    @Test
    void testGetNodeById() throws IOException {
        ProcessDefinition definition = loader.loadFromClasspath("wealth-product-recording");
        
        // 查找存在的节点
        ProcessNode node = definition.getNodeById("identity-verification");
        assertNotNull(node);
        assertEquals("身份核验", node.getName());
        
        // 查找不存在的节点
        ProcessNode nonExistent = definition.getNodeById("non-existent");
        assertNull(nonExistent);
    }
}

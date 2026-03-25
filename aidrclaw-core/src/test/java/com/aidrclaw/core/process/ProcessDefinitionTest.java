package com.aidrclaw.core.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程定义模型测试
 */
class ProcessDefinitionTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper(new YAMLFactory());
    }

    @Test
    void testProcessDefinition_FromYaml() throws Exception {
        // 给定：一个 YAML 格式的流程定义
        String yaml = """
            processId: test-process
            name: 测试流程
            version: "1.0"
            description: 用于测试的流程定义
            nodes:
              - nodeId: start
                name: 开始节点
                nodeType: AI_NODE
                plugin: test-plugin
                timeout: 300
                maxRetries: 3
                transitions:
                  - condition: "result.success == true"
                    next: end
              - nodeId: end
                name: 结束节点
                nodeType: END_NODE
            """;

        // 当：解析 YAML 为 ProcessDefinition 对象
        ProcessDefinition definition = objectMapper.readValue(yaml, ProcessDefinition.class);

        // 那么：流程定义字段正确
        assertNotNull(definition);
        assertEquals("test-process", definition.getProcessId());
        assertEquals("测试流程", definition.getName());
        assertEquals("1.0", definition.getVersion());
        assertEquals("用于测试的流程定义", definition.getDescription());
        assertNotNull(definition.getNodes());
        assertEquals(2, definition.getNodes().size());
    }

    @Test
    void testProcessNode_HasAllFields() throws Exception {
        // 给定：一个完整的流程节点 YAML
        String yaml = """
            nodeId: identity-check
            name: 身份核验
            nodeType: AI_NODE
            plugin: face-plugin
            timeout: 300
            maxRetries: 3
            retryDelay: 1000
            transitions:
              - condition: "result.success == true"
                next: risk-disclosure
              - condition: "result.success == false"
                next: identity-check
            """;

        // 当：解析为 ProcessNode
        ProcessNode node = objectMapper.readValue(yaml, ProcessNode.class);

        // 那么：所有字段正确
        assertNotNull(node);
        assertEquals("identity-check", node.getNodeId());
        assertEquals("身份核验", node.getName());
        assertEquals(NodeType.AI_NODE, node.getNodeType());
        assertEquals("face-plugin", node.getPlugin());
        assertEquals(300, node.getTimeout());
        assertEquals(3, node.getMaxRetries());
        assertEquals(1000, node.getRetryDelay());
        assertNotNull(node.getTransitions());
        assertEquals(2, node.getTransitions().size());
    }

    @Test
    void testNodeType_EnumValues() {
        // 验证所有节点类型枚举值存在
        assertNotNull(NodeType.AI_NODE);
        assertNotNull(NodeType.AGENT_NODE);
        assertNotNull(NodeType.MANUAL_NODE);
        assertNotNull(NodeType.CONDITION_NODE);
        assertNotNull(NodeType.END_NODE);
    }

    @Test
    void testTransition_HasAllFields() throws Exception {
        // 给定：一个跳转规则 YAML
        String yaml = """
            condition: "result.success == true && retryCount < 3"
            next: next-node-id
            action: "retryCount++"
            """;

        // 当：解析为 Transition
        Transition transition = objectMapper.readValue(yaml, Transition.class);

        // 那么：所有字段正确
        assertNotNull(transition);
        assertEquals("result.success == true && retryCount < 3", transition.getCondition());
        assertEquals("next-node-id", transition.getNext());
        assertEquals("retryCount++", transition.getAction());
    }

    @Test
    void testProcessDefinition_WithEmptyNodes() throws Exception {
        // 给定：一个没有节点的流程定义
        String yaml = """
            processId: empty-process
            name: 空流程
            version: "1.0"
            description: 没有节点的流程
            nodes: []
            """;

        // 当：解析 YAML
        ProcessDefinition definition = objectMapper.readValue(yaml, ProcessDefinition.class);

        // 那么：节点列表为空但不为 null
        assertNotNull(definition);
        assertNotNull(definition.getNodes());
        assertTrue(definition.getNodes().isEmpty());
    }

    @Test
    void testProcessNode_WithoutOptionalFields() throws Exception {
        // 给定：一个只有必需字段的节点
        String yaml = """
            nodeId: simple-node
            name: 简单节点
            nodeType: END_NODE
            """;

        // 当：解析为 ProcessNode
        ProcessNode node = objectMapper.readValue(yaml, ProcessNode.class);

        // 那么：必需字段正确，可选字段为默认值
        assertNotNull(node);
        assertEquals("simple-node", node.getNodeId());
        assertEquals("简单节点", node.getName());
        assertEquals(NodeType.END_NODE, node.getNodeType());
        // 可选字段
        assertNull(node.getPlugin());
        assertEquals(0, node.getTimeout());
        assertEquals(0, node.getMaxRetries());
        assertEquals(0, node.getRetryDelay());
        assertTrue(node.getTransitions() == null || node.getTransitions().isEmpty());
    }
}

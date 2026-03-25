package com.aidrclaw.core.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程实例测试
 */
class ProcessInstanceTest {

    private ProcessDefinition processDefinition;
    private ProcessInstance instance;

    @BeforeEach
    void setUp() {
        // 创建简单的测试流程定义
        processDefinition = new ProcessDefinition();
        processDefinition.setProcessId("test-process");
        processDefinition.setName("测试流程");
        processDefinition.setVersion("1.0");

        // 创建实例
        instance = new ProcessInstance("session-001", processDefinition);
    }

    @Test
    void testConstructor_HasAllFields() {
        // 验证构造函数初始化正确
        assertNotNull(instance);
        assertEquals("session-001", instance.getSessionId());
        assertEquals("test-process", instance.getProcessDefinition().getProcessId());
        assertEquals(ProcessInstance.ProcessState.RUNNING, instance.getState());
        assertNull(instance.getCurrentNodeId());
        assertNotNull(instance.getContext());
    }

    @Test
    void testGetCurrentNode() {
        // 初始状态没有当前节点
        assertNull(instance.getCurrentNode());

        // 设置当前节点后
        instance.setCurrentNodeId("node-1");
        ProcessNode currentNode = instance.getCurrentNode();
        assertNotNull(currentNode);
        assertEquals("node-1", currentNode.getNodeId());
    }

    @Test
    void testMarkNodeCompleted() {
        // 标记节点完成
        instance.markNodeCompleted("node-1");
        instance.markNodeCompleted("node-2");

        // 验证历史记录
        assertEquals(2, instance.getHistory().size());
        assertTrue(instance.getHistory().contains("node-1"));
        assertTrue(instance.getHistory().contains("node-2"));
    }

    @Test
    void testRetryCountManagement() {
        // 初始重试次数为 0
        assertEquals(0, instance.getRetryCount("node-1"));

        // 增加重试次数
        instance.incrementRetryCount("node-1");
        assertEquals(1, instance.getRetryCount("node-1"));

        instance.incrementRetryCount("node-1");
        assertEquals(2, instance.getRetryCount("node-1"));

        // 其他节点不受影响
        assertEquals(0, instance.getRetryCount("node-2"));
    }

    @Test
    void testStateTransition() {
        // 初始状态
        assertEquals(ProcessInstance.ProcessState.RUNNING, instance.getState());

        // 状态转换
        instance.setState(ProcessInstance.ProcessState.PAUSED);
        assertEquals(ProcessInstance.ProcessState.PAUSED, instance.getState());

        instance.setState(ProcessInstance.ProcessState.COMPLETED);
        assertEquals(ProcessInstance.ProcessState.COMPLETED, instance.getState());

        instance.setState(ProcessInstance.ProcessState.EXCEPTION);
        assertEquals(ProcessInstance.ProcessState.EXCEPTION, instance.getState());
    }

    @Test
    void testProcessExecutionContext() {
        ProcessExecutionContext context = instance.getContext();

        // 测试变量管理
        context.setVariable("customerName", "张三");
        context.setVariable("riskLevel", "R3");
        assertEquals("张三", context.getVariable("customerName"));
        assertEquals("R3", context.getVariable("riskLevel"));

        // 测试节点结果管理
        com.aidrclaw.plugin.PluginResult result = com.aidrclaw.plugin.PluginResult.success();
        context.setNodeResult("node-1", result);
        assertNotNull(context.getNodeResult("node-1"));
        assertTrue(context.getNodeResult("node-1").isSuccess());
    }

    @Test
    void testGetNextNode() {
        // 创建带节点的流程定义
        ProcessNode node1 = new ProcessNode();
        node1.setNodeId("node-1");
        node1.setName("节点 1");
        node1.setNodeType(NodeType.AI_NODE);
        node1.setPlugin("test-plugin");

        Transition transition = new Transition();
        transition.setCondition("result.success == true");
        transition.setNext("node-2");
        node1.setTransitions(java.util.List.of(transition));

        ProcessNode node2 = new ProcessNode();
        node2.setNodeId("node-2");
        node2.setName("节点 2");
        node2.setNodeType(NodeType.END_NODE);

        processDefinition.setNodes(java.util.List.of(node1, node2));
        instance = new ProcessInstance("session-002", processDefinition);

        // 获取下一节点
        instance.setCurrentNodeId("node-1");
        ProcessNode nextNode = instance.getNextNode("node-2");
        assertNotNull(nextNode);
        assertEquals("node-2", nextNode.getNodeId());
    }
}

package com.aidrclaw.agent.dialog;

import com.aidrclaw.agent.model.DialogNode;
import com.aidrclaw.agent.model.DialogTree;
import com.aidrclaw.agent.state.DialogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话树管理器
 * 
 * 负责加载对话树、管理会话对话状态
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Component
public class DialogTreeManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DialogTreeManager.class);
    
    /**
     * 对话树缓存
     */
    private final Map<String, DialogTree> dialogTreeCache;
    
    /**
     * 会话状态缓存
     */
    private final Map<String, DialogState> sessionStates;
    
    public DialogTreeManager() {
        this.dialogTreeCache = new ConcurrentHashMap<>();
        this.sessionStates = new ConcurrentHashMap<>();
    }
    
    /**
     * 加载对话树
     * 
     * @param treeId 对话树 ID
     * @return 对话树对象
     * @throws DialogNodeNotFoundException 对话树不存在时抛出
     */
    public DialogTree loadDialogTree(String treeId) throws DialogNodeNotFoundException {
        // 先查缓存
        if (dialogTreeCache.containsKey(treeId)) {
            logger.debug("Cache hit for dialog tree: {}", treeId);
            return dialogTreeCache.get(treeId);
        }
        
        // TODO: 从数据库/文件加载对话树
        // 这里暂时返回 null，后续实现数据库加载
        logger.warn("Dialog tree not found: {} (TODO: implement database loading)", treeId);
        throw new DialogNodeNotFoundException("Dialog tree not found: " + treeId);
    }
    
    /**
     * 缓存对话树
     */
    public void cacheDialogTree(DialogTree tree) {
        dialogTreeCache.put(tree.getId(), tree);
        logger.info("Cached dialog tree: {}", tree.getId());
    }
    
    /**
     * 获取节点
     */
    public DialogNode getNode(String treeId, String nodeId) throws DialogNodeNotFoundException {
        DialogTree tree = dialogTreeCache.get(treeId);
        if (tree == null) {
            throw new DialogNodeNotFoundException("Dialog tree not found: " + treeId);
        }
        
        DialogNode node = tree.getNode(nodeId);
        if (node == null) {
            throw new DialogNodeNotFoundException("Dialog node not found: " + nodeId);
        }
        
        return node;
    }
    
    /**
     * 获取当前会话状态
     */
    public DialogState getCurrentState(String sessionId) {
        return sessionStates.get(sessionId);
    }
    
    /**
     * 创建会话状态
     */
    public DialogState createState(String sessionId, String dialogTreeId, String currentNodeId) {
        DialogState state = DialogState.builder()
                .sessionId(sessionId)
                .dialogTreeId(dialogTreeId)
                .currentNodeId(currentNodeId)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sessionStates.put(sessionId, state);
        logger.info("Created dialog state for session: {} at node: {}", sessionId, currentNodeId);
        return state;
    }
    
    /**
     * 流转到下一节点
     */
    public void transitionTo(String sessionId, String nextNodeId) throws DialogNodeNotFoundException {
        DialogState state = sessionStates.get(sessionId);
        if (state == null) {
            throw new IllegalStateException("No dialog state for session: " + sessionId);
        }
        
        // 记录历史
        if (state.getCurrentNodeId() != null) {
            state.addToHistory(state.getCurrentNodeId());
        }
        
        // 更新当前节点
        state.setCurrentNodeId(nextNodeId);
        state.setNextNodeId(null);
        state.setTimestamp(System.currentTimeMillis());
        
        logger.info("Transitioned session {} to node {}", sessionId, nextNodeId);
    }
    
    /**
     * 获取对话树（从缓存）
     */
    public DialogTree getDialogTree(String treeId) {
        return dialogTreeCache.get(treeId);
    }
    
    /**
     * 清除会话状态
     */
    public void clearState(String sessionId) {
        sessionStates.remove(sessionId);
        logger.info("Cleared dialog state for session: {}", sessionId);
    }
    
    /**
     * 自定义异常：对话节点未找到
     */
    public static class DialogNodeNotFoundException extends Exception {
        public DialogNodeNotFoundException(String message) {
            super(message);
        }
    }
}

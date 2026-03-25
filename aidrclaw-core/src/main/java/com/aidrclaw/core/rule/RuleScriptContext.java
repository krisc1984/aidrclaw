package com.aidrclaw.core.rule;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * 规则脚本上下文
 * 
 * 为 JavaScript 脚本提供变量绑定
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class RuleScriptContext {
    
    /**
     * 脚本变量绑定
     */
    private final Bindings bindings;
    
    public RuleScriptContext() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        this.bindings = engine != null ? engine.createBindings() : new SimpleBindings();
    }
    
    /**
     * 设置变量绑定
     */
    public void setBinding(String name, Object value) {
        bindings.put(name, value);
    }
    
    /**
     * 获取变量绑定
     */
    public Object getBinding(String name) {
        return bindings.get(name);
    }
    
    /**
     * 获取所有绑定
     */
    public Map<String, Object> getBindings() {
        return bindings;
    }
    
    /**
     * 移除绑定
     */
    public void removeBinding(String name) {
        bindings.remove(name);
    }
    
    /**
     * 清除所有绑定
     */
    public void clear() {
        bindings.clear();
    }
}

package com.aidrclaw.core.event;

import org.springframework.context.ApplicationEvent;

/**
 * 双录系统领域事件基类
 */
public abstract class DomainEvent extends ApplicationEvent {

    public DomainEvent(Object source) {
        super(source);
    }
}

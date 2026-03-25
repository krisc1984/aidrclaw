package com.aidrclaw.quality.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 敏感词配置
 * 
 * 配置 sensitive-word 库的 Bean
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Configuration
public class SensitiveWordConfig {
    
    /**
     * 敏感词库 Bean
     * 
     * 使用 DFA 算法，支持快速匹配
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                .init();
    }
}

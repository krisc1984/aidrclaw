package com.aidrclaw.quality.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 质检配置
 * 
 * 配置异步执行线程池
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class QualityInspectionConfig {
    
    /**
     * 质检异步执行线程池
     */
    @Bean(name = "qualityExecutor")
    public Executor qualityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("quality-inspection-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

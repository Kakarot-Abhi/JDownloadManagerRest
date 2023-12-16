package com.project.jdownloadermanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

    @Autowired
    DownloaderConfig downloaderConfig;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(downloaderConfig.getPoolSize());
        executor.setMaxPoolSize(downloaderConfig.getMaxPoolSize());
        executor.setQueueCapacity(downloaderConfig.getQueueSize());
        executor.setThreadNamePrefix("download-thread-");
        executor.initialize();
        return executor;
    }
}

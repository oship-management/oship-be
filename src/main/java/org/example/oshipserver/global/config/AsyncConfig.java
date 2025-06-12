package org.example.oshipserver.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final int LOG_CORE_POOL_SIZE = 5;
    private static final int LOG_MAX_POOL_SIZE = 10;
    private static final int LOG_QUEUE_CAPACITY = 500;
    private static final String LOG_THREAD_NAME_PREFIX = "LogExecutor-";

    @Bean(name = "logTaskExecutor")
    public Executor getLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(LOG_CORE_POOL_SIZE);
        executor.setMaxPoolSize(LOG_MAX_POOL_SIZE);
        executor.setQueueCapacity(LOG_QUEUE_CAPACITY);
        executor.setThreadNamePrefix(LOG_THREAD_NAME_PREFIX);
        executor.initialize();
        return executor;
    }
}

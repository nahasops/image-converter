package com.nahasops.imageconverter.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.nahasops.imageconverter.exception.AsyncExceptionHandler;

@Configuration
@EnableAsync()
public class ImageConverterExecutor implements AsyncConfigurer {

 	@Value("${imageconverter.thread.core-pool}")
    private int corePoolSize;

    @Value("${imageconverter.thread.max-pool}")
    private int maxPoolSize;

    @Value("${imageconverter.thread.timeout}")
    private int threadTimeout;

    @Bean
    @Qualifier("imageConverterExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    	
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setKeepAliveSeconds(threadTimeout);
        threadPoolTaskExecutor.setThreadNamePrefix("imageconverter_task_executor_thread");

        return threadPoolTaskExecutor;
    }
    
    @Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new AsyncExceptionHandler();
	}
	
}
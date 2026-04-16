package com.hobart.lottery.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI 定时任务调度配置
 * 启用 Spring 的定时任务支持
 */
@Configuration
@EnableScheduling
public class AiSchedulingConfig {
}
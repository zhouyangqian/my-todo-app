package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * API 网关启动类
 * <p>
 * 基于 Spring Cloud Gateway 的微服务网关，负责请求路由、Token 验证、
 * 请求日志记录和全局异常处理。通过 Nacos 实现服务发现。
 * 支持健康检查、断路器、多维度限流等网关治理能力。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

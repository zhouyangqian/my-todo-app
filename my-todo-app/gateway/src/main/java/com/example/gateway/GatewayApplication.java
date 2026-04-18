package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关启动类
 * <p>
 * 基于 Spring Cloud Gateway 的微服务网关，负责请求路由、Token 验证、
 * 请求日志记录和全局异常处理。通过 Nacos 实现服务发现。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

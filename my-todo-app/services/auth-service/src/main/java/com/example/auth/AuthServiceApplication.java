package com.example.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证服务启动类
 * <p>
 * 负责用户登录、注册、令牌刷新、修改密码等认证相关功能。
 * 通过 Nacos 注册发现，Mapper 扫描 com.example.auth.mapper 包。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.auth.mapper")
@ComponentScan(basePackages = {"com.example.auth", "com.example.common"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

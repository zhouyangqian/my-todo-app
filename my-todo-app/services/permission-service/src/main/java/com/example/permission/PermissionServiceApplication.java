package com.example.permission;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 权限服务启动类
 * <p>
 * 该类是权限管理微服务的入口，负责以下核心功能：
 * 1. 用户权限管理（权限的增删改查、权限树构建）
 * 2. 角色管理（角色的增删改查、角色分配）
 * 3. 权限校验（判断用户是否拥有指定权限）
 * 4. 基于Redis的权限缓存机制
 * </p>
 * <p>
 * 使用 Spring Boot 自动配置、Nacos 服务发现注册、MyBatis-Plus Mapper 扫描
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.permission.mapper")
@ComponentScan(basePackages = {"com.example.permission", "com.example.common"})
public class PermissionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermissionServiceApplication.class, args);
    }
}

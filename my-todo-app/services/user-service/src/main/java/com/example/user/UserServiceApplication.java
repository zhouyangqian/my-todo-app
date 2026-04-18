package com.example.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 * <p>
 * 该类是用户服务（user-service）的入口程序，基于 Spring Boot 构建。
 * 主要职责包括：
 * <ul>
 *     <li>启用 Spring Boot 自动配置</li>
 *     <li>注册到服务发现中心（如 Nacos、Eureka），以供其他微服务调用</li>
 *     <li>自动扫描 MyBatis-Plus 的 Mapper 接口所在包，实现数据访问层的自动代理</li>
 * </ul>
 * </p>
 */
@SpringBootApplication   // 开启 Spring Boot 自动配置和组件扫描
@EnableDiscoveryClient  // 启用服务注册与发现，将本服务注册到注册中心
@MapperScan("com.example.user.mapper")  // 扫描指定包下的 MyBatis Mapper 接口，自动生成实现类
public class UserServiceApplication {

    /**
     * 应用程序主入口方法
     * <p>
     * 通过 SpringApplication.run 启动嵌入式的 Tomcat 容器，
     * 并初始化 Spring 应用上下文，加载所有配置和 Bean。
     * </p>
     *
     * @param args 命令行启动参数（可传入 --server.port=8081 等配置）
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

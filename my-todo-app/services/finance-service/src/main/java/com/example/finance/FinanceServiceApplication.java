package com.example.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 财务服务启动入口类
 * <p>
 * 基于 Spring Boot 构建的微服务应用，提供财务管理相关的全部后端能力。
 * 主要功能模块包括：应收账款管理、应付账款管理、收支记录管理、
 * 银行账户管理、发票管理等。
 * </p>
 * <p>
 * 核心注解说明：
 * <ul>
 *   <li>@SpringBootApplication - Spring Boot 自动配置与组件扫描</li>
 *   <li>@EnableDiscoveryClient - 启用服务注册与发现（Nacos/Eureka）</li>
 *   <li>@MapperScan - 自动扫描 MyBatis Mapper 接口所在包</li>
 *   <li>@EnableScheduling - 启用定时任务调度（如逾期账单提醒）</li>
 * </ul>
 * </p>
 *
 * @author finance-team
 * @since 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.finance.mapper")
@EnableScheduling
@ComponentScan(basePackages = {"com.example.finance", "com.example.common"})
public class FinanceServiceApplication {
    /**
     * 应用程序主入口方法
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceServiceApplication.class, args);
    }
}

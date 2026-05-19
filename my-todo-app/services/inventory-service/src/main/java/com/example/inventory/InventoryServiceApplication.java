package com.example.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 库存管理服务启动类
 * <p>
 * 提供库存查询、入库、出库、锁定/解锁、库存盘点、库存预警等功能。
 * 通过 Nacos 注册发现，Mapper 扫描 com.example.inventory.mapper 包。
 * 启用了定时任务调度（库存预警等）。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.inventory.mapper")
@EnableScheduling
@ComponentScan(basePackages = {"com.example.inventory", "com.example.common"})
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}

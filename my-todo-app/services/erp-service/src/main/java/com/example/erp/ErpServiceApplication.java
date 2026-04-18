package com.example.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ERP 进销存服务启动类
 * <p>
 * 提供商品管理、仓库管理、客户管理、供应商管理、库存管理、采购订单、销售订单等功能。
 * 通过 Nacos 注册发现，Mapper 扫描 com.example.erp.mapper 包。
 * 启用了定时任务调度（库存预警等）。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.erp.mapper")
@EnableScheduling
public class ErpServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpServiceApplication.class, args);
    }
}

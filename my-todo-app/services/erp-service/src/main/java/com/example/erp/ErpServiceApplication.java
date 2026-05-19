package com.example.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * ERP 进销存服务启动类
 * <p>
 * 提供商品管理、仓库管理、客户管理、供应商管理、采购订单、销售订单等功能。
 * 通过 Nacos 注册发现，Mapper 扫描 com.example.erp.mapper 包。
 * 启用了 Feign 客户端（用于调用财务服务和库存服务）。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.erp")
@MapperScan("com.example.erp.mapper")
@ComponentScan(basePackages = {"com.example.erp", "com.example.common"})
public class ErpServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpServiceApplication.class, args);
    }
}

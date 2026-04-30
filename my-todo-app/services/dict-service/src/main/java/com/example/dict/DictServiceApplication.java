package com.example.dict;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 字典服务启动类
 * <p>
 * 提供数据字典管理和系统参数配置功能。
 * 通过 Nacos 注册发现，Mapper 扫描 com.example.dict.mapper 包。
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.dict.mapper")
@ComponentScan(basePackages = {"com.example.dict", "com.example.common"})
public class DictServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DictServiceApplication.class, args);
    }
}

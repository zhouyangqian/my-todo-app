# Gateway

API 网关服务

## 端口
8080

## 主要功能
- JWT Token 验证
- 请求路由分发
- IP 限流 (Redis)
- 熔断降级
- 服务监控

## 启动
```bash
mvn spring-boot:run
```

依赖: Nacos, Redis

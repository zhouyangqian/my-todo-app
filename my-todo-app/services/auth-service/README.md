# Auth Service

用户认证与账号管理服务

## 端口
8081

## 数据库
my_todo_auth

## 主要功能
- 用户登录/登出
- Token 管理 (JWT + Redis)
- 租户注册
- 验证码
- SSE 实时通知

## 启动
```bash
mvn spring-boot:run
```

依赖: MySQL, Redis, Nacos

# Permission Service

权限与部门管理服务

## 端口
8083

## 数据库
my_todo_permission

## 主要功能
- 角色管理 (CRUD)
- 权限管理 (菜单、按钮、数据权限)
- 部门管理 (树形结构)
- 数据权限规则
- 角色权限分配

## 启动
```bash
mvn spring-boot:run
```

依赖: MySQL, Redis, Nacos

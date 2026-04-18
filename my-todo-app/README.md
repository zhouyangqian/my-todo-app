# My Todo App - SaaS 多租户企业管理系统

基于 Spring Cloud 微服务架构的综合企业管理系统。

## 技术栈

### 后端
- **Java 17+** (LTS)
- **Spring Boot 3.2.0**
- **Spring Cloud 2023.0.0**
- **Spring Cloud Alibaba 2023.0.0.0-RC1**
- **MyBatis-Plus 3.5.5**
- **MySQL 8.0.33**
- **Redis 7.0+**
- **Nacos**（服务发现与配置中心）
- **Spring Cloud Gateway**

### 前端
- **Vue 3** + TypeScript
- **Element Plus** UI 组件库
- **Vite** 构建工具
- **Pinia** 状态管理
- **ECharts** 图表

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      API 网关 (8080)                         │
│                   Token 验证 / 路由转发                       │
└─────────────────────────────────────────────────────────────┘
                              │
       ┌──────────────────────┼──────────────────────┐
       │                      │                      │
       ▼                      ▼                      ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  认证服务    │      │  用户服务    │      │  权限服务    │
│  (8081)     │      │  (8082)     │      │  (8083)     │
└─────────────┘      └─────────────┘      └─────────────┘
       │                      │                      │
       └──────────────────────┼──────────────────────┘
                              │
       ┌──────────────────────┼──────────────────────┐
       │                      │                      │
       ▼                      ▼                      ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│  字典服务    │      │ ERP进销存    │      │  财务服务    │
│  (8084)     │      │  (8085)     │      │  (8086)     │
└─────────────┘      └─────────────┘      └─────────────┘
```

## 模块说明

| 模块 | 端口 | 描述 |
|------|------|------|
| gateway | 8080 | API 网关，JWT 令牌验证与路由转发 |
| auth-service | 8081 | 认证与授权服务（登录、注册、Token 刷新） |
| user-service | 8082 | 用户管理服务（租户用户 CRUD） |
| permission-service | 8083 | 权限与角色管理服务 |
| dict-service | 8084 | 参数字典管理服务 |
| erp-service | 8085 | 进销存管理服务（商品、仓库、采购、销售） |
| finance-service | 8086 | 财务管理服务（应收应付、收支记录、发票） |

## 本地开发指南

### 一、环境准备

开发前请确保本地已安装以下软件：

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | 推荐 Eclipse Temurin 或 Oracle JDK |
| Maven | 3.8+ | 用于后端构建 |
| Docker | 20+ | 用于运行 MySQL、Redis、Nacos |
| Docker Compose | 2.0+ | 随 Docker Desktop 安装 |
| Node.js | 18+ | 前端开发（推荐 LTS 版本） |
| npm/pnpm | npm 9+ / pnpm 8+ | 前端包管理 |

验证环境：

```bash
java -version        # 确认 Java 17+
mvn -version         # 确认 Maven 3.8+
docker --version     # 确认 Docker
docker compose version  # 确认 Docker Compose
node -v              # 确认 Node.js 18+
```

### 二、启动基础设施（MySQL + Redis + Nacos）

在项目根目录执行：

```bash
# 启动 MySQL、Redis、Nacos
docker compose up -d mysql redis nacos

# 查看容器状态，等待所有服务 healthy
docker compose ps

# 如果需要查看日志
docker compose logs -f mysql
docker compose logs -f nacos
```

启动完成后确认以下服务可用：
- **MySQL**：`localhost:3306`，用户名 `root`，密码 `root`
- **Redis**：`localhost:6379`，无密码
- **Nacos 控制台**：http://localhost:8848/nacos ，账号/密码：`nacos/nacos`

> 注意：MySQL 首次启动时会自动执行 `init-scripts/01-init-databases.sql`，
> 创建所需的数据库（`my_todo_auth`、`my_todo_user`、`my_todo_permission`、`my_todo_dict`、`my_todo_erp`、`my_todo_finance`、`nacos`）。

### 三、后端构建与启动

#### 1. 编译整个项目

```bash
# 在项目根目录，首次构建（安装依赖 + 编译）
mvn clean install -DskipTests

# 后续开发中增量编译
mvn compile
```

> 构建顺序：先编译 `common/` 公共模块，再编译 `services/` 和 `gateway/`。
> 父 `pom.xml` 中的 `<modules>` 已配置好构建顺序，直接在根目录执行即可。

#### 2. 启动后端服务

每个微服务都是独立的 Spring Boot 应用，需要在不同的终端窗口中分别启动。

推荐的启动顺序（按依赖关系）：

```bash
# 终端 1：启动认证服务（必须最先启动，其他服务依赖其 Token 验证）
cd services/auth-service
mvn spring-boot:run

# 终端 2：启动用户服务
cd services/user-service
mvn spring-boot:run

# 终端 3：启动权限服务
cd services/permission-service
mvn spring-boot:run

# 终端 4：启动字典服务
cd services/dict-service
mvn spring-boot:run

# 终端 5：启动 ERP 进销存服务
cd services/erp-service
mvn spring-boot:run

# 终端 6：启动财务服务
cd services/finance-service
mvn spring-boot:run

# 终端 7：启动 API 网关（建议最后启动，等所有服务注册到 Nacos 后再启动）
cd gateway
mvn spring-boot:run
```

也可以在 IDE（IntelliJ IDEA）中直接运行各服务的 `*Application.java` 主类。

#### 3. 验证后端启动成功

各服务启动后，查看 Nacos 控制台 http://localhost:8848/nacos 的「服务列表」，
确认以下服务都已注册：

- `auth-service`
- `user-service`
- `permission-service`
- `dict-service`
- `erp-service`
- `finance-service`
- `gateway`

健康检查接口：

```bash
curl http://localhost:8081/actuator/health   # 认证服务
curl http://localhost:8082/actuator/health   # 用户服务
curl http://localhost:8083/actuator/health   # 权限服务
curl http://localhost:8084/actuator/health   # 字典服务
curl http://localhost:8085/actuator/health   # ERP 服务
curl http://localhost:8086/actuator/health   # 财务服务
curl http://localhost:8080/actuator/health   # 网关
```

### 四、前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次或依赖变更时）
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器启动后：
- 访问地址：http://localhost:3000
- API 请求自动代理到 `http://localhost:8080`（网关），配置在 `vite.config.ts` 中

### 五、本地开发配置说明

各服务的 `application.yml` 已配置好本地开发默认值，无需额外修改即可直接运行：

| 配置项 | 默认值 | 环境变量覆盖 |
|--------|--------|-------------|
| MySQL 地址 | `localhost:3306` | `MYSQL_HOST`、`MYSQL_PORT` |
| MySQL 用户名 | `root` | `MYSQL_USER` |
| MySQL 密码 | `root` | `MYSQL_PASSWORD` |
| Redis 地址 | `localhost:6379` | `REDIS_HOST`、`REDIS_PORT` |
| Nacos 地址 | `localhost:8848` | `NACOS_SERVER` |
| JWT 密钥 | 内置默认值 | `JWT_SECRET` |

如需修改配置，可以：
1. 直接修改各服务的 `src/main/resources/application.yml`
2. 通过环境变量覆盖（如 `export MYSQL_HOST=192.168.1.100`）
3. 在 Nacos 控制台中添加 `common.yaml` 共享配置

### 六、常见问题

**Q：Maven 构建失败，找不到 common 模块依赖？**

确保在项目根目录先执行 `mvn clean install -DskipTests`，将 common 模块安装到本地仓库。

**Q：服务启动报 "Unable to connect to Nacos"？**

1. 检查 Nacos 是否已启动：`docker compose ps nacos`
2. Nacos 首次启动较慢，等待约 30 秒后重试
3. 查看日志：`docker compose logs nacos`

**Q：服务启动报 "Access denied for user 'root'"？**

1. 检查 MySQL 是否已启动：`docker compose ps mysql`
2. 确认 MySQL 健康检查通过后再启动后端服务
3. 默认密码是 `root`，如需修改请设置环境变量 `MYSQL_PASSWORD`

**Q：前端 npm install 失败？**

1. 切换淘宝镜像：`npm config set registry https://registry.npmmirror.com`
2. 删除 `node_modules` 和 `package-lock.json` 后重试
3. 推荐使用 pnpm：`npm install -g pnpm && pnpm install`

**Q：端口被占用？**

各服务默认端口：网关 8080、认证 8081、用户 8082、权限 8083、字典 8084、ERP 8085、财务 8086、前端 3000。
修改对应服务 `application.yml` 中的 `server.port` 即可。

## API 文档

各服务均提供 Knife4j（OpenAPI 3）接口文档：

| 服务 | 文档地址 |
|------|---------|
| 认证服务 | http://localhost:8081/doc.html |
| 用户服务 | http://localhost:8082/doc.html |
| 权限服务 | http://localhost:8083/doc.html |
| 字典服务 | http://localhost:8084/doc.html |
| ERP 服务 | http://localhost:8085/doc.html |
| 财务服务 | http://localhost:8086/doc.html |
| API 网关 | http://localhost:8080/doc.html |

## 项目结构

```
my-todo-app/
├── common/                    # 公共模块
│   ├── common-core/          # 核心工具类
│   ├── common-redis/         # Redis 配置
│   ├── common-mybatis/       # MyBatis-Plus 配置
│   ├── common-security/      # 安全与 JWT
│   ├── common-web/           # Web 配置
│   └── common-feign/         # Feign 客户端工具
├── gateway/                   # API 网关
├── services/                  # 微服务
│   ├── auth-service/         # 认证服务
│   ├── user-service/         # 用户管理
│   ├── permission-service/   # 权限管理
│   ├── dict-service/         # 字典服务
│   ├── erp-service/          # ERP/进销存
│   └── finance-service/      # 财务管理
├── frontend/                  # 前端（Vue 3 + TypeScript）
│   ├── src/api/              # API 接口封装
│   ├── src/views/            # 页面组件
│   ├── src/router/           # 路由配置
│   ├── src/stores/           # 状态管理
│   ├── src/layouts/          # 布局组件
│   └── src/utils/            # 工具函数
├── init-scripts/              # 数据库初始化脚本
├── specs/                     # 功能规格文档
└── docker-compose.yml        # Docker 基础设施配置
```

## 功能特性

### 多租户 SaaS
- 数据库级别租户数据隔离
- JWT 令牌中携带租户上下文
- 通过 MyBatis 拦截器实现租户感知查询

### 安全机制
- 基于 JWT 的身份认证
- BCrypt 密码加密
- 登录尝试次数限制
- 账号锁定保护

### API 设计
- RESTful 风格接口
- 统一响应格式
- 全局异常处理
- OpenAPI 3 接口文档（Knife4j）

## 测试

```bash
# 后端单元测试
mvn test

# 后端集成测试
mvn verify

# 前端测试
cd frontend
npm run test

# 前端测试覆盖率
npm run test:coverage
```

## 代码规范
- 遵循 Google Java 编码规范
- 使用 Lombok 减少样板代码
- 使用 MapStruct 进行对象映射

## 许可证

MIT License

-- 网关路由配置表
CREATE TABLE IF NOT EXISTS gateway_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(100) NOT NULL COMMENT '路由ID',
    uri VARCHAR(255) NOT NULL COMMENT '目标URI',
    predicates TEXT COMMENT '断言配置JSON',
    filters TEXT COMMENT '过滤器配置JSON',
    priority INT DEFAULT 0 COMMENT '优先级',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    tenant_id BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 网关限流配置表
CREATE TABLE IF NOT EXISTS gateway_rate_limit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(100) COMMENT '路由ID(为空表示全局)',
    path_pattern VARCHAR(255) COMMENT '路径匹配模式',
    max_requests INT NOT NULL DEFAULT 100 COMMENT '最大请求数',
    time_window_seconds INT NOT NULL DEFAULT 60 COMMENT '时间窗口(秒)',
    dimension VARCHAR(20) DEFAULT 'IP' COMMENT '限流维度: IP/USER/TENANT',
    enabled TINYINT DEFAULT 1,
    tenant_id BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 网关熔断配置表
CREATE TABLE IF NOT EXISTS gateway_circuit_breaker (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id VARCHAR(100) NOT NULL COMMENT '服务ID',
    failure_threshold INT DEFAULT 5 COMMENT '失败阈值',
    cooldown_seconds INT DEFAULT 60 COMMENT '冷却时间(秒)',
    half_open_max_calls INT DEFAULT 3 COMMENT '半开状态最大调用数',
    enabled TINYINT DEFAULT 1,
    tenant_id BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

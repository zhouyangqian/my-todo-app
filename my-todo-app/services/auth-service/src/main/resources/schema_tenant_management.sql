CREATE TABLE IF NOT EXISTS sys_tenant_quota (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    max_users INT NOT NULL DEFAULT 5,
    max_storage_mb INT NOT NULL DEFAULT 1024,
    max_api_calls_per_day INT NOT NULL DEFAULT 10000,
    max_concurrent_requests INT NOT NULL DEFAULT 100,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_tenant_resource_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_count INT NOT NULL DEFAULT 0,
    storage_used_mb DECIMAL(10,2) NOT NULL DEFAULT 0,
    api_calls_today INT NOT NULL DEFAULT 0,
    concurrent_requests INT NOT NULL DEFAULT 0,
    record_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_tenant_date (tenant_id, record_date)
);

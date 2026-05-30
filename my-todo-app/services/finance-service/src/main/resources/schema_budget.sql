-- 预算管理表结构
-- 执行前提: 数据库 my_todo_finance 已存在

CREATE TABLE IF NOT EXISTS fin_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    budget_name VARCHAR(128) NOT NULL,
    budget_type VARCHAR(32) NOT NULL COMMENT 'DEPARTMENT/PROJECT/OVERALL',
    target_id BIGINT COMMENT '部门ID或项目ID',
    period_type VARCHAR(16) NOT NULL COMMENT 'MONTHLY/QUARTERLY/YEARLY',
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    budget_amount DECIMAL(18,4) NOT NULL,
    used_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
    frozen_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
    remaining_amount DECIMAL(18,4) NOT NULL DEFAULT 0,
    control_level VARCHAR(16) NOT NULL DEFAULT 'WARN' COMMENT 'FORCE/WARN/LOG',
    warning_threshold DECIMAL(5,2) DEFAULT 80.00 COMMENT '警告阈值(百分比)',
    status INT NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已审批 2=执行中 3=已结束',
    deleted INT NOT NULL DEFAULT 0,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_tenant_id (tenant_id),
    KEY idx_period (period_start, period_end)
);

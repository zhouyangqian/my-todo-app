-- 银行对账相关表结构
-- 执行前提: 数据库 my_todo_finance 已存在

CREATE TABLE IF NOT EXISTS fin_bank_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    bank_account_id BIGINT NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(18,4) NOT NULL,
    description VARCHAR(500),
    reference_no VARCHAR(64),
    transaction_type VARCHAR(32) COMMENT 'DEBIT/CREDIT',
    match_status INT NOT NULL DEFAULT 0 COMMENT '0=未匹配 1=自动匹配 2=手动匹配 3=异常',
    matched_record_id BIGINT COMMENT '匹配的系统记录ID',
    import_batch VARCHAR(64),
    deleted INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_tenant_account (tenant_id, bank_account_id),
    KEY idx_match_status (match_status)
);

CREATE TABLE IF NOT EXISTS fin_bank_reconciliation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    bank_account_id BIGINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status INT NOT NULL DEFAULT 0 COMMENT '0=进行中 1=已完成',
    total_bank_amount DECIMAL(18,4) DEFAULT 0,
    total_system_amount DECIMAL(18,4) DEFAULT 0,
    matched_count INT DEFAULT 0,
    unmatched_count INT DEFAULT 0,
    deleted INT NOT NULL DEFAULT 0,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

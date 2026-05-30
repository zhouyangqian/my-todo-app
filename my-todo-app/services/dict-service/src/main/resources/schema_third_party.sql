CREATE TABLE IF NOT EXISTS third_party_api (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    api_name VARCHAR(128) NOT NULL,
    api_url VARCHAR(512) NOT NULL,
    api_method VARCHAR(10) DEFAULT 'GET',
    auth_type VARCHAR(32) DEFAULT 'NONE' COMMENT 'NONE/API_KEY/OAUTH2/BASIC',
    api_key_header VARCHAR(64),
    api_key_value VARCHAR(256),
    description VARCHAR(500),
    timeout_ms INT DEFAULT 5000,
    retry_count INT DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    deleted INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS third_party_call_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    api_id BIGINT NOT NULL,
    request_time DATETIME NOT NULL,
    response_status INT,
    response_time_ms INT,
    error_message VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

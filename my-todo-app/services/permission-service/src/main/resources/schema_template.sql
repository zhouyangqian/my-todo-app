-- sys_permission_template 权限模板表
-- 用于存储权限模板，支持系统预设和自定义模板，可快速应用到角色

CREATE TABLE IF NOT EXISTS sys_permission_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '0=系统预设',
    template_name VARCHAR(128) NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    is_system INT NOT NULL DEFAULT 0 COMMENT '1=系统预设',
    permission_ids TEXT COMMENT '权限ID列表(JSON数组)',
    status INT NOT NULL DEFAULT 1,
    deleted INT NOT NULL DEFAULT 0,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_code (tenant_id, template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限模板表';

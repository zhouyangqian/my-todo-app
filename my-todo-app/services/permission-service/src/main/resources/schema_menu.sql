CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    parent_id BIGINT DEFAULT NULL COMMENT '父菜单ID',
    menu_name VARCHAR(128) NOT NULL,
    menu_type INT NOT NULL COMMENT '1=目录 2=菜单 3=按钮',
    path VARCHAR(256) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '前端组件路径',
    permission_code VARCHAR(128) COMMENT '权限编码',
    icon VARCHAR(64),
    sort_order INT NOT NULL DEFAULT 0,
    visible INT NOT NULL DEFAULT 1 COMMENT '0=隐藏 1=显示',
    status INT NOT NULL DEFAULT 1,
    deleted INT NOT NULL DEFAULT 0,
    created_by BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT DEFAULT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_parent_id (parent_id),
    KEY idx_tenant_id (tenant_id)
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
);

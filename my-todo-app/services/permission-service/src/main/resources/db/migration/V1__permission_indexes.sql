CREATE INDEX idx_permission_tenant ON sys_permission(tenant_id);
CREATE INDEX idx_role_permission_role ON sys_role_permission(role_id);
CREATE INDEX idx_data_permission_rule_tenant ON data_permission_rule(tenant_id);

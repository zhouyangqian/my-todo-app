CREATE INDEX idx_sys_user_tenant ON sys_user(tenant_id);
CREATE INDEX idx_sys_role_tenant ON sys_role(tenant_id);
CREATE INDEX idx_audit_log_tenant_time ON audit_log(tenant_id, created_at DESC);

CREATE INDEX idx_user_tenant_username ON sys_user(tenant_id, user_name);
CREATE INDEX idx_login_session_tenant ON login_session(tenant_id, user_id);
CREATE INDEX idx_refresh_token_tenant ON refresh_token(tenant_id, user_id);

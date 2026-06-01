-- 财务模块索引优化
-- 账单表索引
CREATE INDEX idx_bill_tenant_status ON fin_bill(tenant_id, status);
CREATE INDEX idx_bill_tenant_type ON fin_bill(tenant_id, bill_type);

-- 付款记录表索引
CREATE INDEX idx_payment_tenant_type ON payment_record(tenant_id, type);
CREATE INDEX idx_payment_tenant_date ON payment_record(tenant_id, payment_date);

-- 发票表索引
CREATE INDEX idx_invoice_tenant_status ON invoice(tenant_id, status);
CREATE INDEX idx_invoice_tenant_direction ON invoice(tenant_id, invoice_direction);

-- 财务报表索引
CREATE INDEX idx_report_tenant_type_period ON fin_report(tenant_id, report_type, report_period);

-- 预算表索引
CREATE INDEX idx_budget_tenant_period ON fin_budget(tenant_id, budget_period);

-- 应收应付索引
CREATE INDEX idx_ar_tenant_status ON account_receivable(tenant_id, status);
CREATE INDEX idx_ap_tenant_status ON account_payable(tenant_id, status);

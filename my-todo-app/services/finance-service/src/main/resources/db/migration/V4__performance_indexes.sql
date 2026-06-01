-- 性能优化：高流量表复合索引

-- 账单流水查询优化
CREATE INDEX idx_bill_tenant_date_status ON fin_bill(tenant_id, created_at DESC, status);

-- 付款记录按日期范围查询优化
CREATE INDEX idx_payment_tenant_date_range ON payment_record(tenant_id, payment_date DESC, type);

-- 应收账款账龄分析优化
CREATE INDEX idx_ar_tenant_due_date ON account_receivable(tenant_id, due_date, status);

-- 应付账款到期提醒优化
CREATE INDEX idx_ap_tenant_due_date ON account_payable(tenant_id, due_date, status);

-- 银行对账匹配查询优化
CREATE INDEX idx_bank_record_tenant_date ON fin_bank_record(tenant_id, transaction_date DESC);

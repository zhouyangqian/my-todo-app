-- 初始化数据

-- 默认成本计算方法
INSERT INTO fin_cost_config (tenant_id, cost_method, `description`, created_at, updated_at)
VALUES (0, 'WEIGHTED_AVERAGE', '加权平均法（系统默认）', NOW(), NOW())
ON DUPLICATE KEY UPDATE cost_method = cost_method;

-- 默认银行账户类型配置（通过字典管理，此处仅作记录）
-- 银行账户类型：1-基本户 2-一般户 3-专用户 4-临时户

-- 默认预算类别
INSERT INTO fin_budget (tenant_id, budget_name, budget_period, total_amount, used_amount, status, created_at, updated_at)
VALUES (0, '默认年度预算模板', 'YEARLY', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE budget_name = budget_name;

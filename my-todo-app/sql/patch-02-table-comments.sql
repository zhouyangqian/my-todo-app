-- ============================================================
-- Patch 02: 为缺失表注释的表添加 COMMENT
-- 问题: sql/my-todo-app-dev.sql 中 90 张表全部缺少表级 COMMENT=
-- 来源: db-scripts/*.sql + services/*/schema*.sql
-- ============================================================

USE `my-todo-app-dev`;

-- ===================== Auth 模块 (6 表) =====================
ALTER TABLE `login_session`       COMMENT '登录会话表';
ALTER TABLE `refresh_token`       COMMENT '刷新令牌表';
ALTER TABLE `login_log`           COMMENT '登录日志表';
ALTER TABLE `captcha`             COMMENT '验证码表';
ALTER TABLE `password_history`    COMMENT '密码历史表';
ALTER TABLE `social_account`      COMMENT '社交账号绑定表';

-- ===================== User 模块 (4 表) =====================
ALTER TABLE `sys_user`            COMMENT '用户表';
ALTER TABLE `sys_user_profile`    COMMENT '用户详情表';
ALTER TABLE `sys_user_address`    COMMENT '用户地址表';
ALTER TABLE `sys_user_role`       COMMENT '用户角色关联表';

-- ===================== Permission 模块 (6 表) =====================
ALTER TABLE `sys_role`            COMMENT '角色表';
ALTER TABLE `sys_permission`      COMMENT '权限表';
ALTER TABLE `sys_role_permission` COMMENT '角色权限关联表';
ALTER TABLE `sys_department`      COMMENT '部门表';
ALTER TABLE `sys_menu`            COMMENT '菜单管理表';
ALTER TABLE `sys_role_menu`       COMMENT '角色菜单关联表';

-- ===================== Dict 模块 (3+16 表) =====================
ALTER TABLE `sys_dict_type`       COMMENT '字典类型表';
ALTER TABLE `sys_dict_item`       COMMENT '字典项表';
ALTER TABLE `sys_config`          COMMENT '系统配置表';

-- 参数字典
ALTER TABLE `sys_parameter_category`   COMMENT '参数分类表';
ALTER TABLE `sys_parameter_dictionary` COMMENT '参数字典表';
ALTER TABLE `sys_parameter_item`       COMMENT '参数项表';

-- API 市场
ALTER TABLE `api_definition`      COMMENT 'API定义表';
ALTER TABLE `api_subscription`    COMMENT 'API订阅表';
ALTER TABLE `api_usage_record`    COMMENT 'API使用记录表';

-- 第三方API
ALTER TABLE `third_party_api`     COMMENT '第三方API配置表';
ALTER TABLE `third_party_call_log` COMMENT '第三方API调用日志表';

-- 链路追踪
ALTER TABLE `trace_config`        COMMENT '链路追踪配置表';
ALTER TABLE `trace_alert`         COMMENT '链路追踪告警表';

-- 代码生成
ALTER TABLE `code_template`       COMMENT '代码模板表';
ALTER TABLE `gen_history`         COMMENT '代码生成历史表';

-- 错误文档
ALTER TABLE `error_category`      COMMENT '错误分类表';
ALTER TABLE `error_solution`      COMMENT '错误解决方案表';

-- SaaS 套餐
ALTER TABLE `saas_package`        COMMENT 'SaaS套餐表';
ALTER TABLE `tenant_subscription` COMMENT '租户订阅表';
ALTER TABLE `marketing_activity`  COMMENT '营销活动表';
ALTER TABLE `activity_participation` COMMENT '活动参与记录表';

-- ===================== Auth 扩展 (5 表) =====================
ALTER TABLE `sys_tenant`          COMMENT '租户信息表';
ALTER TABLE `sys_token_blacklist` COMMENT 'Token黑名单表';
ALTER TABLE `sys_captcha`         COMMENT '验证码表(新)';
ALTER TABLE `sys_tenant_quota`    COMMENT '租户配额表';
ALTER TABLE `sys_tenant_resource_usage` COMMENT '租户资源使用表';

-- ===================== Permission 扩展 (6 表) =====================
ALTER TABLE `sys_user_permission`     COMMENT '用户权限关联表';
ALTER TABLE `sys_audit_log`           COMMENT '审计日志表';
ALTER TABLE `sys_permission_log`      COMMENT '权限审计日志表';
ALTER TABLE `sys_session`             COMMENT '会话管理表';
ALTER TABLE `sys_blacklist`           COMMENT '黑名单管理表';
ALTER TABLE `sys_permission_template` COMMENT '权限模板表';
ALTER TABLE `sys_data_permission_rule` COMMENT '数据权限规则表';
ALTER TABLE `sys_role_inheritance`    COMMENT '角色继承关系表';

-- ===================== ERP 模块 (32 表) =====================
ALTER TABLE `erp_product_category`     COMMENT '商品分类表';
ALTER TABLE `erp_product`              COMMENT '商品表';
ALTER TABLE `erp_warehouse`            COMMENT '仓库表';
ALTER TABLE `erp_inventory`            COMMENT '库存表';
ALTER TABLE `erp_supplier`             COMMENT '供应商表';
ALTER TABLE `erp_customer`             COMMENT '客户表';
ALTER TABLE `erp_purchase_order`       COMMENT '采购订单表';
ALTER TABLE `erp_purchase_order_item`  COMMENT '采购订单明细表';
ALTER TABLE `erp_purchase_return`      COMMENT '采购退货表';
ALTER TABLE `erp_purchase_return_item` COMMENT '采购退货明细表';
ALTER TABLE `erp_sales_order`          COMMENT '销售订单表';
ALTER TABLE `erp_sales_order_item`     COMMENT '销售订单明细表';
ALTER TABLE `erp_sales_quotation`      COMMENT '销售报价单主表';
ALTER TABLE `erp_sales_quotation_item` COMMENT '销售报价单明细表';
ALTER TABLE `erp_sales_return`         COMMENT '销售退货表';
ALTER TABLE `erp_sales_return_item`    COMMENT '销售退货明细表';
ALTER TABLE `erp_sales_shipment`       COMMENT '销售出库单表';
ALTER TABLE `erp_sales_shipment_item`  COMMENT '销售出库单明细表';
ALTER TABLE `erp_inventory_flow`       COMMENT '库存流水表';
ALTER TABLE `erp_inventory_check`      COMMENT '库存盘点表';
ALTER TABLE `erp_inventory_check_item` COMMENT '库存盘点明细表';
ALTER TABLE `erp_product_price`        COMMENT '商品价格表';
ALTER TABLE `erp_product_promotion`    COMMENT '商品促销表';
ALTER TABLE `erp_config`               COMMENT 'ERP系统配置表';
ALTER TABLE `supplier_contact`         COMMENT '供应商联系人表';
ALTER TABLE `customer_contact`         COMMENT '客户联系人表';

-- ===================== Finance 模块 (14 表) =====================
ALTER TABLE `fin_bank_account`       COMMENT '银行账户表';
ALTER TABLE `fin_account_receivable` COMMENT '应收账款表';
ALTER TABLE `fin_account_payable`    COMMENT '应付账款表';
ALTER TABLE `fin_payment_record`     COMMENT '收支记录表';
ALTER TABLE `fin_invoice`            COMMENT '发票表';
ALTER TABLE `fin_cost_config`        COMMENT '成本核算配置表';
ALTER TABLE `fin_cost_history`       COMMENT '成本核算历史表';
ALTER TABLE `fin_profit_calc`        COMMENT '毛利计算表';
ALTER TABLE `fin_report`             COMMENT '财务报表表';
ALTER TABLE `fin_bank_record`        COMMENT '银行对账单记录表';
ALTER TABLE `fin_bank_reconciliation` COMMENT '银行对账记录表';
ALTER TABLE `fin_bill`               COMMENT '统一账单表';
ALTER TABLE `fin_bill_invoice`       COMMENT '账单-发票关联表';
ALTER TABLE `fin_budget`             COMMENT '预算管理表';

-- ===================== Gateway 模块 (3 表) =====================
ALTER TABLE `gateway_route`           COMMENT '网关路由配置表';
ALTER TABLE `gateway_rate_limit`      COMMENT '网关限流配置表';
ALTER TABLE `gateway_circuit_breaker` COMMENT '网关熔断配置表';

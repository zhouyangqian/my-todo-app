/*
 Navicat Premium Data Transfer

 Source Server         : my-todo-app-dev
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : my-todo-app-dev

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 01/06/2026 18:51:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_participation
-- ----------------------------
DROP TABLE IF EXISTS `activity_participation`;
CREATE TABLE `activity_participation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `status` int NOT NULL DEFAULT 1 COMMENT '0=已取消 1=已参与',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_tenant`(`activity_id` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动参与记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_participation
-- ----------------------------

-- ----------------------------
-- Table structure for api_definition
-- ----------------------------
DROP TABLE IF EXISTS `api_definition`;
CREATE TABLE `api_definition`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `api_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GET',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `version` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'v1',
  `status` int NOT NULL DEFAULT 1 COMMENT '0=下线 1=上线 2=审核中',
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'API定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of api_definition
-- ----------------------------

-- ----------------------------
-- Table structure for api_subscription
-- ----------------------------
DROP TABLE IF EXISTS `api_subscription`;
CREATE TABLE `api_subscription`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `api_id` bigint NOT NULL,
  `subscriber_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `call_limit` int NOT NULL DEFAULT 1000,
  `call_count` int NOT NULL DEFAULT 0,
  `status` int NOT NULL DEFAULT 1,
  `expires_at` datetime NULL DEFAULT NULL,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'API订阅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of api_subscription
-- ----------------------------

-- ----------------------------
-- Table structure for api_usage_record
-- ----------------------------
DROP TABLE IF EXISTS `api_usage_record`;
CREATE TABLE `api_usage_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `api_id` bigint NOT NULL,
  `subscription_id` bigint NOT NULL,
  `request_time` datetime NOT NULL,
  `response_status` int NULL DEFAULT NULL,
  `response_time_ms` int NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'API使用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of api_usage_record
-- ----------------------------

-- ----------------------------
-- Table structure for captcha
-- ----------------------------
DROP TABLE IF EXISTS `captcha`;
CREATE TABLE `captcha`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `captcha_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码键',
  `captcha_value` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码值',
  `captcha_type` tinyint NOT NULL DEFAULT 1 COMMENT '类型: 1-图片验证码, 2-短信验证码, 3-邮件验证码',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `used` tinyint NOT NULL DEFAULT 0 COMMENT '已使用: 0-未使用, 1-已使用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_captcha_key`(`captcha_key` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '验证码表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of captcha
-- ----------------------------

-- ----------------------------
-- Table structure for code_template
-- ----------------------------
DROP TABLE IF EXISTS `code_template`;
CREATE TABLE `code_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `template_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ENTITY/MAPPER/SERVICE/CONTROLLER/VUE',
  `template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代码模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of code_template
-- ----------------------------

-- ----------------------------
-- Table structure for customer_contact
-- ----------------------------
DROP TABLE IF EXISTS `customer_contact`;
CREATE TABLE `customer_contact`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID（主键，自增）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人姓名',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认联系人：0-否，1-是',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_customer_contact_customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `idx_customer_contact_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of customer_contact
-- ----------------------------

-- ----------------------------
-- Table structure for erp_config
-- ----------------------------
DROP TABLE IF EXISTS `erp_config`;
CREATE TABLE `erp_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置值',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置类型: APPROVAL-审批规则, NUMBER-编号规则, BIZ-业务参数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key_tenant`(`config_key` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_config_type`(`config_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_config
-- ----------------------------

-- ----------------------------
-- Table structure for erp_customer
-- ----------------------------
DROP TABLE IF EXISTS `erp_customer`;
CREATE TABLE `erp_customer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `customer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户编码',
  `customer_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称',
  `customer_type` tinyint NOT NULL DEFAULT 1 COMMENT '客户类型: 1-企业, 2-个人',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '银行账号',
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '税号',
  `credit_limit` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '信用额度',
  `current_debt` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '当前欠款，客户当前未结算的销售金额',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `settlement_type` tinyint NOT NULL DEFAULT 1 COMMENT '结算方式: 1-现结, 2-月结, 3-账期',
  `credit_days` int NULL DEFAULT 30 COMMENT '账期天数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_customer_code_tenant`(`customer_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_customer
-- ----------------------------

-- ----------------------------
-- Table structure for erp_inventory
-- ----------------------------
DROP TABLE IF EXISTS `erp_inventory`;
CREATE TABLE `erp_inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批次号',
  `quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '库存数量',
  `locked_quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '锁定数量',
  `available_quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '可用数量',
  `stock_min` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '库存下限',
  `stock_max` decimal(18, 4) NULL DEFAULT 99999999.0000 COMMENT '库存上限',
  `cost_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '成本价',
  `production_date` datetime NULL DEFAULT NULL COMMENT '生产日期',
  `expiry_date` datetime NULL DEFAULT NULL COMMENT '过期日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_product`(`warehouse_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_inventory
-- ----------------------------

-- ----------------------------
-- Table structure for erp_inventory_check
-- ----------------------------
DROP TABLE IF EXISTS `erp_inventory_check`;
CREATE TABLE `erp_inventory_check`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `check_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '盘点单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `check_date` datetime NOT NULL COMMENT '盘点日期',
  `check_type` tinyint NOT NULL DEFAULT 1 COMMENT '盘点类型: 1-全盘, 2-抽盘',
  `check_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-盘点中, 2-已完成, 3-已取消',
  `total_profit_qty` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '盘盈数量合计',
  `total_loss_qty` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '盘亏数量合计',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_check_no_tenant`(`check_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存盘点表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_inventory_check
-- ----------------------------

-- ----------------------------
-- Table structure for erp_inventory_check_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_inventory_check_item`;
CREATE TABLE `erp_inventory_check_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `check_id` bigint NOT NULL COMMENT '盘点单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `system_quantity` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '系统数量',
  `actual_quantity` decimal(18, 4) NULL DEFAULT NULL COMMENT '实盘数量',
  `diff_quantity` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '差异数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_check_id`(`check_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存盘点明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_inventory_check_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_inventory_flow
-- ----------------------------
DROP TABLE IF EXISTS `erp_inventory_flow`;
CREATE TABLE `erp_inventory_flow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `biz_type` tinyint NOT NULL COMMENT '业务类型: 1-采购入库, 2-销售出库, 3-调拨入库, 4-调拨出库, 5-盘盈, 6-盘亏, 7-退货入库, 8-退货出库',
  `biz_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务单号',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '业务ID',
  `quantity` decimal(18, 4) NOT NULL COMMENT '变动数量(正数入库,负数出库)',
  `before_quantity` decimal(18, 4) NOT NULL COMMENT '变动前数量',
  `after_quantity` decimal(18, 4) NOT NULL COMMENT '变动后数量',
  `cost_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '成本价',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批次号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_warehouse_product`(`warehouse_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_biz_no`(`biz_no` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存流水表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_inventory_flow
-- ----------------------------

-- ----------------------------
-- Table structure for erp_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '型号',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `barcode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '条码',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `cost_price` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '成本价',
  `sale_price` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '销售价',
  `min_price` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '最低售价',
  `stock_quantity` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '库存数量',
  `stock_min` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '库存预警下限',
  `stock_max` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '库存预警上限',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品图片',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_code_tenant`(`product_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_product
-- ----------------------------
INSERT INTO `erp_product` VALUES (1, 1, 'P202604190001', '纯净水', NULL, NULL, NULL, NULL, NULL, '件', 1.0000, 2.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, NULL, '', 0, 1, '2026-04-19 14:02:37', NULL, '2026-04-19 14:02:37');
INSERT INTO `erp_product` VALUES (2, 1, 'P202604190002', '可乐', '可口可乐', '300毫升', NULL, NULL, NULL, '个', 2.0000, 3.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, NULL, '', 0, 1, '2026-04-19 14:35:34', NULL, '2026-04-19 14:35:34');

-- ----------------------------
-- Table structure for erp_product_category
-- ----------------------------
DROP TABLE IF EXISTS `erp_product_category`;
CREATE TABLE `erp_product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID，顶级分类为0',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code_tenant`(`category_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_product_category
-- ----------------------------

-- ----------------------------
-- Table structure for erp_product_price
-- ----------------------------
DROP TABLE IF EXISTS `erp_product_price`;
CREATE TABLE `erp_product_price`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '价格ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `price_type` tinyint NOT NULL COMMENT '价格类型: 1-成本价, 2-销售价, 3-批发价, 4-会员价, 5-促销价',
  `price` decimal(18, 4) NOT NULL COMMENT '价格',
  `min_quantity` decimal(18, 4) NULL DEFAULT 1.0000 COMMENT '最小数量（阶梯价）',
  `start_date` datetime NULL DEFAULT NULL COMMENT '生效日期',
  `end_date` datetime NULL DEFAULT NULL COMMENT '失效日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_price_type`(`price_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品价格表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_product_price
-- ----------------------------

-- ----------------------------
-- Table structure for erp_product_promotion
-- ----------------------------
DROP TABLE IF EXISTS `erp_product_promotion`;
CREATE TABLE `erp_product_promotion`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '促销ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `promotion_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '促销名称',
  `promotion_type` tinyint NOT NULL COMMENT '促销类型: 1-限时折扣, 2-满减, 3-买赠',
  `product_id` bigint NULL DEFAULT NULL COMMENT '关联商品ID（NULL表示全场活动）',
  `discount_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '折扣率（限时折扣用，如0.80表示8折）',
  `min_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '满减门槛金额',
  `reduce_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '满减优惠金额',
  `gift_product_id` bigint NULL DEFAULT NULL COMMENT '赠品商品ID（买赠用）',
  `gift_quantity` int NULL DEFAULT NULL COMMENT '赠品数量（买赠用）',
  `start_date` datetime NOT NULL COMMENT '促销开始时间',
  `end_date` datetime NOT NULL COMMENT '促销结束时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-未开始, 1-进行中, 2-已结束, 3-已停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_promotion_type`(`promotion_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_end_date`(`start_date` ASC, `end_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品促销表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_product_promotion
-- ----------------------------

-- ----------------------------
-- Table structure for erp_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_order`;
CREATE TABLE `erp_purchase_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `order_date` datetime NOT NULL COMMENT '订单日期',
  `expected_date` datetime NULL DEFAULT NULL COMMENT '预计到货日期',
  `total_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '订单金额',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `paid_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '实付金额',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态: 0-草稿, 1-待审核, 2-已审核, 3-已入库, 4-已完成, 5-已取消',
  `approved_by` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `approved_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '经手人ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no_tenant`(`order_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_purchase_order
-- ----------------------------

-- ----------------------------
-- Table structure for erp_purchase_order_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_order_item`;
CREATE TABLE `erp_purchase_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(18, 4) NOT NULL COMMENT '订单数量',
  `price` decimal(18, 4) NOT NULL COMMENT '单价',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '行折扣金额',
  `amount` decimal(18, 2) NOT NULL COMMENT '行金额',
  `received_quantity` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '已入库数量',
  `received_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '已入库金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购订单明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_purchase_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_purchase_return
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_return`;
CREATE TABLE `erp_purchase_return`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退货单ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `return_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退货单号',
  `order_id` bigint NULL DEFAULT NULL COMMENT '原采购订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原订单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `return_date` datetime NOT NULL COMMENT '退货日期',
  `total_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '退货金额',
  `return_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审核, 2-已审核, 3-已取消',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_return_no_tenant`(`return_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购退货表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_purchase_return
-- ----------------------------

-- ----------------------------
-- Table structure for erp_purchase_return_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_return_item`;
CREATE TABLE `erp_purchase_return_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `return_id` bigint NOT NULL COMMENT '退货单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `quantity` decimal(18, 4) NOT NULL COMMENT '退货数量',
  `price` decimal(18, 4) NOT NULL COMMENT '单价',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_return_id`(`return_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购退货明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_purchase_return_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_order
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_order`;
CREATE TABLE `erp_sales_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `order_date` datetime NOT NULL COMMENT '订单日期',
  `expected_date` datetime NULL DEFAULT NULL COMMENT '预计发货日期',
  `total_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '订单金额',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `received_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '实收金额',
  `delivered_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '已发货金额',
  `delivered_quantity` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '已发货数量(汇总)',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态: 0-草稿, 1-待审核, 2-已审核, 3-已出库, 4-已完成, 5-已取消',
  `approved_by` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `approved_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `sales_id` bigint NULL DEFAULT NULL COMMENT '销售员ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no_tenant`(`order_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '销售订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sales_order
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_order_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_order_item`;
CREATE TABLE `erp_sales_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(18, 4) NOT NULL COMMENT '订单数量',
  `price` decimal(18, 4) NOT NULL COMMENT '单价',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '行折扣金额',
  `amount` decimal(18, 2) NOT NULL COMMENT '行金额',
  `delivered_quantity` decimal(18, 4) NULL DEFAULT 0.0000 COMMENT '已发货数量',
  `delivered_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '已发货金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '销售订单明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sales_order_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_quotation
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_quotation`;
CREATE TABLE `erp_sales_quotation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报价单ID（主键，自增）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID，用于多租户数据隔离',
  `quotation_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报价单编号，同一租户下唯一',
  `customer_id` bigint NOT NULL COMMENT '客户ID，关联 erp_customer 表',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户名称，冗余存储便于查询显示',
  `quotation_date` date NULL DEFAULT NULL COMMENT '报价日期',
  `valid_until` date NULL DEFAULT NULL COMMENT '有效期至',
  `total_amount` decimal(16, 2) NULL DEFAULT 0.00 COMMENT '总金额',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0-草稿, 1-已发送, 2-已接受, 3-已拒绝, 4-已过期, 5-已转订单',
  `converted_order_id` bigint NULL DEFAULT NULL COMMENT '转订单ID，关联 erp_sales_order 表',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除, 1-已删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quotation_no_tenant`(`quotation_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_quotation_date`(`quotation_date` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '销售报价单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of erp_sales_quotation
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_quotation_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_quotation_item`;
CREATE TABLE `erp_sales_quotation_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键，自增）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID，用于多租户数据隔离',
  `quotation_id` bigint NOT NULL COMMENT '报价单ID，关联 erp_sales_quotation 表',
  `product_id` bigint NOT NULL COMMENT '商品ID，关联 erp_product 表',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品编码',
  `quantity` decimal(16, 4) NOT NULL COMMENT '报价数量',
  `unit_price` decimal(16, 2) NOT NULL COMMENT '单价',
  `discount_rate` decimal(5, 2) NULL DEFAULT 100.00 COMMENT '折扣率（百分比，默认100表示无折扣）',
  `amount` decimal(16, 2) NULL DEFAULT 0.00 COMMENT '行金额 = 数量 * 单价 * 折扣率 / 100',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除, 1-已删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_quotation_id`(`quotation_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '销售报价单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of erp_sales_quotation_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_return
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_return`;
CREATE TABLE `erp_sales_return`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退货单ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `return_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退货单号',
  `order_id` bigint NULL DEFAULT NULL COMMENT '原销售订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原订单编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `return_date` datetime NOT NULL COMMENT '退货日期',
  `total_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '退货金额',
  `return_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审核, 2-已审核, 3-已取消',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_return_no_tenant`(`return_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '销售退货表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sales_return
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_return_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_return_item`;
CREATE TABLE `erp_sales_return_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `return_id` bigint NOT NULL COMMENT '退货单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `quantity` decimal(18, 4) NOT NULL COMMENT '退货数量',
  `price` decimal(18, 4) NOT NULL COMMENT '单价',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_return_id`(`return_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '销售退货明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sales_return_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_shipment
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_shipment`;
CREATE TABLE `erp_sales_shipment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `shipment_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出库单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `shipment_date` datetime NOT NULL COMMENT '出库日期',
  `total_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '出库总金额',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `received_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '实收金额',
  `shipment_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审核, 2-已出库, 3-已取消',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shipment_no_tenant`(`shipment_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '销售出库单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sales_shipment
-- ----------------------------

-- ----------------------------
-- Table structure for erp_sales_shipment_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_sales_shipment_item`;
CREATE TABLE `erp_sales_shipment_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `shipment_id` bigint NOT NULL COMMENT '出库单ID',
  `order_item_id` bigint NOT NULL COMMENT '订单明细ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(18, 4) NOT NULL COMMENT '出库数量',
  `price` decimal(18, 4) NOT NULL COMMENT '单价',
  `discount_amount` decimal(18, 2) NULL DEFAULT 0.00 COMMENT '折扣金额',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_shipment_id`(`shipment_id` ASC) USING BTREE,
  INDEX `idx_order_item_id`(`order_item_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '销售出库单明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_sales_shipment_item
-- ----------------------------

-- ----------------------------
-- Table structure for erp_supplier
-- ----------------------------
DROP TABLE IF EXISTS `erp_supplier`;
CREATE TABLE `erp_supplier`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开户银行',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '银行账号',
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '税号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `settlement_type` tinyint NOT NULL DEFAULT 1 COMMENT '结算方式: 1-现结, 2-月结, 3-账期',
  `credit_days` int NULL DEFAULT 30 COMMENT '账期天数',
  `credit_limit` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '信用额度，供应商可赊账的最大金额',
  `current_debt` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '当前欠款，供应商当前未结算的采购金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_code_tenant`(`supplier_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '供应商表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_supplier
-- ----------------------------

-- ----------------------------
-- Table structure for erp_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `erp_warehouse`;
CREATE TABLE `erp_warehouse`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `warehouse_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `warehouse_type` tinyint NOT NULL DEFAULT 1 COMMENT '仓库类型: 1-普通仓, 2-门店仓, 3-虚拟仓',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '负责人ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认仓库',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_code_tenant`(`warehouse_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of erp_warehouse
-- ----------------------------
INSERT INTO `erp_warehouse` VALUES (1, 1, 'WH001', '主仓库', 1, NULL, NULL, NULL, 1, 1, NULL, 0, NULL, '2026-04-18 19:41:53', NULL, '2026-04-18 19:41:53');

-- ----------------------------
-- Table structure for error_category
-- ----------------------------
DROP TABLE IF EXISTS `error_category`;
CREATE TABLE `error_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `category_code`(`category_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '错误分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of error_category
-- ----------------------------

-- ----------------------------
-- Table structure for error_solution
-- ----------------------------
DROP TABLE IF EXISTS `error_solution`;
CREATE TABLE `error_solution`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL,
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cause_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '错误解决方案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of error_solution
-- ----------------------------

-- ----------------------------
-- Table structure for fin_account_payable
-- ----------------------------
DROP TABLE IF EXISTS `fin_account_payable`;
CREATE TABLE `fin_account_payable`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '应付ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `biz_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `amount` decimal(18, 2) NOT NULL COMMENT '应付金额',
  `paid_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '已付金额',
  `unpaid_amount` decimal(18, 2) NOT NULL COMMENT '未付金额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `biz_date` datetime NOT NULL COMMENT '业务日期',
  `due_date` datetime NULL DEFAULT NULL COMMENT '应付日期',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-未结算, 1-部分结算, 2-已结算',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_biz_no`(`biz_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '应付账款表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_account_payable
-- ----------------------------

-- ----------------------------
-- Table structure for fin_account_receivable
-- ----------------------------
DROP TABLE IF EXISTS `fin_account_receivable`;
CREATE TABLE `fin_account_receivable`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '应收ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `biz_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `amount` decimal(18, 2) NOT NULL COMMENT '应收金额',
  `received_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '已收金额',
  `unreceived_amount` decimal(18, 2) NOT NULL COMMENT '未收金额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `biz_date` datetime NOT NULL COMMENT '业务日期',
  `due_date` datetime NULL DEFAULT NULL COMMENT '应收日期',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-未结算, 1-部分结算, 2-已结算',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_customer_id`(`customer_id` ASC) USING BTREE,
  INDEX `idx_biz_no`(`biz_no` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '应收账款表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_account_receivable
-- ----------------------------

-- ----------------------------
-- Table structure for fin_bank_account
-- ----------------------------
DROP TABLE IF EXISTS `fin_bank_account`;
CREATE TABLE `fin_bank_account`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `account_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账户编码',
  `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账户名称',
  `account_type` tinyint NOT NULL DEFAULT 1 COMMENT '账户类型: 1-现金账户, 2-银行账户, 3-支付宝, 4-微信',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开户银行',
  `bank_account_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '银行账号',
  `balance` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认账户',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_account_code_tenant`(`account_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '银行账户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_bank_account
-- ----------------------------
INSERT INTO `fin_bank_account` VALUES (1, 1, 'CASH001', '现金账户', 1, NULL, NULL, 0.00, 'CNY', 1, 1, NULL, 0, NULL, '2026-04-18 19:41:44', NULL, '2026-04-18 19:41:44');
INSERT INTO `fin_bank_account` VALUES (2, 1, 'BANK001', '基本户', 2, '中国银行', '6210000000000000000', 0.00, 'CNY', 1, 0, NULL, 0, NULL, '2026-04-18 19:41:44', NULL, '2026-04-18 19:41:44');

-- ----------------------------
-- Table structure for fin_bank_reconciliation
-- ----------------------------
DROP TABLE IF EXISTS `fin_bank_reconciliation`;
CREATE TABLE `fin_bank_reconciliation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `bank_account_id` bigint NOT NULL,
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `status` int NOT NULL DEFAULT 0 COMMENT '0=进行中 1=已完成',
  `total_bank_amount` decimal(18, 4) NULL DEFAULT 0.0000,
  `total_system_amount` decimal(18, 4) NULL DEFAULT 0.0000,
  `matched_count` int NULL DEFAULT 0,
  `unmatched_count` int NULL DEFAULT 0,
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '银行对账记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fin_bank_reconciliation
-- ----------------------------

-- ----------------------------
-- Table structure for fin_bank_record
-- ----------------------------
DROP TABLE IF EXISTS `fin_bank_record`;
CREATE TABLE `fin_bank_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `bank_account_id` bigint NOT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(18, 4) NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `reference_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `transaction_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'DEBIT/CREDIT',
  `match_status` int NOT NULL DEFAULT 0 COMMENT '0=未匹配 1=自动匹配 2=手动匹配 3=异常',
  `matched_record_id` bigint NULL DEFAULT NULL COMMENT '匹配的系统记录ID',
  `import_batch` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_account`(`tenant_id` ASC, `bank_account_id` ASC) USING BTREE,
  INDEX `idx_match_status`(`match_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '银行对账单记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fin_bank_record
-- ----------------------------

-- ----------------------------
-- Table structure for fin_bill
-- ----------------------------
DROP TABLE IF EXISTS `fin_bill`;
CREATE TABLE `fin_bill`  (
  `id` bigint NOT NULL COMMENT '主键ID，雪花算法生成',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `bill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账单编号，唯一',
  `bill_type` int NOT NULL COMMENT '账单类型：1=应收, 2=应付',
  `direction` int NOT NULL DEFAULT 1 COMMENT '方向：1=正数(正常), -1=红字(退货)',
  `partner_type` int NOT NULL COMMENT '往来单位类型：1=客户, 2=供应商',
  `partner_id` bigint NOT NULL COMMENT '往来单位ID',
  `partner_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '往来单位名称',
  `account_id` bigint NULL DEFAULT NULL COMMENT '关联资金账户ID',
  `currency` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种编码',
  `exchange_rate` decimal(18, 6) NOT NULL DEFAULT 1.000000 COMMENT '汇率',
  `amount` decimal(18, 4) NOT NULL COMMENT '账单金额',
  `base_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '本位币金额（= amount * exchangeRate）',
  `paid_amount` decimal(18, 4) NOT NULL DEFAULT 0.0000 COMMENT '已收/已付金额',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `due_date` date NULL DEFAULT NULL COMMENT '到期日期',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0=草稿, 1=待审核, 2=已审核, 3=部分收付, 4=已完成, 5=已取消',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源类型：PURCHASE/SALE/MANUAL',
  `source_id` bigint NULL DEFAULT NULL COMMENT '来源单据ID',
  `source_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源单据编号',
  `audit_status` int NOT NULL DEFAULT 0 COMMENT '审核状态：0=无, 1=待审核, 2=已通过, 3=已驳回',
  `audit_by` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除标记：0=未删除, 1=已删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_bill_no`(`bill_no` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_bill_type`(`bill_type` ASC) USING BTREE,
  INDEX `idx_partner`(`partner_type` ASC, `partner_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_bill_date`(`bill_date` ASC) USING BTREE,
  INDEX `idx_source`(`source_type` ASC, `source_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '统一账单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fin_bill
-- ----------------------------

-- ----------------------------
-- Table structure for fin_bill_invoice
-- ----------------------------
DROP TABLE IF EXISTS `fin_bill_invoice`;
CREATE TABLE `fin_bill_invoice`  (
  `id` bigint NOT NULL COMMENT '主键ID，雪花算法生成',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID',
  `invoice_id` bigint NOT NULL COMMENT '发票ID',
  `related_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '关联金额',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除标记：0=未删除, 1=已删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_bill_id`(`bill_id` ASC) USING BTREE,
  INDEX `idx_invoice_id`(`invoice_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账单-发票关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fin_bill_invoice
-- ----------------------------

-- ----------------------------
-- Table structure for fin_budget
-- ----------------------------
DROP TABLE IF EXISTS `fin_budget`;
CREATE TABLE `fin_budget`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `budget_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `budget_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'DEPARTMENT/PROJECT/OVERALL',
  `target_id` bigint NULL DEFAULT NULL COMMENT '部门ID或项目ID',
  `period_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MONTHLY/QUARTERLY/YEARLY',
  `period_start` date NOT NULL,
  `period_end` date NOT NULL,
  `budget_amount` decimal(18, 4) NOT NULL,
  `used_amount` decimal(18, 4) NOT NULL DEFAULT 0.0000,
  `frozen_amount` decimal(18, 4) NOT NULL DEFAULT 0.0000,
  `remaining_amount` decimal(18, 4) NOT NULL DEFAULT 0.0000,
  `control_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WARN' COMMENT 'FORCE/WARN/LOG',
  `warning_threshold` decimal(5, 2) NULL DEFAULT 80.00 COMMENT '警告阈值(百分比)',
  `status` int NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已审批 2=执行中 3=已结束',
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_period`(`period_start` ASC, `period_end` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预算管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fin_budget
-- ----------------------------

-- ----------------------------
-- Table structure for fin_cost_config
-- ----------------------------
DROP TABLE IF EXISTS `fin_cost_config`;
CREATE TABLE `fin_cost_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `cost_method` tinyint NOT NULL DEFAULT 1 COMMENT '成本核算方法: 1-FIFO, 2-加权平均, 3-个别计价',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_tenant`(`product_id` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成本核算配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_cost_config
-- ----------------------------

-- ----------------------------
-- Table structure for fin_cost_history
-- ----------------------------
DROP TABLE IF EXISTS `fin_cost_history`;
CREATE TABLE `fin_cost_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `cost_price` decimal(18, 4) NOT NULL COMMENT '成本单价',
  `quantity` decimal(18, 4) NOT NULL COMMENT '数量',
  `total_cost` decimal(18, 4) NOT NULL COMMENT '总成本',
  `biz_type` tinyint NOT NULL COMMENT '业务类型: 1-采购入库, 2-销售出库, 3-调拨, 4-盘点调整',
  `biz_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务单号',
  `cost_method` tinyint NOT NULL COMMENT '使用的成本方法',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_product`(`tenant_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成本核算历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_cost_history
-- ----------------------------

-- ----------------------------
-- Table structure for fin_invoice
-- ----------------------------
DROP TABLE IF EXISTS `fin_invoice`;
CREATE TABLE `fin_invoice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `invoice_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发票编号',
  `invoice_type` tinyint NOT NULL DEFAULT 1 COMMENT '发票类型: 1-增值税专用发票, 2-增值税普通发票, 3-电子发票',
  `invoice_direction` tinyint NOT NULL COMMENT '发票方向: 1-开票(销售), 2-收票(采购)',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID',
  `partner_id` bigint NULL DEFAULT NULL COMMENT '客户/供应商ID',
  `invoice_date` datetime NOT NULL COMMENT '开票日期',
  `amount_without_tax` decimal(18, 2) NOT NULL COMMENT '不含税金额',
  `tax_amount` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '税额',
  `total_amount` decimal(18, 2) NOT NULL COMMENT '价税合计',
  `tax_rate` decimal(5, 2) NULL DEFAULT 13.00 COMMENT '税率',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-待开票, 1-已开票, 2-已作废',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invoice_no_tenant`(`invoice_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_invoice_direction`(`invoice_direction` ASC) USING BTREE,
  INDEX `idx_partner_id`(`partner_id` ASC) USING BTREE,
  INDEX `idx_invoice_date`(`invoice_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '发票表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_invoice
-- ----------------------------

-- ----------------------------
-- Table structure for fin_payment_record
-- ----------------------------
DROP TABLE IF EXISTS `fin_payment_record`;
CREATE TABLE `fin_payment_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `record_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单据编号',
  `record_type` tinyint NOT NULL COMMENT '收支类型: 1-收入, 2-支出',
  `biz_type` tinyint NOT NULL COMMENT '业务类型: 1-销售收款, 2-采购付款, 3-退款, 4-其他收入, 5-其他支出',
  `biz_id` bigint NULL DEFAULT NULL COMMENT '关联业务ID',
  `partner_id` bigint NULL DEFAULT NULL COMMENT '客户/供应商ID',
  `amount` decimal(18, 2) NOT NULL COMMENT '金额',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `payment_method` tinyint NOT NULL DEFAULT 1 COMMENT '支付方式: 1-现金, 2-银行转账, 3-支付宝, 4-微信, 5-支票',
  `bank_account_id` bigint NULL DEFAULT NULL COMMENT '银行账户ID',
  `transaction_date` datetime NOT NULL COMMENT '交易日期',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '经手人ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已审核, 2-已取消',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_record_no_tenant`(`record_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_record_type`(`record_type` ASC) USING BTREE,
  INDEX `idx_biz_type`(`biz_type` ASC) USING BTREE,
  INDEX `idx_transaction_date`(`transaction_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收支记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_payment_record
-- ----------------------------

-- ----------------------------
-- Table structure for fin_profit_calc
-- ----------------------------
DROP TABLE IF EXISTS `fin_profit_calc`;
CREATE TABLE `fin_profit_calc`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sales_order_id` bigint NULL DEFAULT NULL COMMENT '销售订单ID',
  `sales_quantity` decimal(18, 4) NOT NULL COMMENT '销售数量',
  `sales_amount` decimal(18, 2) NOT NULL COMMENT '销售金额',
  `cost_amount` decimal(18, 2) NOT NULL COMMENT '成本金额',
  `gross_profit` decimal(18, 2) NOT NULL COMMENT '毛利',
  `gross_profit_rate` decimal(8, 4) NOT NULL COMMENT '毛利率',
  `calc_date` date NOT NULL COMMENT '计算日期',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_product`(`tenant_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_calc_date`(`calc_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '毛利计算表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_profit_calc
-- ----------------------------

-- ----------------------------
-- Table structure for fin_report
-- ----------------------------
DROP TABLE IF EXISTS `fin_report`;
CREATE TABLE `fin_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `report_type` tinyint NOT NULL COMMENT '报表类型: 1-资产负债表, 2-利润表, 3-现金流量表, 4-毛利分析',
  `report_period` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报表期间 (如 2026-01)',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `report_data` json NOT NULL COMMENT '报表数据(JSON)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已生成, 2-已锁定',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_period_tenant`(`report_type` ASC, `report_period` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '财务报表表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fin_report
-- ----------------------------

-- ----------------------------
-- Table structure for gateway_circuit_breaker
-- ----------------------------
DROP TABLE IF EXISTS `gateway_circuit_breaker`;
CREATE TABLE `gateway_circuit_breaker`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `route_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由ID',
  `failure_threshold` int NOT NULL DEFAULT 5 COMMENT '失败阈值',
  `cooldown_seconds` int NOT NULL DEFAULT 60 COMMENT '冷却时间(秒)',
  `half_open_max` int NULL DEFAULT 3 COMMENT '半开状态最大请求数',
  `state` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CLOSED' COMMENT '状态(CLOSED/OPEN/HALF_OPEN)',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_route`(`tenant_id` ASC, `route_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '网关熔断配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gateway_circuit_breaker
-- ----------------------------

-- ----------------------------
-- Table structure for gateway_rate_limit
-- ----------------------------
DROP TABLE IF EXISTS `gateway_rate_limit`;
CREATE TABLE `gateway_rate_limit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `route_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由ID',
  `limit_dimension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IP' COMMENT '限流维度(IP/USER/TENANT)',
  `max_requests` int NOT NULL DEFAULT 100 COMMENT '最大请求数',
  `window_seconds` int NOT NULL DEFAULT 60 COMMENT '时间窗口(秒)',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_route`(`tenant_id` ASC, `route_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '网关限流配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gateway_rate_limit
-- ----------------------------

-- ----------------------------
-- Table structure for gateway_route
-- ----------------------------
DROP TABLE IF EXISTS `gateway_route`;
CREATE TABLE `gateway_route`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `route_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由ID',
  `uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标URI',
  `predicates` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '断言配置(JSON)',
  `filters` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '过滤器配置(JSON)',
  `order_num` int NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_route_id`(`route_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '网关路由配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gateway_route
-- ----------------------------

-- ----------------------------
-- Table structure for gen_history
-- ----------------------------
DROP TABLE IF EXISTS `gen_history`;
CREATE TABLE `gen_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `table_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `module_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `package_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `gen_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ALL',
  `gen_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代码生成历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_history
-- ----------------------------

-- ----------------------------
-- Table structure for login_log
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID(登录失败时为null)',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '尝试登录的用户名',
  `login_type` tinyint NOT NULL DEFAULT 1 COMMENT '登录类型: 1-密码登录, 2-短信登录, 3-社交登录',
  `login_status` tinyint NOT NULL COMMENT '状态: 0-失败, 1-成功',
  `fail_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败原因',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `device_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备信息',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE,
  INDEX `idx_login_status`(`login_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of login_log
-- ----------------------------
INSERT INTO `login_log` VALUES (7, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:06:30');
INSERT INTO `login_log` VALUES (8, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:06:35');
INSERT INTO `login_log` VALUES (9, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:06:44');
INSERT INTO `login_log` VALUES (10, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:06:59');
INSERT INTO `login_log` VALUES (11, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:13:04');
INSERT INTO `login_log` VALUES (12, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:13:23');
INSERT INTO `login_log` VALUES (13, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:14:11');
INSERT INTO `login_log` VALUES (14, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:14:39');
INSERT INTO `login_log` VALUES (15, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:19:30');
INSERT INTO `login_log` VALUES (16, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:20:58');
INSERT INTO `login_log` VALUES (17, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:23:09');
INSERT INTO `login_log` VALUES (18, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:24:51');
INSERT INTO `login_log` VALUES (19, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:36:47');
INSERT INTO `login_log` VALUES (20, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:40:38');
INSERT INTO `login_log` VALUES (21, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:42:32');
INSERT INTO `login_log` VALUES (22, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 20:45:53');
INSERT INTO `login_log` VALUES (23, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'curl/8.4.0', '2026-04-18 21:04:07');
INSERT INTO `login_log` VALUES (24, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-18 21:04:18');
INSERT INTO `login_log` VALUES (25, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'curl/8.4.0', '2026-04-18 21:20:25');
INSERT INTO `login_log` VALUES (26, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 00:49:32');
INSERT INTO `login_log` VALUES (27, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'curl/8.4.0', '2026-04-19 01:55:13');
INSERT INTO `login_log` VALUES (28, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 10:52:56');
INSERT INTO `login_log` VALUES (29, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 13:35:54');
INSERT INTO `login_log` VALUES (30, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 14:18:03');
INSERT INTO `login_log` VALUES (31, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 14:18:05');
INSERT INTO `login_log` VALUES (32, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 14:21:33');
INSERT INTO `login_log` VALUES (33, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 14:22:05');
INSERT INTO `login_log` VALUES (34, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 14:23:43');
INSERT INTO `login_log` VALUES (35, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '2026-04-19 14:23:53');
INSERT INTO `login_log` VALUES (36, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-16 12:50:38');
INSERT INTO `login_log` VALUES (37, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-16 16:14:57');
INSERT INTO `login_log` VALUES (38, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-16 23:25:35');
INSERT INTO `login_log` VALUES (39, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-19 22:05:01');
INSERT INTO `login_log` VALUES (40, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-20 01:26:18');
INSERT INTO `login_log` VALUES (41, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-20 01:37:03');
INSERT INTO `login_log` VALUES (42, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-23 13:15:32');
INSERT INTO `login_log` VALUES (43, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-23 13:23:07');
INSERT INTO `login_log` VALUES (50, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-23 16:37:02');
INSERT INTO `login_log` VALUES (51, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-23 16:53:47');
INSERT INTO `login_log` VALUES (52, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-23 19:01:46');
INSERT INTO `login_log` VALUES (53, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-23 22:16:35');
INSERT INTO `login_log` VALUES (54, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-24 14:42:36');
INSERT INTO `login_log` VALUES (55, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-24 21:27:18');
INSERT INTO `login_log` VALUES (56, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-24 22:16:22');
INSERT INTO `login_log` VALUES (57, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-25 22:45:21');
INSERT INTO `login_log` VALUES (58, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-25 23:41:21');
INSERT INTO `login_log` VALUES (59, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-05-25 23:43:14');
INSERT INTO `login_log` VALUES (60, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-06-01 14:57:14');
INSERT INTO `login_log` VALUES (61, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-06-01 14:57:29');
INSERT INTO `login_log` VALUES (62, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-06-01 17:16:51');
INSERT INTO `login_log` VALUES (63, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-06-01 17:28:51');
INSERT INTO `login_log` VALUES (64, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-06-01 18:24:40');
INSERT INTO `login_log` VALUES (65, 1, 1, 'admin', 1, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '2026-06-01 18:29:34');

-- ----------------------------
-- Table structure for login_session
-- ----------------------------
DROP TABLE IF EXISTS `login_session`;
CREATE TABLE `login_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `token_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'JWT令牌ID',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'web' COMMENT '设备类型: web, mobile, desktop',
  `device_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备信息(User-Agent)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `expire_time` datetime NOT NULL COMMENT '令牌过期时间',
  `logout_time` datetime NULL DEFAULT NULL COMMENT '登出时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-已登出, 1-活跃',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_token_id`(`token_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_session_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_login_session_tenant`(`tenant_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of login_session
-- ----------------------------
INSERT INTO `login_session` VALUES (1, 1, 1, '4bf0d41d-fb72-449c-959f-31c5ccf52684', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:06:30', '2026-04-18 22:06:30', '2026-05-23 13:15:32', 0, '2026-04-18 20:06:30', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (2, 1, 1, '2b043649-f6e1-47be-9175-59572464ff9d', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:06:35', '2026-04-18 22:06:35', '2026-05-23 13:15:32', 0, '2026-04-18 20:06:34', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (3, 1, 1, '44798c6b-df73-427f-ae59-6d0fa9f2be2a', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:06:44', '2026-04-18 22:06:44', '2026-05-23 13:15:32', 0, '2026-04-18 20:06:44', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (4, 1, 1, '41b4e7bc-928f-4b69-92c0-c27adbfe1efc', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:06:59', '2026-04-18 22:06:59', '2026-05-23 13:15:32', 0, '2026-04-18 20:06:58', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (5, 1, 1, 'c693c46b-03dc-48c0-90f9-38cb275edc6c', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:13:04', '2026-04-18 22:13:04', '2026-05-23 13:15:32', 0, '2026-04-18 20:13:04', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (6, 1, 1, '84f770fb-821f-446b-a71c-d360a4b190d1', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:13:23', '2026-04-18 22:13:23', '2026-05-23 13:15:32', 0, '2026-04-18 20:13:22', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (7, 1, 1, 'd2e1db8e-1ca8-495e-bb62-230017fb7684', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:14:11', '2026-04-18 22:14:11', '2026-05-23 13:15:32', 0, '2026-04-18 20:14:10', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (8, 1, 1, 'dc26e293-d44d-4786-8bc5-3b9c3b1a9a90', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:14:39', '2026-04-18 22:14:39', '2026-05-23 13:15:32', 0, '2026-04-18 20:14:38', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (9, 1, 1, 'adac9694-646f-445a-a058-d3f74979ded9', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:19:30', '2026-04-18 22:19:30', '2026-05-23 13:15:32', 0, '2026-04-18 20:19:29', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (10, 1, 1, '856712f8-138d-4d89-ad69-a3a0b8d2b3bd', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:20:59', '2026-04-18 22:20:59', '2026-05-23 13:15:32', 0, '2026-04-18 20:20:58', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (11, 1, 1, 'b8847466-29fc-4b49-92af-79d701ae3063', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:23:09', '2026-04-18 22:23:09', '2026-05-23 13:15:32', 0, '2026-04-18 20:23:09', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (12, 1, 1, '86619b47-3bec-4559-aecb-df1d4255449b', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:24:51', '2026-04-18 22:24:51', '2026-05-23 13:15:32', 0, '2026-04-18 20:24:51', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (13, 1, 1, '5736e8b3-0171-4679-9694-3dd0d55b0e32', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:36:47', '2026-04-18 22:36:47', '2026-05-23 13:15:32', 0, '2026-04-18 20:36:47', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (14, 1, 1, '5d59ddd5-92b6-4922-894a-7ce52ab88160', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:40:38', '2026-04-18 22:40:38', '2026-05-23 13:15:32', 0, '2026-04-18 20:40:38', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (15, 1, 1, '28c0d460-3ae5-43ab-8a59-d6b51c3afa3e', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:42:33', '2026-04-18 22:42:33', '2026-05-23 13:15:33', 0, '2026-04-18 20:42:32', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (16, 1, 1, '79446b45-9fe2-4e52-8a9d-c2abd30289b7', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 20:45:53', '2026-04-18 22:45:53', '2026-05-23 13:15:33', 0, '2026-04-18 20:45:53', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (17, 1, 1, '42706c07-87a6-405a-aa7b-c2a350436b89', 'web', 'curl/8.4.0', '0:0:0:0:0:0:0:1', '2026-04-18 21:04:07', '2026-04-18 23:04:07', '2026-05-23 13:15:33', 0, '2026-04-18 21:04:06', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (18, 1, 1, '67951f94-fc62-4c0a-bdea-12c835736dce', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-18 21:04:18', '2026-04-18 23:04:18', '2026-05-23 13:15:33', 0, '2026-04-18 21:04:18', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (19, 1, 1, 'e68671c3-3187-48f5-b35b-38c0c7e222b2', 'web', 'curl/8.4.0', '0:0:0:0:0:0:0:1', '2026-04-18 21:20:25', '2026-04-18 23:20:25', '2026-05-23 13:15:33', 0, '2026-04-18 21:20:24', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (20, 1, 1, '33734a95-9412-41d8-add1-09a6a668a38a', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 00:49:32', '2026-04-19 02:49:32', '2026-05-23 13:15:33', 0, '2026-04-19 00:49:32', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (21, 1, 1, 'f55b3a86-c949-44f6-b032-c75a283dc36d', 'web', 'curl/8.4.0', '0:0:0:0:0:0:0:1', '2026-04-19 01:55:13', '2026-04-19 03:55:13', '2026-05-23 13:15:33', 0, '2026-04-19 01:55:13', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (22, 1, 1, '6818b564-453c-46d2-a0c1-4e4b302d581e', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 10:52:56', '2026-04-19 12:52:56', '2026-05-23 13:15:33', 0, '2026-04-19 10:52:55', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (23, 1, 1, '7f6d0918-fc8d-434a-8904-28c7f7a70921', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 13:35:54', '2026-04-19 15:35:54', '2026-05-23 13:15:33', 0, '2026-04-19 13:35:54', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (24, 1, 1, '139189c8-b17e-47cd-9049-0394bef91f3a', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 14:18:03', '2026-04-19 16:18:03', '2026-05-23 13:15:33', 0, '2026-04-19 14:18:02', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (25, 1, 1, '7d21be85-db80-4ecc-b006-66319bcd96a3', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 14:18:05', '2026-04-19 16:18:05', '2026-05-23 13:15:33', 0, '2026-04-19 14:18:04', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (26, 1, 1, '926efd94-db44-4675-b304-e4faedd88b2e', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 14:21:33', '2026-04-19 16:21:33', '2026-05-23 13:15:33', 0, '2026-04-19 14:21:33', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (27, 1, 1, '1f323f28-232b-4354-865c-dbd5e16dd928', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 14:22:05', '2026-04-19 16:22:05', '2026-05-23 13:15:33', 0, '2026-04-19 14:22:05', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (28, 1, 1, '75d171d5-2a47-4ec4-817e-ff49628c2111', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 14:23:43', '2026-04-19 16:23:43', '2026-05-23 13:15:33', 0, '2026-04-19 14:23:43', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (29, 1, 1, 'f14ca65c-51c6-46a4-aba8-21509db09f6b', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.0.8364.400', '0:0:0:0:0:0:0:1', '2026-04-19 14:23:53', '2026-04-19 16:23:53', '2026-05-23 13:15:33', 0, '2026-04-19 14:23:53', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (30, 1, 1, '5851b410-d4ca-422b-86d4-3c3389ccda71', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-16 12:50:38', '2026-05-16 14:50:38', '2026-05-23 13:15:33', 0, '2026-05-16 12:50:37', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (31, 1, 1, 'df1918e7-b382-4250-969a-aa9588b0f470', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-16 16:14:57', '2026-05-16 18:14:57', '2026-05-23 13:15:33', 0, '2026-05-16 16:14:56', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (32, 1, 1, '97cb6aea-9d6a-4b4f-881a-b4205f91d625', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-16 23:25:35', '2026-05-17 01:25:35', '2026-05-23 13:15:33', 0, '2026-05-16 23:25:35', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (33, 1, 1, '0adb56b4-cf86-4d2e-84dd-09ccdface636', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-19 22:05:01', '2026-05-20 00:05:01', '2026-05-23 13:15:33', 0, '2026-05-19 22:05:01', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (34, 1, 1, '5801d319-65ef-478a-81bb-a7783b063ae9', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-20 01:26:18', '2026-05-20 03:26:18', '2026-05-23 13:15:33', 0, '2026-05-20 01:26:18', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (35, 1, 1, 'fd1b3f97-8ae1-454c-b770-596046593135', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-20 01:37:04', '2026-05-20 03:37:04', '2026-05-23 13:15:33', 0, '2026-05-20 01:37:03', '2026-05-23 13:15:32');
INSERT INTO `login_session` VALUES (36, 1, 1, 'c2714bad-8e33-40da-9313-6e1fee210a1b', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-23 13:15:32', '2026-05-23 15:15:32', '2026-05-23 13:23:07', 0, '2026-05-23 13:15:32', '2026-05-23 13:23:07');
INSERT INTO `login_session` VALUES (37, 1, 1, 'a932f0c2-8800-4366-bb1f-aba265abc91c', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-23 13:23:07', '2026-05-23 15:23:07', '2026-05-23 16:37:02', 0, '2026-05-23 13:23:07', '2026-05-23 16:37:01');
INSERT INTO `login_session` VALUES (40, 1, 1, '5082b3a3-cdc6-42e0-aff4-d585d7f7fdf3', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-23 16:37:02', '2026-05-23 18:37:02', '2026-05-23 16:53:47', 0, '2026-05-23 16:37:01', '2026-05-23 16:53:46');
INSERT INTO `login_session` VALUES (41, 1, 1, '6709c0bc-f215-4695-a6da-986b3cf9e9f1', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-23 16:53:47', '2026-05-23 18:53:47', '2026-05-23 19:01:46', 0, '2026-05-23 16:53:46', '2026-05-23 19:01:45');
INSERT INTO `login_session` VALUES (42, 1, 1, '7d6e2634-9d23-4824-b1c3-b30476ce4c90', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-23 19:01:46', '2026-05-23 21:01:46', '2026-05-23 22:16:35', 0, '2026-05-23 19:01:45', '2026-05-23 22:16:35');
INSERT INTO `login_session` VALUES (43, 1, 1, 'babe0e8d-a4d1-46e2-a531-b8712aa00294', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-23 22:16:35', '2026-05-24 00:16:35', '2026-05-24 14:42:37', 0, '2026-05-23 22:16:35', '2026-05-24 14:42:36');
INSERT INTO `login_session` VALUES (44, 1, 1, '8109a3a9-7bf0-4727-aa71-c2b0cbe19411', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-24 14:42:37', '2026-05-24 16:42:37', '2026-05-24 21:27:18', 0, '2026-05-24 14:42:36', '2026-05-24 21:27:18');
INSERT INTO `login_session` VALUES (45, 1, 1, 'df94158f-6de8-41fc-94e8-7fe1f143ae69', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-24 21:27:18', '2026-05-24 23:27:18', '2026-05-24 22:16:22', 0, '2026-05-24 21:27:18', '2026-05-24 22:16:22');
INSERT INTO `login_session` VALUES (46, 1, 1, '8a90d428-5845-4413-9bc0-ef709583b225', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-24 22:16:22', '2026-05-25 00:16:22', '2026-05-25 22:45:21', 0, '2026-05-24 22:16:22', '2026-05-25 22:45:21');
INSERT INTO `login_session` VALUES (47, 1, 1, 'b784a0a5-713a-4667-88f4-0a5d2cca5788', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-25 22:45:21', '2026-05-26 00:45:21', '2026-05-25 23:41:21', 0, '2026-05-25 22:45:21', '2026-05-25 23:41:21');
INSERT INTO `login_session` VALUES (48, 1, 1, '1dc32fa9-ef1a-49af-b722-274c0a3c9eec', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-25 23:41:21', '2026-05-26 01:41:21', '2026-05-25 23:43:14', 0, '2026-05-25 23:41:21', '2026-05-25 23:43:13');
INSERT INTO `login_session` VALUES (49, 1, 1, '074d2bfc-4cd3-43bd-b232-49f20ac92e86', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-05-25 23:43:14', '2026-05-26 01:43:14', '2026-06-01 14:57:14', 0, '2026-05-25 23:43:13', '2026-06-01 14:57:14');
INSERT INTO `login_session` VALUES (50, 1, 1, 'fec51ff5-4fa0-425a-9bbc-01bc213fb755', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-06-01 14:57:14', '2026-06-01 16:57:14', '2026-06-01 14:57:29', 0, '2026-06-01 14:57:14', '2026-06-01 14:57:28');
INSERT INTO `login_session` VALUES (51, 1, 1, '08fd8208-d798-48d3-93aa-29c61e6e7ab0', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-06-01 14:57:29', '2026-06-01 16:57:29', '2026-06-01 17:16:51', 0, '2026-06-01 14:57:28', '2026-06-01 17:16:51');
INSERT INTO `login_session` VALUES (52, 1, 1, 'bcdb956e-c16e-48ce-b507-95dfcd80e65c', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-06-01 17:16:51', '2026-06-01 19:16:51', '2026-06-01 17:28:52', 0, '2026-06-01 17:16:51', '2026-06-01 17:28:51');
INSERT INTO `login_session` VALUES (53, 1, 1, 'a3d72c2c-0014-45bc-ac47-396d0d4d87d2', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-06-01 17:28:52', '2026-06-01 19:28:52', '2026-06-01 18:24:40', 0, '2026-06-01 17:28:51', '2026-06-01 18:24:39');
INSERT INTO `login_session` VALUES (54, 1, 1, '3329510d-7e60-4e29-9c5d-6518265f3946', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-06-01 18:24:40', '2026-06-01 20:24:40', '2026-06-01 18:29:34', 0, '2026-06-01 18:24:39', '2026-06-01 18:29:34');
INSERT INTO `login_session` VALUES (55, 1, 1, '71612014-edc1-4a2d-85e0-d6568305ea76', 'web', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 QQBrowser/21.1.8662.400', '0:0:0:0:0:0:0:1', '2026-06-01 18:29:34', '2026-06-01 20:29:34', NULL, 1, '2026-06-01 18:29:34', '2026-06-01 18:29:34');

-- ----------------------------
-- Table structure for marketing_activity
-- ----------------------------
DROP TABLE IF EXISTS `marketing_activity`;
CREATE TABLE `marketing_activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `activity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'DISCOUNT/TRIAL/GIFT',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `discount_value` decimal(10, 2) NULL DEFAULT NULL,
  `status` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '营销活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of marketing_activity
-- ----------------------------

-- ----------------------------
-- Table structure for password_history
-- ----------------------------
DROP TABLE IF EXISTS `password_history`;
CREATE TABLE `password_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希值',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '密码历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of password_history
-- ----------------------------

-- ----------------------------
-- Table structure for refresh_token
-- ----------------------------
DROP TABLE IF EXISTS `refresh_token`;
CREATE TABLE `refresh_token`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `token_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '刷新令牌哈希值(SHA-256)',
  `session_id` bigint NULL DEFAULT NULL COMMENT '关联会话ID',
  `expire_time` datetime NOT NULL COMMENT '令牌过期时间',
  `revoked` tinyint NOT NULL DEFAULT 0 COMMENT '已吊销: 0-有效, 1-已吊销',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_token_hash`(`token_hash` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_token_user_revoked`(`user_id` ASC, `revoked` ASC) USING BTREE,
  INDEX `idx_refresh_token_tenant`(`tenant_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '刷新令牌表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of refresh_token
-- ----------------------------
INSERT INTO `refresh_token` VALUES (1, 1, 1, 'd00d1fd44faf0a24a9dc4e8661008e636d2dfc090f8a94e6db6eeb6cc36960b4', 1, '2026-04-25 20:06:30', 0, '2026-04-18 20:06:30');
INSERT INTO `refresh_token` VALUES (2, 1, 1, '756a6f575569b6d04f74be3af0f4924f179913187f9e684607c05b838009c5e6', 2, '2026-04-25 20:06:35', 0, '2026-04-18 20:06:34');
INSERT INTO `refresh_token` VALUES (3, 1, 1, 'c1a8424f2e49edf89a6bea6a37705bcb02ddec44674fe2a89e7e63924ac0e537', 3, '2026-04-25 20:06:44', 0, '2026-04-18 20:06:44');
INSERT INTO `refresh_token` VALUES (4, 1, 1, '65f5b46b5d9514f3267d920b354c28dfb0827656c892d10ece4b071f68c6b8ee', 4, '2026-04-25 20:06:59', 0, '2026-04-18 20:06:58');
INSERT INTO `refresh_token` VALUES (5, 1, 1, '32686a1f858c86c8b507d00249497f296c3f001397a451000ffda5e97f6305e2', 5, '2026-04-25 20:13:04', 0, '2026-04-18 20:13:04');
INSERT INTO `refresh_token` VALUES (6, 1, 1, '5df51d1bdb3402911735e560069bde434a9ecf955599aaf4444efb01f6f61657', 6, '2026-04-25 20:13:23', 0, '2026-04-18 20:13:22');
INSERT INTO `refresh_token` VALUES (7, 1, 1, '06f63d3103980652a1978d97a25a6c730480fcd64342be8e1a0738763785f530', 7, '2026-04-25 20:14:11', 0, '2026-04-18 20:14:10');
INSERT INTO `refresh_token` VALUES (8, 1, 1, 'f1f274d4c89fe5698850a0542e7d491b9db9b3cc8d6c06e8a01f824c450245e0', 8, '2026-04-25 20:14:39', 0, '2026-04-18 20:14:38');
INSERT INTO `refresh_token` VALUES (9, 1, 1, '81036d2b1425ccf8a3a227b90fb993cc11803826bf137df15be12ac81a98b204', 9, '2026-04-25 20:19:30', 0, '2026-04-18 20:19:29');
INSERT INTO `refresh_token` VALUES (10, 1, 1, '9e6d8046c0e990699b342024de651a08001b53c88e34acf957541ce182d1363c', 10, '2026-04-25 20:20:59', 0, '2026-04-18 20:20:58');
INSERT INTO `refresh_token` VALUES (11, 1, 1, 'cfbe3cbb79b405f040ec0561c5e6c8b7ff85388b7a0d2986049683df94b248d9', 11, '2026-04-25 20:23:09', 0, '2026-04-18 20:23:09');
INSERT INTO `refresh_token` VALUES (12, 1, 1, '47c1372bc3379d03d2bef8fc03f32cdb55e21e9da3a5079afed3f115c1cb2d40', 12, '2026-04-25 20:24:51', 0, '2026-04-18 20:24:51');
INSERT INTO `refresh_token` VALUES (13, 1, 1, '037b58780ae449e3a5c301831140956724be1e1120aa0e27504e017f4530ecc4', 13, '2026-04-25 20:36:47', 0, '2026-04-18 20:36:47');
INSERT INTO `refresh_token` VALUES (14, 1, 1, 'c7be5c8c6d54883bc914f764c703fbebc004b41c990985e9b98eb6a308fff085', 14, '2026-04-25 20:40:38', 0, '2026-04-18 20:40:38');
INSERT INTO `refresh_token` VALUES (15, 1, 1, 'ca4a56220675b4ae34855ba5720e0ca8357f14314b42f12bc334026be662954e', 15, '2026-04-25 20:42:33', 0, '2026-04-18 20:42:32');
INSERT INTO `refresh_token` VALUES (16, 1, 1, '2419ab01fe19b8a7984e45d88ddbe3db853a36a194c066dbd6b75f412c8dc0c8', 16, '2026-04-25 20:45:53', 0, '2026-04-18 20:45:53');
INSERT INTO `refresh_token` VALUES (17, 1, 1, 'd99690de5b43b71f074bb64caf4e6fa640e47e409bfc2f45947f1aad57cced31', 17, '2026-04-25 21:04:07', 0, '2026-04-18 21:04:06');
INSERT INTO `refresh_token` VALUES (18, 1, 1, '236cc634f0398034e6d31e516c4c9e488b70c06a9a44174cf02563ab0c27db29', 18, '2026-04-25 21:04:18', 0, '2026-04-18 21:04:18');
INSERT INTO `refresh_token` VALUES (19, 1, 1, '6e97f5e4a683817e218c84385b08aa24ca3b240eb268f89d1b6336e8ae640c61', 19, '2026-04-25 21:20:25', 0, '2026-04-18 21:20:24');
INSERT INTO `refresh_token` VALUES (20, 1, 1, '691574415370757b04107ca2082866b32bab216bdff8e55c568f3901f5c034e6', 20, '2026-04-26 00:49:32', 0, '2026-04-19 00:49:32');
INSERT INTO `refresh_token` VALUES (21, 1, 1, 'c3179ed684d00361e94c5ecb371581c10eb11ff3aed2d30475ccf6701e8804bd', 21, '2026-04-26 01:55:13', 0, '2026-04-19 01:55:13');
INSERT INTO `refresh_token` VALUES (22, 1, 1, 'c25f101c373a0e423d19ba35face6cea6e4e8d9e29dffacc1fb7d8a9a46eb5b5', 22, '2026-04-26 10:52:56', 0, '2026-04-19 10:52:55');
INSERT INTO `refresh_token` VALUES (23, 1, 1, '3654e4291e9fed33bdbe50264701b2461651cfc0e36364c879c8f1932be68a8b', 23, '2026-04-26 13:35:54', 0, '2026-04-19 13:35:54');
INSERT INTO `refresh_token` VALUES (24, 1, 1, 'a6ec2e08a2ba1b3b63d2544e914403c6f8aa1510b461b4b75576f4c72b9ddb0a', 24, '2026-04-26 14:18:03', 0, '2026-04-19 14:18:02');
INSERT INTO `refresh_token` VALUES (25, 1, 1, '3aac00800570fe65fef10dd2546cac3d5f11440071e8a8b33abf665e7037685c', 25, '2026-04-26 14:18:05', 0, '2026-04-19 14:18:04');
INSERT INTO `refresh_token` VALUES (26, 1, 1, '0fbbbea68826a9f6ca31da38410ae2b90b2107f4c1ee356de0c8e1261f93248b', 26, '2026-04-26 14:21:33', 0, '2026-04-19 14:21:33');
INSERT INTO `refresh_token` VALUES (27, 1, 1, 'ffa9b03f3863e437187ee202b41a8e645c47fbfeb786c5780efa327d1430dac8', 27, '2026-04-26 14:22:05', 0, '2026-04-19 14:22:05');
INSERT INTO `refresh_token` VALUES (28, 1, 1, 'c5003db733ecc0430bdb3454449beeb1e235181b7d6fdc26b4fcab7e34bb9c4d', 28, '2026-04-26 14:23:43', 0, '2026-04-19 14:23:43');
INSERT INTO `refresh_token` VALUES (29, 1, 1, '4349bfa7482093936d53e1042dac5a1cfe3d940b6e8f1a4e7e1184b37c5ab8c2', 29, '2026-04-26 14:23:53', 0, '2026-04-19 14:23:53');
INSERT INTO `refresh_token` VALUES (30, 1, 1, 'ce1e0bb66a3df56cf1fd77f463f283778d4f164e41d21bc77997734759c3a0f0', 30, '2026-05-23 12:50:38', 0, '2026-05-16 12:50:38');
INSERT INTO `refresh_token` VALUES (31, 1, 1, '80d2090e52e3e3276e20f882adc5a6bcd13c0568bbfcc72350a7c9436f698620', 31, '2026-05-23 16:14:57', 0, '2026-05-16 16:14:56');
INSERT INTO `refresh_token` VALUES (32, 1, 1, 'bc9ddd71c79107419652e665c5406e7791fc0547c1dcc5dc41e16503be073f35', 32, '2026-05-23 23:25:35', 0, '2026-05-16 23:25:35');
INSERT INTO `refresh_token` VALUES (33, 1, 1, '49de89c61e31ff84ab59af8603b4d2d64001abd841e257fd5dfb16b890fe29f6', 33, '2026-05-26 22:05:01', 0, '2026-05-19 22:05:01');
INSERT INTO `refresh_token` VALUES (34, 1, 1, 'b7773343510d72724cbe066b25514cc44cdf2d15e5a26a11057903056a4a870d', 34, '2026-05-27 01:26:18', 0, '2026-05-20 01:26:18');
INSERT INTO `refresh_token` VALUES (35, 1, 1, 'f58ebb3c036372ee90635a383ee2c034eaea5e53b353ff53823a64235193404f', 35, '2026-05-27 01:37:04', 0, '2026-05-20 01:37:03');
INSERT INTO `refresh_token` VALUES (36, 1, 1, 'a83ac507c0b3ed2cce0a371045f1d029ef70f1f453c6a5aac95b86aaae4b4407', 36, '2026-05-30 13:15:33', 0, '2026-05-23 13:15:32');
INSERT INTO `refresh_token` VALUES (37, 1, 1, 'e137043d7101c1dbb33f771bb7f8a46c53714a1baa2b8d4cde578fd2e8dd1af4', 37, '2026-05-30 13:23:07', 0, '2026-05-23 13:23:07');
INSERT INTO `refresh_token` VALUES (40, 1, 1, 'a70ba610b4d3397d346a9b0ce9c4551ffdb09e6bda4cd07d20f756655cd9adf1', 40, '2026-05-30 16:37:02', 0, '2026-05-23 16:37:01');
INSERT INTO `refresh_token` VALUES (41, 1, 1, 'd6b06a4439bdd44dd83e7b9f75dd6d50cfef178407681694f4eb5c97da68761b', 41, '2026-05-30 16:53:47', 0, '2026-05-23 16:53:46');
INSERT INTO `refresh_token` VALUES (42, 1, 1, 'd105920c0d0df80f8fa93163c03298cbae8ab6d4544164bfb370fbaa38fde83f', 42, '2026-05-30 19:01:46', 0, '2026-05-23 19:01:45');
INSERT INTO `refresh_token` VALUES (43, 1, 1, '0f778266da2ab64c9184c8878bf688196caf3cb9905e44a46a8378702a9ccddc', 43, '2026-05-30 22:16:35', 0, '2026-05-23 22:16:35');
INSERT INTO `refresh_token` VALUES (44, 1, 1, '4080146f9c141d291b237f2be279add7e48ab0300af1d380dee593443ccd7c0a', 44, '2026-05-31 14:42:37', 0, '2026-05-24 14:42:36');
INSERT INTO `refresh_token` VALUES (45, 1, 1, '2ccc29c376ac61c29c814eeccbaff86d5646098a15a1f958ab275bbd61d3c899', 45, '2026-05-31 21:27:18', 0, '2026-05-24 21:27:18');
INSERT INTO `refresh_token` VALUES (46, 1, 1, 'c9c5f0c4adbe9efc4e4680603ad14444599fa40007e443ee46e4868d4567fd16', 46, '2026-05-31 22:16:22', 0, '2026-05-24 22:16:22');
INSERT INTO `refresh_token` VALUES (47, 1, 1, 'fbbd69e2e8efd53cdb77b4e7345a62848d12ed7e79cb0f1fb9af25d5dcd3072d', 47, '2026-06-01 22:45:21', 0, '2026-05-25 22:45:21');
INSERT INTO `refresh_token` VALUES (48, 1, 1, '860f656e9ffeea582537239547b1e262d712e6ed953eeec2945c3163925e20d8', 48, '2026-06-01 23:41:21', 0, '2026-05-25 23:41:21');
INSERT INTO `refresh_token` VALUES (49, 1, 1, 'd19e9a2ad877dc97c4546501bffd2c280c79180457426f012783b3a95aa2d345', 49, '2026-06-01 23:43:14', 0, '2026-05-25 23:43:13');
INSERT INTO `refresh_token` VALUES (50, 1, 1, 'a8c757f090968b694048746c0f3c69dfe3533d164be292c121e1016f75e85308', 50, '2026-06-08 14:57:14', 0, '2026-06-01 14:57:14');
INSERT INTO `refresh_token` VALUES (51, 1, 1, '19ffb188238709c4ba524fee86b6125caeca1300042a3c0cd1d8b4b797cf76bc', 51, '2026-06-08 14:57:29', 0, '2026-06-01 14:57:28');
INSERT INTO `refresh_token` VALUES (52, 1, 1, '0fbe3a4227881561332dbe126e8f0124480d3b4dcc31b77d9c9e3ae37c3867e6', 52, '2026-06-08 17:16:51', 0, '2026-06-01 17:16:51');
INSERT INTO `refresh_token` VALUES (53, 1, 1, 'cb2cfbb4eabffbc21e89192729c0eaf3d1db319c058748b458ac1b38d56825c9', 53, '2026-06-08 17:28:52', 0, '2026-06-01 17:28:51');
INSERT INTO `refresh_token` VALUES (54, 1, 1, 'e6d9736f60e0901031dce51ed7ac0defbe1c035d3574d62e42e96109fe49484c', 54, '2026-06-08 18:24:40', 0, '2026-06-01 18:24:39');
INSERT INTO `refresh_token` VALUES (55, 1, 1, '2b9cc35a48f7220bad64b5d07869acd69e05a9c2cb00d11d6cdcd295917705f2', 55, '2026-06-08 18:29:34', 0, '2026-06-01 18:29:34');

-- ----------------------------
-- Table structure for saas_package
-- ----------------------------
DROP TABLE IF EXISTS `saas_package`;
CREATE TABLE `saas_package`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `package_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `package_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `billing_cycle` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'MONTHLY',
  `max_users` int NOT NULL DEFAULT 5,
  `max_storage_mb` int NOT NULL DEFAULT 1024,
  `features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '功能列表(JSON)',
  `status` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `package_code`(`package_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SaaS套餐表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of saas_package
-- ----------------------------

-- ----------------------------
-- Table structure for social_account
-- ----------------------------
DROP TABLE IF EXISTS `social_account`;
CREATE TABLE `social_account`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '社交账号ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提供商: wechat, alipay, dingtalk',
  `provider_user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提供商用户ID',
  `union_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '统一ID',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `extra_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '扩展数据(JSON格式)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_provider_user`(`provider` ASC, `provider_user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社交账号绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of social_account
-- ----------------------------

-- ----------------------------
-- Table structure for supplier_contact
-- ----------------------------
DROP TABLE IF EXISTS `supplier_contact`;
CREATE TABLE `supplier_contact`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID（主键，自增）',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人姓名',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认联系人：0-否，1-是',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplier_contact_supplier_id`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_supplier_contact_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of supplier_contact
-- ----------------------------

-- ----------------------------
-- Table structure for sys_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_audit_log`;
CREATE TABLE `sys_audit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operation_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `target_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `result` int NOT NULL DEFAULT 1 COMMENT '0失败 1成功',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作详情',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `sys_blacklist`;
CREATE TABLE `sys_blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `user_id` bigint NOT NULL COMMENT '被拉黑用户ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拉黑原因',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'TENANT_ADMIN' COMMENT '操作人类型(TENANT_ADMIN/SYSTEM_ADMIN)',
  `status` int NOT NULL DEFAULT 1 COMMENT '1=生效, 0=已解除',
  `added_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `removed_at` datetime NULL DEFAULT NULL,
  `removed_by` bigint NULL DEFAULT NULL,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_user`(`tenant_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '黑名单管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_blacklist
-- ----------------------------

-- ----------------------------
-- Table structure for sys_captcha
-- ----------------------------
DROP TABLE IF EXISTS `sys_captcha`;
CREATE TABLE `sys_captcha`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `captcha_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码Key',
  `code_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SHA-256哈希',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID(可选)',
  `expiry_time` datetime NOT NULL COMMENT '过期时间',
  `used` int NOT NULL DEFAULT 0 COMMENT '是否已使用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `captcha_key`(`captcha_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '验证码表(新)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_captcha
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `config_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置编码',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_type` tinyint NOT NULL DEFAULT 1 COMMENT '配置类型: 1-系统, 2-业务',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否系统内置: 0-否, 1-是',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_code_tenant`(`config_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 1, 'system.name', '系统名称', 'My Todo App', 1, '系统显示名称', 1, 1, 1, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_config` VALUES (2, 1, 'system.logo', '系统Logo', '/logo.png', 1, '系统Logo路径', 1, 1, 2, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_config` VALUES (3, 1, 'login.max_attempts', '最大登录尝试次数', '5', 1, '登录失败最大次数', 1, 1, 3, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_config` VALUES (4, 1, 'login.lock_duration', '账户锁定时长(分钟)', '30', 1, '账户锁定时长', 1, 1, 4, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_config` VALUES (5, 1, 'password.min_length', '密码最小长度', '8', 1, '密码最小长度要求', 1, 1, 5, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');

-- ----------------------------
-- Table structure for sys_data_permission_rule
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_permission_rule`;
CREATE TABLE `sys_data_permission_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `role_id` bigint NOT NULL COMMENT '关联角色ID',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `scope_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'ALL/DEPT/SELF/PROJECT/DEPT_AND_SUB',
  `table_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标表名(空=所有表)',
  `dept_column` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'dept_id' COMMENT '部门字段名',
  `user_column` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'created_by' COMMENT '用户字段名',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据权限规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_data_permission_rule
-- ----------------------------

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门ID',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门编码',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `full_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '完整路径',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '负责人用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dept_code_tenant`(`dept_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (1, 1, 0, 'XinBU', '吏部', NULL, 1, NULL, '', NULL, 0, 1, 0, 1, '2026-05-16 23:38:57', NULL, '2026-05-16 23:38:57');
INSERT INTO `sys_department` VALUES (2, 1, 0, 'HuBu', '户部', NULL, 1, NULL, '', NULL, 0, 1, 0, 1, '2026-05-16 23:40:21', NULL, '2026-05-16 23:40:21');

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `item_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典项编码',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典项名称',
  `item_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '字典项值',
  `ext_value1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展值1',
  `ext_value2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展值2',
  `ext_value3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展值3',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认: 0-否, 1-是',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_dict_type_id`(`dict_type_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (1, 1, 1, 'male', '男', '1', NULL, NULL, NULL, 1, 0, 1, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_item` VALUES (2, 1, 1, 'female', '女', '2', NULL, NULL, NULL, 1, 0, 2, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_item` VALUES (3, 1, 1, 'unknown', '未知', '0', NULL, NULL, NULL, 1, 1, 3, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_item` VALUES (4, 1, 2, 'enabled', '启用', '1', NULL, NULL, NULL, 1, 1, 1, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_item` VALUES (5, 1, 2, 'disabled', '禁用', '0', NULL, NULL, NULL, 1, 0, 2, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_item` VALUES (7, 1, 3, 'yes', '是', '1', NULL, NULL, NULL, 1, 0, 1, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_item` VALUES (8, 1, 3, 'no', '否', '0', NULL, NULL, NULL, 1, 1, 2, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否系统内置: 0-否, 1-是',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_code_tenant`(`dict_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, 1, 'gender', '性别', '用户性别', 1, 1, 1, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_type` VALUES (2, 1, 'status', '状态', '通用状态', 1, 1, 2, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_type` VALUES (3, 1, 'yes_no', '是否', '是否选项', 1, 1, 3, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_type` VALUES (4, 1, 'order_status', '订单状态', '订单状态', 1, 1, 4, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');
INSERT INTO `sys_dict_type` VALUES (5, 1, 'payment_status', '支付状态', '支付状态', 1, 1, 5, NULL, 0, NULL, '2026-04-18 19:41:39', NULL, '2026-04-18 19:41:39');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父菜单ID',
  `menu_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `menu_type` int NOT NULL COMMENT '1=目录 2=菜单 3=按钮',
  `path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端组件路径',
  `permission_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限编码',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `visible` int NOT NULL DEFAULT 1 COMMENT '0=隐藏 1=显示',
  `status` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_parameter_category
-- ----------------------------
DROP TABLE IF EXISTS `sys_parameter_category`;
CREATE TABLE `sys_parameter_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `category_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '分类类型(SYSTEM/BUSINESS)',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` int NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_code`(`tenant_id` ASC, `category_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '参数分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_parameter_category
-- ----------------------------

-- ----------------------------
-- Table structure for sys_parameter_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `sys_parameter_dictionary`;
CREATE TABLE `sys_parameter_dictionary`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `param_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名称',
  `param_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数编码',
  `param_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '参数值(JSON支持)',
  `value_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '值类型(STRING/NUMBER/BOOLEAN/JSON)',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `validation_rule` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '验证规则(正则或范围)',
  `sort_order` int NOT NULL DEFAULT 0,
  `status` int NOT NULL DEFAULT 1,
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_param_code`(`tenant_id` ASC, `param_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '参数字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_parameter_dictionary
-- ----------------------------

-- ----------------------------
-- Table structure for sys_parameter_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_parameter_item`;
CREATE TABLE `sys_parameter_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `dictionary_id` bigint NOT NULL COMMENT '参数字典ID',
  `item_label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项标签',
  `item_value` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项值',
  `sort_order` int NOT NULL DEFAULT 0,
  `status` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '参数项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_parameter_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` tinyint NOT NULL DEFAULT 1 COMMENT '类型: 1-菜单, 2-按钮, 3-API',
  `resource_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源路径',
  `http_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'HTTP方法',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见: 0-否, 1-是',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code_tenant`(`permission_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_permission_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2567 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (2217, 1, 0, 'system', '系统管理', 1, '/system', 'GET', 'Setting', 'system', NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2218, 1, 0, 'dict', '字典管理', 1, '/dict', 'GET', 'Collection', 'dict', NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2219, 1, 0, 'erp', 'ERP管理', 1, '/erp', 'GET', 'ShoppingCart', 'erp', NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2220, 1, 0, 'finance', '财务管理', 1, '/finance', 'GET', 'Money', 'finance', NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2221, 1, 2217, 'system:user', '用户管理', 1, '/api/users/get-user-page', 'GET', 'User', '/system/user', 'system/user/index', 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2222, 1, 2217, 'system:role', '角色管理', 1, '/api/roles/get-role-list', 'GET', 'UserFilled', '/system/role', 'system/role/index', 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2223, 1, 2217, 'system:permission', '权限管理', 1, '/api/permissions/get-current-user-permissions', 'GET', 'Lock', '/system/permission', 'system/permission/index', 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2224, 1, 2217, 'system:dept', '部门管理', 1, '/api/system/dept/get-department-tree', 'GET', 'OfficeBuilding', '/system/dept', 'system/dept/index', 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2228, 1, 2221, 'system:user:list', '用户列表', 2, '/api/users/get-user-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2229, 1, 2221, 'system:user:detail', '用户详情', 2, '/api/users/get-user/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2230, 1, 2221, 'system:user:create', '新增用户', 2, '/api/users/create-user', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2231, 1, 2221, 'system:user:update', '编辑用户', 2, '/api/users/update-user/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2232, 1, 2221, 'system:user:delete', '删除用户', 2, '/api/users/delete-user/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2233, 1, 2221, 'system:user:enable', '启用用户', 2, '/api/users/enable-user/{id}', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2234, 1, 2221, 'system:user:disable', '禁用用户', 2, '/api/users/disable-user/{id}', 'POST', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2235, 1, 2221, 'system:user:kick', '踢出用户', 2, '/api/auth/kick-user/{id}', 'POST', NULL, NULL, NULL, 8, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2243, 1, 2222, 'system:role:list', '角色列表', 2, '/api/roles/get-role-list', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2244, 1, 2222, 'system:role:detail', '角色详情', 2, '/api/roles/get-role/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2245, 1, 2222, 'system:role:create', '新增角色', 2, '/api/roles/create-role', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2246, 1, 2222, 'system:role:update', '编辑角色', 2, '/api/roles/update-role/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2247, 1, 2222, 'system:role:delete', '删除角色', 2, '/api/roles/delete-role/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2248, 1, 2222, 'system:role:assignToUser', '分配角色', 2, '/api/roles/assign-to-user', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2249, 1, 2222, 'system:role:assignPerm', '分配权限', 2, '/api/roles/assign-to-user', 'POST', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2250, 1, 2223, 'system:permission:list', '权限列表', 2, '/api/permissions/get-current-user-permissions', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2251, 1, 2223, 'system:permission:detail', '权限详情', 2, '/api/permissions/get-user-permissions/{userId}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2252, 1, 2223, 'system:permission:create', '新增权限', 2, '/api/permissions/create-permission', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2253, 1, 2223, 'system:permission:update', '编辑权限', 2, '/api/permissions/update-permission/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2254, 1, 2223, 'system:permission:delete', '删除权限', 2, '/api/permissions/delete-permission/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2257, 1, 2224, 'system:dept:list', '部门列表', 2, '/api/system/dept/get-department-tree', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2258, 1, 2224, 'system:dept:detail', '部门详情', 2, '/api/system/dept/get-department-tree', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2259, 1, 2224, 'system:dept:create', '新增部门', 2, '/api/system/dept/create-department', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2260, 1, 2224, 'system:dept:update', '编辑部门', 2, '/api/system/dept/update-department/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2261, 1, 2224, 'system:dept:delete', '删除部门', 2, '/api/system/dept/delete-department/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2264, 1, 2218, 'dict:type', '字典类型', 1, '/api/dict/types/get-dict-type-page', 'GET', 'Files', '/dict/type', 'dict/type/index', 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2265, 1, 2218, 'dict:config', '系统配置', 1, '/api/dict/types/get-dict-type-page', 'GET', 'Tools', '/dict/config', 'dict/config/index', 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2267, 1, 2264, 'dict:type:list', '字典类型列表', 2, '/api/dict/types/get-dict-type-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2268, 1, 2264, 'dict:type:detail', '字典类型详情', 2, '/api/dict/types/get-dict-type/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2269, 1, 2264, 'dict:type:create', '新增字典类型', 2, '/api/dict/types/create-dict-type', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2270, 1, 2264, 'dict:type:update', '编辑字典类型', 2, '/api/dict/types/update-dict-type/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2271, 1, 2264, 'dict:type:delete', '删除字典类型', 2, '/api/dict/types/delete-dict-type/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2274, 1, 2265, 'dict:config:list', '配置列表', 2, '/api/dict/config/get-config-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2275, 1, 2265, 'dict:config:detail', '配置详情', 2, '/api/dict/config/get-config/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2276, 1, 2265, 'dict:config:create', '新增配置', 2, '/api/dict/config/create-config', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2277, 1, 2265, 'dict:config:update', '编辑配置', 2, '/api/dict/config/update-config/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2278, 1, 2265, 'dict:config:delete', '删除配置', 2, '/api/dict/config/delete-config/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2281, 1, 2264, 'dict:item:list', '字典项列表', 2, '/api/dict/items/code/{dictCode}', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2282, 1, 2264, 'dict:item:create', '新增字典项', 2, '/api/dict/items/add-dict-item', 'POST', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2283, 1, 2264, 'dict:item:update', '编辑字典项', 2, '/api/dict/items/update-dict-item/{id}', 'PUT', NULL, NULL, NULL, 8, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2284, 1, 2264, 'dict:item:delete', '删除字典项', 2, '/api/dict/items/delete-dict-item/{id}', 'DELETE', NULL, NULL, NULL, 9, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2288, 1, 2218, 'dict:parameter', '参数管理', 1, '/api/parameter-dictionaries/get-dictionary-page', 'GET', 'Setting', '/dict/parameter', 'dict/parameter/index', 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2289, 1, 2288, 'dict:parameter:category:list', '参数分类列表', 2, '/api/parameter-categories/get-category-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2290, 1, 2288, 'dict:parameter:category:create', '新增参数分类', 2, '/api/parameter-categories/create-category', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2291, 1, 2288, 'dict:parameter:category:update', '编辑参数分类', 2, '/api/parameter-categories/update-category/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2292, 1, 2288, 'dict:parameter:category:delete', '删除参数分类', 2, '/api/parameter-categories/delete-category/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2293, 1, 2288, 'dict:parameter:dictionary:list', '参数字典列表', 2, '/api/parameter-dictionaries/get-dictionary-page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2294, 1, 2288, 'dict:parameter:dictionary:detail', '参数字典详情', 2, '/api/parameter-dictionaries/get-dictionary/{id}', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2295, 1, 2288, 'dict:parameter:dictionary:create', '新增参数字典', 2, '/api/parameter-dictionaries/create-dictionary', 'POST', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2296, 1, 2288, 'dict:parameter:dictionary:update', '编辑参数字典', 2, '/api/parameter-dictionaries/update-dictionary/{id}', 'PUT', NULL, NULL, NULL, 8, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2297, 1, 2288, 'dict:parameter:dictionary:delete', '删除参数字典', 2, '/api/parameter-dictionaries/delete-dictionary/{id}', 'DELETE', NULL, NULL, NULL, 9, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2298, 1, 2288, 'dict:parameter:item:list', '参数项列表', 2, '/api/parameter-items/get-item-page', 'GET', NULL, NULL, NULL, 10, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2299, 1, 2288, 'dict:parameter:item:create', '新增参数项', 2, '/api/parameter-items/create-item', 'POST', NULL, NULL, NULL, 11, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2300, 1, 2288, 'dict:parameter:item:update', '编辑参数项', 2, '/api/parameter-items/update-item/{id}', 'PUT', NULL, NULL, NULL, 12, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2301, 1, 2288, 'dict:parameter:item:delete', '删除参数项', 2, '/api/parameter-items/delete-item/{id}', 'DELETE', NULL, NULL, NULL, 13, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2304, 1, 2218, 'dict:api-market', 'API市场', 1, '/api/api-market/definitions/page', 'GET', 'Connection', '/dict/api-market', 'dict/api-market/index', 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2305, 1, 2304, 'dict:api-market:list', 'API列表', 2, '/api/api-market/definitions/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2306, 1, 2304, 'dict:api-market:detail', 'API详情', 2, '/api/api-market/definitions/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2307, 1, 2304, 'dict:api-market:create', '新增API', 2, '/api/api-market/definitions/create', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2308, 1, 2304, 'dict:api-market:update', '编辑API', 2, '/api/api-market/definitions/update/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2309, 1, 2304, 'dict:api-market:delete', '删除API', 2, '/api/api-market/definitions/delete/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2310, 1, 2304, 'dict:api-market:subscribe', '订阅API', 2, '/api/api-market/subscriptions/subscribe', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2311, 1, 2304, 'dict:api-market:unsubscribe', '取消订阅', 2, '/api/api-market/subscriptions/{id}', 'DELETE', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2312, 1, 2218, 'dict:third-party', '第三方API', 1, '/api/third-party/page', 'GET', 'Link', '/dict/third-party', 'dict/third-party/index', 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2313, 1, 2312, 'dict:third-party:list', '第三方API列表', 2, '/api/third-party/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2314, 1, 2312, 'dict:third-party:create', '新增第三方API', 2, '/api/third-party/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2315, 1, 2312, 'dict:third-party:update', '编辑第三方API', 2, '/api/third-party/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2316, 1, 2312, 'dict:third-party:delete', '删除第三方API', 2, '/api/third-party/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2317, 1, 2312, 'dict:third-party:call-logs', '调用日志', 2, '/api/third-party/call-logs/page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2318, 1, 2312, 'dict:third-party:health-check', '健康检查', 2, '/api/third-party/health-check/{id}', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2320, 1, 2218, 'dict:package', 'SaaS套餐', 1, '/api/packages/page', 'GET', 'Box', '/dict/package', 'dict/package/index', 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2321, 1, 2320, 'dict:package:list', '套餐列表', 2, '/api/packages/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2322, 1, 2320, 'dict:package:detail', '套餐详情', 2, '/api/packages/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2323, 1, 2320, 'dict:package:create', '新增套餐', 2, '/api/packages/create', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2324, 1, 2320, 'dict:package:update', '编辑套餐', 2, '/api/packages/update/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2325, 1, 2320, 'dict:package:delete', '删除套餐', 2, '/api/packages/delete/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2326, 1, 2320, 'dict:package:subscribe', '订阅套餐', 2, '/api/packages/subscribe', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2328, 1, 2218, 'dict:activity', '营销活动', 1, '/api/activities/page', 'GET', 'Present', '/dict/activity', 'dict/activity/index', 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2329, 1, 2328, 'dict:activity:list', '活动列表', 2, '/api/activities/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2330, 1, 2328, 'dict:activity:detail', '活动详情', 2, '/api/activities/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2331, 1, 2328, 'dict:activity:create', '新增活动', 2, '/api/activities/create', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2332, 1, 2328, 'dict:activity:update', '编辑活动', 2, '/api/activities/update/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2333, 1, 2328, 'dict:activity:delete', '删除活动', 2, '/api/activities/delete/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2336, 1, 2218, 'dict:trace', '追踪管理', 1, '/api/trace/configs/page', 'GET', 'View', '/dict/trace', 'dict/trace/index', 8, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2337, 1, 2336, 'dict:trace:config:list', '追踪配置列表', 2, '/api/trace/configs/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2338, 1, 2336, 'dict:trace:config:create', '新增追踪配置', 2, '/api/trace/configs/create', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2339, 1, 2336, 'dict:trace:config:update', '编辑追踪配置', 2, '/api/trace/configs/update/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2340, 1, 2336, 'dict:trace:config:delete', '删除追踪配置', 2, '/api/trace/configs/delete/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2341, 1, 2336, 'dict:trace:alert:list', '告警列表', 2, '/api/trace/alerts/page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2342, 1, 2336, 'dict:trace:alert:acknowledge', '确认告警', 2, '/api/trace/alerts/acknowledge/{id}', 'PUT', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2344, 1, 2218, 'dict:codegen', '代码生成', 1, '/api/dict/codegen/template/page', 'GET', 'DocumentCopy', '/dict/codegen', 'dict/codegen/index', 9, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2345, 1, 2344, 'dict:codegen:list', '模板列表', 2, '/api/dict/codegen/template/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2346, 1, 2344, 'dict:codegen:detail', '模板详情', 2, '/api/dict/codegen/template/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2347, 1, 2344, 'dict:codegen:create', '新增模板', 2, '/api/dict/codegen/template', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2348, 1, 2344, 'dict:codegen:update', '编辑模板', 2, '/api/dict/codegen/template/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2349, 1, 2344, 'dict:codegen:delete', '删除模板', 2, '/api/dict/codegen/template/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2350, 1, 2344, 'dict:codegen:generate', '生成代码', 2, '/api/dict/codegen/generate', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2351, 1, 2344, 'dict:codegen:history', '生成历史', 2, '/api/dict/codegen/history/page', 'GET', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2352, 1, 2218, 'dict:error-doc', '错误文档', 1, '/api/dict/error-doc/category/page', 'GET', 'Warning', '/dict/error-doc', 'dict/error-doc/index', 10, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2353, 1, 2352, 'dict:error-doc:category:list', '错误分类列表', 2, '/api/dict/error-doc/category/page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2354, 1, 2352, 'dict:error-doc:category:create', '新增错误分类', 2, '/api/dict/error-doc/category', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2355, 1, 2352, 'dict:error-doc:category:update', '编辑错误分类', 2, '/api/dict/error-doc/category/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2356, 1, 2352, 'dict:error-doc:category:delete', '删除错误分类', 2, '/api/dict/error-doc/category/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2357, 1, 2352, 'dict:error-doc:solution:list', '解决方案列表', 2, '/api/dict/error-doc/solution/page', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2358, 1, 2352, 'dict:error-doc:solution:create', '新增解决方案', 2, '/api/dict/error-doc/solution', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2359, 1, 2352, 'dict:error-doc:search', '搜索错误', 2, '/api/dict/error-doc/search', 'GET', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2360, 1, 2219, 'erp:productCategory', '商品分类', 1, '/api/erp/product-categories/get-category-page', 'GET', 'Folder', '/erp/product-category', 'erp/product-category/index', 0, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2361, 1, 2219, 'erp:product', '商品管理', 1, '/api/erp/products/get-product-page', 'GET', 'Goods', '/erp/product', 'erp/product/index', 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2362, 1, 2219, 'erp:warehouse', '仓库管理', 1, '/api/erp/warehouses/get-warehouse-page', 'GET', 'House', '/erp/warehouse', 'erp/warehouse/index', 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2363, 1, 2219, 'erp:inventory', '库存管理', 1, '/api/erp/inventory/get-inventory-page', 'GET', 'Box', '/erp/inventory', 'erp/inventory/index', 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2364, 1, 2219, 'erp:inventoryFlow', '库存流水', 1, '/api/erp/inventory/get-flow-page', 'GET', 'List', '/erp/inventory-flow', 'erp/inventory-flow/index', 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2365, 1, 2219, 'erp:supplier', '供应商管理', 1, '/api/erp/suppliers/get-supplier-page', 'GET', 'Van', '/erp/supplier', 'erp/supplier/index', 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2366, 1, 2219, 'erp:customer', '客户管理', 1, '/api/erp/customers/get-customer-page', 'GET', 'Avatar', '/erp/customer', 'erp/customer/index', 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2367, 1, 2219, 'erp:salesOrder', '销售订单', 1, '/api/erp/sales-orders/get-sales-order-page', 'GET', 'ShoppingCart', '/erp/sales-order', 'erp/sales-order/index', 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2368, 1, 2219, 'erp:inventoryCheck', '库存盘点', 1, '/api/erp/inventory-checks/get-check-page', 'GET', 'Document', '/erp/inventory-check', 'erp/inventory-check/index', 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2369, 1, 2219, 'erp:inventoryAlert', '库存预警', 1, '/api/erp/inventory/get-alert-inventories', 'GET', 'Warning', '/erp/inventory-alert', 'erp/inventory-alert/index', 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2370, 1, 2219, 'erp:purchaseOrder', '采购订单', 1, '/api/erp/purchase-orders/get-purchase-order-page', 'GET', 'ShoppingCartFull', '/erp/purchase-order', 'erp/purchase-order/index', 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2371, 1, 2219, 'erp:purchaseReturn', '采购退货', 1, '/api/erp/purchase-returns/get-return-page', 'GET', 'RefreshLeft', '/erp/purchase-return', 'erp/purchase-return/index', 8, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2372, 1, 2219, 'erp:salesReturn', '销售退货', 1, '/api/erp/sales-returns/get-return-page', 'GET', 'RefreshRight', '/erp/sales-return', 'erp/sales-return/index', 10, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2373, 1, 2219, 'erp:productPrice', '商品价格', 1, '/api/erp/product-prices/get-price-page', 'GET', 'PriceTag', '/erp/product-price', 'erp/product-price/index', 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2374, 1, 2219, 'erp:productPromotion', '商品促销', 1, '/api/erp/product-promotions/get-promotion-page', 'GET', 'Present', '/erp/product-promotion', 'erp/product-promotion/index', 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2375, 1, 2219, 'erp:salesShipment', '销售出库', 1, '/api/erp/sales-shipments/get-sales-shipment-page', 'GET', 'Van', '/erp/sales-shipment', 'erp/sales-shipment/index', 8, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2376, 1, 2219, 'erp:report', '报表统计', 1, '/api/erp/reports/dashboard', 'GET', 'DataAnalysis', '/erp/report', 'erp/report/index', 9, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2377, 1, 2219, 'erp:config', '系统配置', 1, '/api/erp/configs/get-config-page', 'GET', 'Setting', '/erp/config', 'erp/config/index', 10, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2391, 1, 2360, 'erp:productCategory:list', '分类列表', 2, '/api/erp/product-categories/get-category-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2392, 1, 2360, 'erp:productCategory:detail', '分类详情', 2, '/api/erp/product-categories/get-category/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2393, 1, 2360, 'erp:productCategory:create', '新增分类', 2, '/api/erp/product-categories/create-category', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2394, 1, 2360, 'erp:productCategory:update', '编辑分类', 2, '/api/erp/product-categories/update-category/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2395, 1, 2360, 'erp:productCategory:delete', '删除分类', 2, '/api/erp/product-categories/delete-category/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2398, 1, 2368, 'erp:inventoryCheck:list', '盘点列表', 2, '/api/erp/inventory-checks/get-check-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2399, 1, 2368, 'erp:inventoryCheck:create', '新建盘点', 2, '/api/erp/inventory-checks/create-check', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2400, 1, 2368, 'erp:inventoryCheck:submit', '提交结果', 2, '/api/erp/inventory-checks/submit-check/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2401, 1, 2368, 'erp:inventoryCheck:cancel', '取消盘点', 2, '/api/erp/inventory-checks/cancel-check/{id}', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2405, 1, 2369, 'erp:inventoryAlert:list', '预警列表', 2, '/api/erp/inventory/get-alert-inventories', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2406, 1, 2371, 'erp:purchaseReturn:list', '退货列表', 2, '/api/erp/purchase-returns/get-return-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2407, 1, 2371, 'erp:purchaseReturn:create', '新建退货', 2, '/api/erp/purchase-returns/create-return', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2408, 1, 2371, 'erp:purchaseReturn:submit', '提交审核', 2, '/api/erp/purchase-returns/submit-for-approval/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2409, 1, 2371, 'erp:purchaseReturn:approve', '审核退货', 2, '/api/erp/purchase-returns/approve-return/{id}', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2410, 1, 2371, 'erp:purchaseReturn:cancel', '取消退货', 2, '/api/erp/purchase-returns/cancel-return/{id}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2413, 1, 2372, 'erp:salesReturn:list', '退货列表', 2, '/api/erp/sales-returns/get-return-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2414, 1, 2372, 'erp:salesReturn:create', '新建退货', 2, '/api/erp/sales-returns/create-return', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2415, 1, 2372, 'erp:salesReturn:submit', '提交审核', 2, '/api/erp/sales-returns/submit-for-approval/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2416, 1, 2372, 'erp:salesReturn:approve', '审核退货', 2, '/api/erp/sales-returns/approve-return/{id}', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2417, 1, 2372, 'erp:salesReturn:cancel', '取消退货', 2, '/api/erp/sales-returns/cancel-return/{id}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2420, 1, 2373, 'erp:productPrice:list', '价格列表', 2, '/api/erp/product-prices/get-price-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2421, 1, 2373, 'erp:productPrice:create', '新增价格', 2, '/api/erp/product-prices/create-price', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2422, 1, 2373, 'erp:productPrice:update', '编辑价格', 2, '/api/erp/product-prices/update-price/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2423, 1, 2373, 'erp:productPrice:delete', '删除价格', 2, '/api/erp/product-prices/delete-price/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2427, 1, 2374, 'erp:productPromotion:list', '促销列表', 2, '/api/erp/product-promotions/get-promotion-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2428, 1, 2374, 'erp:productPromotion:create', '新增促销', 2, '/api/erp/product-promotions/create-promotion', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2429, 1, 2374, 'erp:productPromotion:update', '编辑促销', 2, '/api/erp/product-promotions/update-promotion/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2430, 1, 2374, 'erp:productPromotion:delete', '删除促销', 2, '/api/erp/product-promotions/delete-promotion/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2431, 1, 2374, 'erp:productPromotion:enable', '启用促销', 2, '/api/erp/product-promotions/enable-promotion/{id}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2432, 1, 2374, 'erp:productPromotion:disable', '停用促销', 2, '/api/erp/product-promotions/disable-promotion/{id}', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2434, 1, 2361, 'erp:product:list', '商品列表', 2, '/api/erp/products/get-product-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2435, 1, 2361, 'erp:product:detail', '商品详情', 2, '/api/erp/products/get-product/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2436, 1, 2361, 'erp:product:create', '新增商品', 2, '/api/erp/products/create-product', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2437, 1, 2361, 'erp:product:update', '编辑商品', 2, '/api/erp/products/update-product/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2438, 1, 2361, 'erp:product:delete', '删除商品', 2, '/api/erp/products/delete-product/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2441, 1, 2362, 'erp:warehouse:list', '仓库列表', 2, '/api/erp/warehouses/get-warehouse-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2442, 1, 2362, 'erp:warehouse:detail', '仓库详情', 2, '/api/erp/warehouses/get-warehouse/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2443, 1, 2362, 'erp:warehouse:create', '新增仓库', 2, '/api/erp/warehouses/create-warehouse', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2444, 1, 2362, 'erp:warehouse:update', '编辑仓库', 2, '/api/erp/warehouses/update-warehouse/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2445, 1, 2362, 'erp:warehouse:delete', '删除仓库', 2, '/api/erp/warehouses/delete-warehouse/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2446, 1, 2362, 'erp:warehouse:setDefault', '设为默认', 2, '/api/erp/warehouses/get-default-warehouse', 'GET', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2448, 1, 2363, 'erp:inventory:list', '库存列表', 2, '/api/erp/inventory/get-inventory-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2449, 1, 2363, 'erp:inventory:query', '查询库存', 2, '/api/erp/inventory/get-inventory', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2450, 1, 2363, 'erp:inventory:inbound', '入库操作', 2, '/api/erp/inventory/inbound', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2451, 1, 2363, 'erp:inventory:outbound', '出库操作', 2, '/api/erp/inventory/outbound', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2452, 1, 2363, 'erp:inventory:alert', '库存预警', 2, '/api/erp/inventory/get-alert-inventories', 'GET', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2455, 1, 2365, 'erp:supplier:list', '供应商列表', 2, '/api/erp/suppliers/get-supplier-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2456, 1, 2365, 'erp:supplier:detail', '供应商详情', 2, '/api/erp/suppliers/get-supplier/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2457, 1, 2365, 'erp:supplier:create', '新增供应商', 2, '/api/erp/suppliers/create-supplier', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2458, 1, 2365, 'erp:supplier:update', '编辑供应商', 2, '/api/erp/suppliers/update-supplier/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2459, 1, 2365, 'erp:supplier:delete', '删除供应商', 2, '/api/erp/suppliers/delete-supplier/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2462, 1, 2366, 'erp:customer:list', '客户列表', 2, '/api/erp/customers/get-customer-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2463, 1, 2366, 'erp:customer:detail', '客户详情', 2, '/api/erp/customers/get-customer/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2464, 1, 2366, 'erp:customer:create', '新增客户', 2, '/api/erp/customers/create-customer', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2465, 1, 2366, 'erp:customer:update', '编辑客户', 2, '/api/erp/customers/update-customer/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2466, 1, 2366, 'erp:customer:delete', '删除客户', 2, '/api/erp/customers/delete-customer/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2469, 1, 2370, 'erp:purchaseOrder:list', '订单列表', 2, '/api/erp/purchase-orders/get-purchase-order-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2470, 1, 2370, 'erp:purchaseOrder:detail', '订单详情', 2, '/api/erp/purchase-orders/get-purchase-order/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2471, 1, 2370, 'erp:purchaseOrder:create', '新建订单', 2, '/api/erp/purchase-orders/create-purchase-order', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2472, 1, 2370, 'erp:purchaseOrder:update', '编辑订单', 2, '/api/erp/purchase-orders/update-purchase-order/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2473, 1, 2370, 'erp:purchaseOrder:submit', '提交审核', 2, '/api/erp/purchase-orders/submit-for-approval/{id}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2474, 1, 2370, 'erp:purchaseOrder:approve', '审核订单', 2, '/api/erp/purchase-orders/approve-order/{id}', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2475, 1, 2370, 'erp:purchaseOrder:cancel', '取消订单', 2, '/api/erp/purchase-orders/cancel-order/{id}', 'POST', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2476, 1, 2367, 'erp:salesOrder:list', '订单列表', 2, '/api/erp/sales-orders/get-sales-order-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2477, 1, 2367, 'erp:salesOrder:detail', '订单详情', 2, '/api/erp/sales-orders/get-sales-order/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2478, 1, 2367, 'erp:salesOrder:create', '新建订单', 2, '/api/erp/sales-orders/create-sales-order', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2479, 1, 2367, 'erp:salesOrder:update', '编辑订单', 2, '/api/erp/sales-orders/update-sales-order/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2480, 1, 2367, 'erp:salesOrder:submit', '提交审核', 2, '/api/erp/sales-orders/submit-for-approval/{id}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2481, 1, 2367, 'erp:salesOrder:approve', '审核订单', 2, '/api/erp/sales-orders/approve-order/{id}', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2482, 1, 2367, 'erp:salesOrder:cancel', '取消订单', 2, '/api/erp/sales-orders/cancel-order/{id}', 'POST', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2483, 1, 2375, 'erp:salesShipment:list', '出库单列表', 2, '/api/erp/sales-shipments/get-sales-shipment-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2484, 1, 2375, 'erp:salesShipment:detail', '出库单详情', 2, '/api/erp/sales-shipments/get-sales-shipment/{id}', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2485, 1, 2375, 'erp:salesShipment:shippable', '可发货商品', 2, '/api/erp/sales-shipments/get-shippable-items/{orderId}', 'GET', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2486, 1, 2375, 'erp:salesShipment:create', '新建出库单', 2, '/api/erp/sales-shipments/create-sales-shipment', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2487, 1, 2375, 'erp:salesShipment:approve', '审核出库', 2, '/api/erp/sales-shipments/approve-shipment/{id}', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2488, 1, 2375, 'erp:salesShipment:cancel', '取消出库', 2, '/api/erp/sales-shipments/cancel-shipment/{id}', 'POST', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2490, 1, 2220, 'finance:receivable', '应收账款', 1, '/api/finance/receivables/get-receivable-page', 'GET', 'CreditCard', '/finance/receivable', 'finance/receivable/index', 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2491, 1, 2220, 'finance:payable', '应付账款', 1, '/api/finance/payables/get-payable-page', 'GET', 'Wallet', '/finance/payable', 'finance/payable/index', 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2492, 1, 2220, 'finance:record', '收支记录', 1, '/api/finance/records/get-record-page', 'GET', 'Tickets', '/finance/record', 'finance/record/index', 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2493, 1, 2220, 'finance:bankAccount', '银行账户', 1, '/api/finance/bank-accounts/get-bank-account-page', 'GET', 'Postcard', '/finance/bank-account', 'finance/bank-account/index', 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2497, 1, 2490, 'finance:receivable:list', '应收列表', 2, '/api/finance/receivables/get-receivable-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2498, 1, 2490, 'finance:receivable:create', '新增应收', 2, '/api/finance/receivables/create-receivable', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2499, 1, 2490, 'finance:receivable:receive', '收款确认', 2, '/api/finance/receivables/receive-payment/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2500, 1, 2490, 'finance:receivable:overdue', '逾期应收', 2, '/api/finance/receivables/get-overdue-receivables', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2504, 1, 2491, 'finance:payable:list', '应付列表', 2, '/api/finance/payables/get-payable-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2505, 1, 2491, 'finance:payable:create', '新增应付', 2, '/api/finance/payables/create-payable', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2506, 1, 2491, 'finance:payable:pay', '付款确认', 2, '/api/finance/payables/make-payment/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2507, 1, 2491, 'finance:payable:overdue', '逾期应付', 2, '/api/finance/payables/get-overdue-payables', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2511, 1, 2492, 'finance:record:list', '记录列表', 2, '/api/finance/records/get-record-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2512, 1, 2492, 'finance:record:create', '新增记录', 2, '/api/finance/records/create-record', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2513, 1, 2492, 'finance:record:approve', '审核记录', 2, '/api/finance/records/approve-record/{id}', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2514, 1, 2492, 'finance:record:cancel', '取消记录', 2, '/api/finance/records/cancel-record/{id}', 'POST', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2518, 1, 2493, 'finance:bankAccount:list', '账户列表', 2, '/api/finance/bank-accounts/get-bank-account-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2519, 1, 2493, 'finance:bankAccount:all', '所有账户', 2, '/api/finance/bank-accounts/get-all-bank-accounts', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2520, 1, 2493, 'finance:bankAccount:create', '新增账户', 2, '/api/finance/bank-accounts/create-bank-account', 'POST', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2521, 1, 2493, 'finance:bankAccount:update', '编辑账户', 2, '/api/finance/bank-accounts/update-bank-account/{id}', 'PUT', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2522, 1, 2493, 'finance:bankAccount:delete', '删除账户', 2, '/api/finance/bank-accounts/delete-bank-account/{id}', 'DELETE', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2525, 1, 2376, 'erp:report:dashboard', 'Dashboard', 2, '/api/erp/reports/dashboard', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2526, 1, 2376, 'erp:report:sales', '销售报表', 2, '/api/erp/reports/sales', 'GET', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2527, 1, 2376, 'erp:report:purchase', '采购报表', 2, '/api/erp/reports/purchase', 'GET', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2528, 1, 2376, 'erp:report:inventory', '库存报表', 2, '/api/erp/reports/inventory', 'GET', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2532, 1, 2377, 'erp:config:list', '配置列表', 2, '/api/erp/configs/get-config-page', 'GET', NULL, NULL, NULL, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2533, 1, 2377, 'erp:config:create', '新增配置', 2, '/api/erp/configs/create-config', 'POST', NULL, NULL, NULL, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2534, 1, 2377, 'erp:config:update', '编辑配置', 2, '/api/erp/configs/update-config/{id}', 'PUT', NULL, NULL, NULL, 3, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2535, 1, 2377, 'erp:config:delete', '删除配置', 2, '/api/erp/configs/delete-config/{id}', 'DELETE', NULL, NULL, NULL, 4, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2536, 1, 2377, 'erp:config:batchUpdate', '批量更新', 2, '/api/erp/configs/batch-update', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_permission` VALUES (2546, 1, 2336, 'dict:trace:alert:create', '创建告警', 2, '/api/trace/alerts/create', 'POST', NULL, NULL, NULL, 5, 1, 1, 0, NULL, '2026-06-01 17:59:41', NULL, '2026-06-01 17:59:41');
INSERT INTO `sys_permission` VALUES (2547, 1, 2336, 'dict:trace:alert:update', '更新告警', 2, '/api/trace/alerts/update/{id}', 'PUT', NULL, NULL, NULL, 6, 1, 1, 0, NULL, '2026-06-01 17:59:41', NULL, '2026-06-01 17:59:41');
INSERT INTO `sys_permission` VALUES (2548, 1, 2336, 'dict:trace:alert:delete', '删除告警', 2, '/api/trace/alerts/delete/{id}', 'DELETE', NULL, NULL, NULL, 7, 1, 1, 0, NULL, '2026-06-01 17:59:41', NULL, '2026-06-01 17:59:41');

-- ----------------------------
-- Table structure for sys_permission_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission_log`;
CREATE TABLE `sys_permission_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '被操作用户ID',
  `resource` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资源标识',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `permission` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限编码',
  `result` int NOT NULL DEFAULT 0 COMMENT '结果(0拒绝 1通过)',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原因',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission_template`;
CREATE TABLE `sys_permission_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '0=系统预设',
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_system` int NOT NULL DEFAULT 0 COMMENT '1=系统预设',
  `permission_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '权限ID列表(JSON数组)',
  `status` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_code`(`tenant_id` ASC, `template_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission_template
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父角色ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `data_scope` tinyint NOT NULL DEFAULT 1 COMMENT '数据权限范围: 1-全部, 2-本部门, 3-本人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否系统角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code_tenant`(`role_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sys_role_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (58, 1, 'SUPER_ADMIN', '超级管理员', '拥有全部系统权限', 0, 1, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15', 0);
INSERT INTO `sys_role` VALUES (59, 1, 'ADMIN', '管理员', '拥有管理权限', 0, 2, 1, 1, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15', 0);
INSERT INTO `sys_role` VALUES (60, 1, 'USER', '普通用户', '基础用户权限', 0, 3, 1, 3, 0, NULL, '2026-06-01 17:54:15', NULL, '2026-06-01 17:54:15', 0);

-- ----------------------------
-- Table structure for sys_role_inheritance
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_inheritance`;
CREATE TABLE `sys_role_inheritance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `child_role_id` bigint NOT NULL COMMENT '子角色ID',
  `parent_role_id` bigint NOT NULL COMMENT '父角色ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_inheritance`(`child_role_id` ASC, `parent_role_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_child_role`(`child_role_id` ASC) USING BTREE,
  INDEX `idx_parent_role`(`parent_role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色继承关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_inheritance
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE,
  INDEX `idx_role_permission_role`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8921 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (4925, 0, 1, 1814, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4926, 0, 1, 1815, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4927, 0, 1, 1816, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4928, 0, 1, 1817, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4929, 0, 1, 1818, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4930, 0, 1, 1819, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4931, 0, 1, 1820, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4932, 0, 1, 1821, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4933, 0, 1, 1822, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4934, 0, 1, 1823, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4935, 0, 1, 1824, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4936, 0, 1, 1825, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4937, 0, 1, 1826, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4938, 0, 1, 1827, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4939, 0, 1, 1828, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4940, 0, 1, 1829, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4941, 0, 1, 1830, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4942, 0, 1, 1831, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4943, 0, 1, 1832, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4944, 0, 1, 1833, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4945, 0, 1, 1834, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4946, 0, 1, 1835, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4947, 0, 1, 1836, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4948, 0, 1, 1837, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4949, 0, 1, 1838, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4950, 0, 1, 1839, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4951, 0, 1, 1840, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4952, 0, 1, 1841, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4953, 0, 1, 1842, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4954, 0, 1, 1843, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4955, 0, 1, 1844, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4956, 0, 1, 1845, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4957, 0, 1, 1846, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4958, 0, 1, 1847, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4959, 0, 1, 1848, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4960, 0, 1, 1849, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4961, 0, 1, 1850, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4962, 0, 1, 1851, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4963, 0, 1, 1852, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4964, 0, 1, 1853, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4965, 0, 1, 1854, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4966, 0, 1, 1855, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4967, 0, 1, 1856, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4968, 0, 1, 1857, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4969, 0, 1, 1858, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4970, 0, 1, 1859, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4971, 0, 1, 1860, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4972, 0, 1, 1861, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4973, 0, 1, 1862, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4974, 0, 1, 1863, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4975, 0, 1, 1864, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4976, 0, 1, 1865, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4977, 0, 1, 1866, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4978, 0, 1, 1867, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4979, 0, 1, 1868, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4980, 0, 1, 1869, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4981, 0, 1, 1870, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4982, 0, 1, 1871, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4983, 0, 1, 1872, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4984, 0, 1, 1873, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4985, 0, 1, 1874, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4986, 0, 1, 1875, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4987, 0, 1, 1876, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4988, 0, 1, 1877, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4989, 0, 1, 1878, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4990, 0, 1, 1879, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4991, 0, 1, 1880, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4992, 0, 1, 1881, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4993, 0, 1, 1882, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4994, 0, 1, 1883, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4995, 0, 1, 1884, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4996, 0, 1, 1885, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4997, 0, 1, 1886, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4998, 0, 1, 1887, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (4999, 0, 1, 1888, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5000, 0, 1, 1889, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5001, 0, 1, 1890, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5002, 0, 1, 1891, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5003, 0, 1, 1892, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5004, 0, 1, 1893, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5005, 0, 1, 1894, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5006, 0, 1, 1895, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5007, 0, 1, 1896, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5008, 0, 1, 1897, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5009, 0, 1, 1898, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5010, 0, 1, 1899, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5011, 0, 1, 1900, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5012, 0, 1, 1901, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5013, 0, 1, 1902, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5014, 0, 1, 1903, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5015, 0, 1, 1904, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5016, 0, 1, 1905, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5017, 0, 1, 1906, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5018, 0, 1, 1907, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5019, 0, 1, 1908, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5020, 0, 1, 1909, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5021, 0, 1, 1910, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5022, 0, 1, 1911, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5023, 0, 1, 1912, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5024, 0, 1, 1913, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5025, 0, 1, 1914, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5026, 0, 1, 1915, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5027, 0, 1, 1916, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5028, 0, 1, 1917, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5029, 0, 1, 1918, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5030, 0, 1, 1919, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5031, 0, 1, 1920, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5032, 0, 1, 1921, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5033, 0, 1, 1922, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5034, 0, 1, 1923, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5035, 0, 1, 1924, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5036, 0, 1, 1925, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5037, 0, 1, 1926, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5038, 0, 1, 1927, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5039, 0, 1, 1928, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5040, 0, 1, 1929, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5041, 0, 1, 1930, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5042, 0, 1, 1931, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5043, 0, 1, 1932, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5044, 0, 1, 1933, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5045, 0, 1, 1934, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5046, 0, 1, 1935, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5047, 0, 1, 1936, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5048, 0, 1, 1937, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5049, 0, 1, 1938, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5050, 0, 1, 1939, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5051, 0, 1, 1940, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5052, 0, 1, 1941, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5053, 0, 1, 1942, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5054, 0, 1, 1943, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5055, 0, 1, 1944, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5056, 0, 1, 1945, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5057, 0, 1, 1946, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5058, 0, 1, 1947, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5059, 0, 1, 1948, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5060, 0, 1, 1949, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5061, 0, 1, 1950, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5062, 0, 1, 1951, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5063, 0, 1, 1952, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5064, 0, 1, 1953, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5065, 0, 1, 1954, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5066, 0, 1, 1955, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5067, 0, 1, 1956, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5068, 0, 1, 1957, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5069, 0, 1, 1958, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5070, 0, 1, 1959, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (5071, 0, 1, 1960, NULL, '2026-05-23 23:18:51');
INSERT INTO `sys_role_permission` VALUES (6607, 0, 1, 1961, NULL, '2026-05-25 23:24:23');
INSERT INTO `sys_role_permission` VALUES (8103, 1, 58, 2217, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8104, 1, 58, 2218, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8105, 1, 58, 2219, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8106, 1, 58, 2220, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8107, 1, 58, 2221, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8108, 1, 58, 2222, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8109, 1, 58, 2223, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8110, 1, 58, 2224, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8111, 1, 58, 2228, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8112, 1, 58, 2229, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8113, 1, 58, 2230, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8114, 1, 58, 2231, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8115, 1, 58, 2232, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8116, 1, 58, 2233, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8117, 1, 58, 2234, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8118, 1, 58, 2235, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8119, 1, 58, 2243, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8120, 1, 58, 2244, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8121, 1, 58, 2245, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8122, 1, 58, 2246, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8123, 1, 58, 2247, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8124, 1, 58, 2248, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8125, 1, 58, 2249, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8126, 1, 58, 2250, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8127, 1, 58, 2251, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8128, 1, 58, 2252, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8129, 1, 58, 2253, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8130, 1, 58, 2254, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8131, 1, 58, 2257, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8132, 1, 58, 2258, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8133, 1, 58, 2259, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8134, 1, 58, 2260, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8135, 1, 58, 2261, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8136, 1, 58, 2264, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8137, 1, 58, 2265, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8138, 1, 58, 2267, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8139, 1, 58, 2268, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8140, 1, 58, 2269, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8141, 1, 58, 2270, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8142, 1, 58, 2271, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8143, 1, 58, 2274, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8144, 1, 58, 2275, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8145, 1, 58, 2276, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8146, 1, 58, 2277, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8147, 1, 58, 2278, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8148, 1, 58, 2281, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8149, 1, 58, 2282, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8150, 1, 58, 2283, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8151, 1, 58, 2284, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8152, 1, 58, 2288, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8153, 1, 58, 2289, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8154, 1, 58, 2290, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8155, 1, 58, 2291, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8156, 1, 58, 2292, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8157, 1, 58, 2293, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8158, 1, 58, 2294, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8159, 1, 58, 2295, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8160, 1, 58, 2296, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8161, 1, 58, 2297, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8162, 1, 58, 2298, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8163, 1, 58, 2299, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8164, 1, 58, 2300, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8165, 1, 58, 2301, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8166, 1, 58, 2304, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8167, 1, 58, 2305, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8168, 1, 58, 2306, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8169, 1, 58, 2307, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8170, 1, 58, 2308, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8171, 1, 58, 2309, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8172, 1, 58, 2310, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8173, 1, 58, 2311, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8174, 1, 58, 2312, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8175, 1, 58, 2313, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8176, 1, 58, 2314, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8177, 1, 58, 2315, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8178, 1, 58, 2316, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8179, 1, 58, 2317, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8180, 1, 58, 2318, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8181, 1, 58, 2320, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8182, 1, 58, 2321, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8183, 1, 58, 2322, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8184, 1, 58, 2323, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8185, 1, 58, 2324, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8186, 1, 58, 2325, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8187, 1, 58, 2326, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8188, 1, 58, 2328, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8189, 1, 58, 2329, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8190, 1, 58, 2330, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8191, 1, 58, 2331, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8192, 1, 58, 2332, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8193, 1, 58, 2333, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8194, 1, 58, 2336, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8195, 1, 58, 2337, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8196, 1, 58, 2338, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8197, 1, 58, 2339, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8198, 1, 58, 2340, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8199, 1, 58, 2341, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8200, 1, 58, 2342, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8201, 1, 58, 2344, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8202, 1, 58, 2345, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8203, 1, 58, 2346, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8204, 1, 58, 2347, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8205, 1, 58, 2348, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8206, 1, 58, 2349, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8207, 1, 58, 2350, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8208, 1, 58, 2351, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8209, 1, 58, 2352, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8210, 1, 58, 2353, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8211, 1, 58, 2354, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8212, 1, 58, 2355, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8213, 1, 58, 2356, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8214, 1, 58, 2357, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8215, 1, 58, 2358, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8216, 1, 58, 2359, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8217, 1, 58, 2360, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8218, 1, 58, 2361, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8219, 1, 58, 2362, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8220, 1, 58, 2363, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8221, 1, 58, 2364, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8222, 1, 58, 2365, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8223, 1, 58, 2366, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8224, 1, 58, 2367, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8225, 1, 58, 2368, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8226, 1, 58, 2369, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8227, 1, 58, 2370, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8228, 1, 58, 2371, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8229, 1, 58, 2372, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8230, 1, 58, 2373, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8231, 1, 58, 2374, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8232, 1, 58, 2375, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8233, 1, 58, 2376, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8234, 1, 58, 2377, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8235, 1, 58, 2391, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8236, 1, 58, 2392, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8237, 1, 58, 2393, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8238, 1, 58, 2394, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8239, 1, 58, 2395, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8240, 1, 58, 2398, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8241, 1, 58, 2399, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8242, 1, 58, 2400, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8243, 1, 58, 2401, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8244, 1, 58, 2405, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8245, 1, 58, 2406, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8246, 1, 58, 2407, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8247, 1, 58, 2408, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8248, 1, 58, 2409, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8249, 1, 58, 2410, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8250, 1, 58, 2413, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8251, 1, 58, 2414, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8252, 1, 58, 2415, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8253, 1, 58, 2416, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8254, 1, 58, 2417, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8255, 1, 58, 2420, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8256, 1, 58, 2421, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8257, 1, 58, 2422, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8258, 1, 58, 2423, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8259, 1, 58, 2427, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8260, 1, 58, 2428, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8261, 1, 58, 2429, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8262, 1, 58, 2430, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8263, 1, 58, 2431, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8264, 1, 58, 2432, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8265, 1, 58, 2434, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8266, 1, 58, 2435, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8267, 1, 58, 2436, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8268, 1, 58, 2437, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8269, 1, 58, 2438, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8270, 1, 58, 2441, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8271, 1, 58, 2442, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8272, 1, 58, 2443, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8273, 1, 58, 2444, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8274, 1, 58, 2445, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8275, 1, 58, 2446, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8276, 1, 58, 2448, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8277, 1, 58, 2449, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8278, 1, 58, 2450, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8279, 1, 58, 2451, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8280, 1, 58, 2452, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8281, 1, 58, 2455, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8282, 1, 58, 2456, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8283, 1, 58, 2457, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8284, 1, 58, 2458, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8285, 1, 58, 2459, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8286, 1, 58, 2462, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8287, 1, 58, 2463, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8288, 1, 58, 2464, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8289, 1, 58, 2465, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8290, 1, 58, 2466, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8291, 1, 58, 2469, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8292, 1, 58, 2470, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8293, 1, 58, 2471, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8294, 1, 58, 2472, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8295, 1, 58, 2473, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8296, 1, 58, 2474, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8297, 1, 58, 2475, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8298, 1, 58, 2476, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8299, 1, 58, 2477, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8300, 1, 58, 2478, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8301, 1, 58, 2479, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8302, 1, 58, 2480, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8303, 1, 58, 2481, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8304, 1, 58, 2482, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8305, 1, 58, 2483, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8306, 1, 58, 2484, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8307, 1, 58, 2485, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8308, 1, 58, 2486, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8309, 1, 58, 2487, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8310, 1, 58, 2488, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8311, 1, 58, 2490, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8312, 1, 58, 2491, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8313, 1, 58, 2492, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8314, 1, 58, 2493, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8315, 1, 58, 2497, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8316, 1, 58, 2498, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8317, 1, 58, 2499, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8318, 1, 58, 2500, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8319, 1, 58, 2504, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8320, 1, 58, 2505, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8321, 1, 58, 2506, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8322, 1, 58, 2507, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8323, 1, 58, 2511, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8324, 1, 58, 2512, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8325, 1, 58, 2513, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8326, 1, 58, 2514, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8327, 1, 58, 2518, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8328, 1, 58, 2519, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8329, 1, 58, 2520, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8330, 1, 58, 2521, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8331, 1, 58, 2522, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8332, 1, 58, 2525, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8333, 1, 58, 2526, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8334, 1, 58, 2527, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8335, 1, 58, 2528, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8336, 1, 58, 2532, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8337, 1, 58, 2533, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8338, 1, 58, 2534, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8339, 1, 58, 2535, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8340, 1, 58, 2536, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8613, 1, 60, 2217, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8614, 1, 60, 2218, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8615, 1, 60, 2219, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8616, 1, 60, 2220, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8617, 1, 60, 2221, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8618, 1, 60, 2222, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8619, 1, 60, 2223, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8620, 1, 60, 2224, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8621, 1, 60, 2264, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8622, 1, 60, 2265, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8623, 1, 60, 2288, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8624, 1, 60, 2304, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8625, 1, 60, 2312, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8626, 1, 60, 2320, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8627, 1, 60, 2328, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8628, 1, 60, 2336, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8629, 1, 60, 2344, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8630, 1, 60, 2352, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8631, 1, 60, 2360, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8632, 1, 60, 2361, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8633, 1, 60, 2362, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8634, 1, 60, 2363, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8635, 1, 60, 2364, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8636, 1, 60, 2365, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8637, 1, 60, 2366, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8638, 1, 60, 2367, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8639, 1, 60, 2368, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8640, 1, 60, 2369, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8641, 1, 60, 2370, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8642, 1, 60, 2371, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8643, 1, 60, 2372, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8644, 1, 60, 2373, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8645, 1, 60, 2374, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8646, 1, 60, 2375, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8647, 1, 60, 2376, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8648, 1, 60, 2377, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8649, 1, 60, 2490, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8650, 1, 60, 2491, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8651, 1, 60, 2492, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8652, 1, 60, 2493, NULL, '2026-06-01 17:54:15');
INSERT INTO `sys_role_permission` VALUES (8676, 1, 58, 2546, NULL, '2026-06-01 17:59:41');
INSERT INTO `sys_role_permission` VALUES (8677, 1, 58, 2548, NULL, '2026-06-01 17:59:41');
INSERT INTO `sys_role_permission` VALUES (8678, 1, 58, 2547, NULL, '2026-06-01 17:59:41');
INSERT INTO `sys_role_permission` VALUES (8679, 1, 59, 2217, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8680, 1, 59, 2221, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8681, 1, 59, 2228, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8682, 1, 59, 2229, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8683, 1, 59, 2230, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8684, 1, 59, 2231, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8685, 1, 59, 2232, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8686, 1, 59, 2233, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8687, 1, 59, 2234, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8688, 1, 59, 2235, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8689, 1, 59, 2222, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8690, 1, 59, 2243, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8691, 1, 59, 2244, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8692, 1, 59, 2245, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8693, 1, 59, 2246, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8694, 1, 59, 2247, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8695, 1, 59, 2248, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8696, 1, 59, 2249, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8697, 1, 59, 2223, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8698, 1, 59, 2250, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8699, 1, 59, 2251, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8700, 1, 59, 2252, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8701, 1, 59, 2253, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8702, 1, 59, 2254, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8703, 1, 59, 2224, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8704, 1, 59, 2257, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8705, 1, 59, 2258, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8706, 1, 59, 2259, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8707, 1, 59, 2260, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8708, 1, 59, 2261, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8709, 1, 59, 2218, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8710, 1, 59, 2264, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8711, 1, 59, 2267, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8712, 1, 59, 2268, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8713, 1, 59, 2269, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8714, 1, 59, 2270, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8715, 1, 59, 2271, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8716, 1, 59, 2281, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8717, 1, 59, 2282, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8718, 1, 59, 2283, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8719, 1, 59, 2284, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8720, 1, 59, 2265, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8721, 1, 59, 2274, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8722, 1, 59, 2275, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8723, 1, 59, 2276, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8724, 1, 59, 2277, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8725, 1, 59, 2278, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8726, 1, 59, 2288, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8727, 1, 59, 2289, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8728, 1, 59, 2290, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8729, 1, 59, 2291, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8730, 1, 59, 2292, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8731, 1, 59, 2293, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8732, 1, 59, 2294, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8733, 1, 59, 2295, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8734, 1, 59, 2296, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8735, 1, 59, 2297, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8736, 1, 59, 2298, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8737, 1, 59, 2299, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8738, 1, 59, 2300, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8739, 1, 59, 2301, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8740, 1, 59, 2304, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8741, 1, 59, 2305, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8742, 1, 59, 2306, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8743, 1, 59, 2307, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8744, 1, 59, 2308, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8745, 1, 59, 2309, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8746, 1, 59, 2310, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8747, 1, 59, 2311, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8748, 1, 59, 2312, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8749, 1, 59, 2313, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8750, 1, 59, 2314, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8751, 1, 59, 2315, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8752, 1, 59, 2316, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8753, 1, 59, 2317, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8754, 1, 59, 2318, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8755, 1, 59, 2320, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8756, 1, 59, 2321, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8757, 1, 59, 2322, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8758, 1, 59, 2323, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8759, 1, 59, 2324, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8760, 1, 59, 2325, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8761, 1, 59, 2326, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8762, 1, 59, 2328, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8763, 1, 59, 2329, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8764, 1, 59, 2330, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8765, 1, 59, 2331, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8766, 1, 59, 2332, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8767, 1, 59, 2333, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8768, 1, 59, 2336, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8769, 1, 59, 2337, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8770, 1, 59, 2338, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8771, 1, 59, 2339, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8772, 1, 59, 2340, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8773, 1, 59, 2341, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8774, 1, 59, 2546, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8775, 1, 59, 2342, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8776, 1, 59, 2547, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8777, 1, 59, 2548, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8778, 1, 59, 2344, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8779, 1, 59, 2345, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8780, 1, 59, 2346, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8781, 1, 59, 2347, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8782, 1, 59, 2348, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8783, 1, 59, 2349, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8784, 1, 59, 2350, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8785, 1, 59, 2351, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8786, 1, 59, 2352, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8787, 1, 59, 2353, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8788, 1, 59, 2354, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8789, 1, 59, 2355, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8790, 1, 59, 2356, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8791, 1, 59, 2357, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8792, 1, 59, 2358, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8793, 1, 59, 2359, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8794, 1, 59, 2219, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8795, 1, 59, 2360, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8796, 1, 59, 2391, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8797, 1, 59, 2392, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8798, 1, 59, 2393, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8799, 1, 59, 2394, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8800, 1, 59, 2395, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8801, 1, 59, 2361, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8802, 1, 59, 2434, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8803, 1, 59, 2435, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8804, 1, 59, 2436, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8805, 1, 59, 2437, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8806, 1, 59, 2438, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8807, 1, 59, 2362, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8808, 1, 59, 2441, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8809, 1, 59, 2442, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8810, 1, 59, 2443, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8811, 1, 59, 2444, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8812, 1, 59, 2445, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8813, 1, 59, 2446, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8814, 1, 59, 2373, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8815, 1, 59, 2420, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8816, 1, 59, 2421, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8817, 1, 59, 2422, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8818, 1, 59, 2423, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8819, 1, 59, 2363, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8820, 1, 59, 2448, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8821, 1, 59, 2449, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8822, 1, 59, 2450, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8823, 1, 59, 2451, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8824, 1, 59, 2452, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8825, 1, 59, 2374, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8826, 1, 59, 2427, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8827, 1, 59, 2428, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8828, 1, 59, 2429, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8829, 1, 59, 2430, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8830, 1, 59, 2431, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8831, 1, 59, 2432, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8832, 1, 59, 2364, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8833, 1, 59, 2365, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8834, 1, 59, 2455, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8835, 1, 59, 2456, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8836, 1, 59, 2457, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8837, 1, 59, 2458, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8838, 1, 59, 2459, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8839, 1, 59, 2368, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8840, 1, 59, 2398, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8841, 1, 59, 2399, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8842, 1, 59, 2400, 1, '2026-06-01 18:05:53');
INSERT INTO `sys_role_permission` VALUES (8843, 1, 59, 2401, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8844, 1, 59, 2366, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8845, 1, 59, 2462, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8846, 1, 59, 2463, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8847, 1, 59, 2464, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8848, 1, 59, 2465, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8849, 1, 59, 2466, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8850, 1, 59, 2369, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8851, 1, 59, 2405, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8852, 1, 59, 2367, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8853, 1, 59, 2476, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8854, 1, 59, 2477, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8855, 1, 59, 2478, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8856, 1, 59, 2479, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8857, 1, 59, 2480, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8858, 1, 59, 2481, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8859, 1, 59, 2482, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8860, 1, 59, 2370, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8861, 1, 59, 2469, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8862, 1, 59, 2470, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8863, 1, 59, 2471, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8864, 1, 59, 2472, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8865, 1, 59, 2473, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8866, 1, 59, 2474, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8867, 1, 59, 2475, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8868, 1, 59, 2371, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8869, 1, 59, 2406, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8870, 1, 59, 2407, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8871, 1, 59, 2408, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8872, 1, 59, 2409, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8873, 1, 59, 2410, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8874, 1, 59, 2375, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8875, 1, 59, 2483, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8876, 1, 59, 2484, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8877, 1, 59, 2485, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8878, 1, 59, 2486, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8879, 1, 59, 2487, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8880, 1, 59, 2488, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8881, 1, 59, 2376, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8882, 1, 59, 2525, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8883, 1, 59, 2526, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8884, 1, 59, 2527, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8885, 1, 59, 2528, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8886, 1, 59, 2372, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8887, 1, 59, 2413, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8888, 1, 59, 2414, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8889, 1, 59, 2415, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8890, 1, 59, 2416, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8891, 1, 59, 2417, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8892, 1, 59, 2377, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8893, 1, 59, 2532, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8894, 1, 59, 2533, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8895, 1, 59, 2534, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8896, 1, 59, 2535, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8897, 1, 59, 2536, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8898, 1, 59, 2220, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8899, 1, 59, 2490, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8900, 1, 59, 2497, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8901, 1, 59, 2498, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8902, 1, 59, 2499, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8903, 1, 59, 2500, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8904, 1, 59, 2491, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8905, 1, 59, 2504, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8906, 1, 59, 2505, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8907, 1, 59, 2506, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8908, 1, 59, 2507, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8909, 1, 59, 2492, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8910, 1, 59, 2511, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8911, 1, 59, 2512, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8912, 1, 59, 2513, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8913, 1, 59, 2514, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8914, 1, 59, 2493, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8915, 1, 59, 2518, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8916, 1, 59, 2519, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8917, 1, 59, 2520, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8918, 1, 59, 2521, 1, '2026-06-01 18:05:54');
INSERT INTO `sys_role_permission` VALUES (8919, 1, 59, 2522, 1, '2026-06-01 18:05:54');

-- ----------------------------
-- Table structure for sys_session
-- ----------------------------
DROP TABLE IF EXISTS `sys_session`;
CREATE TABLE `sys_session`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID（UUID）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `device_info` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备信息',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器User-Agent',
  `status` int NOT NULL DEFAULT 1 COMMENT '会话状态：1=活跃，0=已踢出',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `last_active` datetime NULL DEFAULT NULL COMMENT '最后活跃时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_session
-- ----------------------------

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户名称',
  `tenant_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户编码',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态(0禁用 1正常 2过期)',
  `user_limit` int NOT NULL DEFAULT 5 COMMENT '用户数量限制',
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `deleted` int NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` bigint NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tenant_code`(`tenant_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------

-- ----------------------------
-- Table structure for sys_tenant_quota
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_quota`;
CREATE TABLE `sys_tenant_quota`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `max_users` int NOT NULL DEFAULT 5,
  `max_storage_mb` int NOT NULL DEFAULT 1024,
  `max_api_calls_per_day` int NOT NULL DEFAULT 10000,
  `max_concurrent_requests` int NOT NULL DEFAULT 100,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户配额表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant_quota
-- ----------------------------

-- ----------------------------
-- Table structure for sys_tenant_resource_usage
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_resource_usage`;
CREATE TABLE `sys_tenant_resource_usage`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `user_count` int NOT NULL DEFAULT 0,
  `storage_used_mb` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `api_calls_today` int NOT NULL DEFAULT 0,
  `concurrent_requests` int NOT NULL DEFAULT 0,
  `record_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_date`(`tenant_id` ASC, `record_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户资源使用表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_tenant_resource_usage
-- ----------------------------

-- ----------------------------
-- Table structure for sys_token_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `sys_token_blacklist`;
CREATE TABLE `sys_token_blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token_prefix` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Token前缀(前32字符)',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `expiry_time` datetime NOT NULL COMMENT '过期时间',
  `reason` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOGOUT' COMMENT '原因(LOGOUT/REFRESH/FORCED)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_token_prefix`(`token_prefix` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_expiry_time`(`expiry_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Token黑名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_token_blacklist
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT '用户ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `pass_word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `locked` tinyint NOT NULL DEFAULT 0 COMMENT '锁定状态: 0-未锁定, 1-已锁定',
  `locked_until` datetime NULL DEFAULT NULL COMMENT '锁定到期时间',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '登录失败次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `password_changed_at` datetime NULL DEFAULT NULL COMMENT '密码修改时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username_tenant`(`user_name` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sys_user_tenant`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_user_tenant_username`(`tenant_id` ASC, `user_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '$2a$10$FaVniNYzRkROPg/PlCLKJOQKLE1jGcDJZJYE7ryXfOFOMdIrfbN7u', 'admin@example.com', '13800000000', '系统管理员', NULL, NULL, 1, 0, NULL, 0, '2026-06-01 18:29:34', '0:0:0:0:0:0:0:1', NULL, 0, NULL, '2026-05-23 15:13:11', NULL, '2026-05-23 15:13:11');

-- ----------------------------
-- Table structure for sys_user_address
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_address`;
CREATE TABLE `sys_user_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区/县',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认: 0-否, 1-是',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_address
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_permission`;
CREATE TABLE `sys_user_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `user_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_permission`(`user_id` ASC, `permission_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_profile
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_profile`;
CREATE TABLE `sys_user_profile`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位',
  `hire_date` date NULL DEFAULT NULL COMMENT '入职日期',
  `leave_date` date NULL DEFAULT NULL COMMENT '离职日期',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_profile
-- ----------------------------
INSERT INTO `sys_user_profile` VALUES (1, 1, '系统管理员', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-18 19:41:27', '2026-04-18 19:41:27');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (3, 1, 2045547755942199298, 46, 1, '2026-05-16 23:46:49');
INSERT INTO `sys_user_role` VALUES (4, 1, 2055673487263649794, 46, 1, '2026-05-16 23:46:52');
INSERT INTO `sys_user_role` VALUES (6, 1, 1, 52, 1, '2026-05-23 22:16:19');
INSERT INTO `sys_user_role` VALUES (7, 1, 1, 55, 1, '2026-06-01 16:25:24');
INSERT INTO `sys_user_role` VALUES (8, 1, 1, 58, 1, '2026-06-01 17:54:15');

-- ----------------------------
-- Table structure for tenant_subscription
-- ----------------------------
DROP TABLE IF EXISTS `tenant_subscription`;
CREATE TABLE `tenant_subscription`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `package_id` bigint NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NULL DEFAULT NULL,
  `status` int NOT NULL DEFAULT 1 COMMENT '0=过期 1=有效',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户订阅表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_subscription
-- ----------------------------

-- ----------------------------
-- Table structure for third_party_api
-- ----------------------------
DROP TABLE IF EXISTS `third_party_api`;
CREATE TABLE `third_party_api`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `api_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `api_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'GET',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NONE' COMMENT 'NONE/API_KEY/OAUTH2/BASIC',
  `api_key_header` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `api_key_value` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `timeout_ms` int NULL DEFAULT 5000,
  `retry_count` int NULL DEFAULT 0,
  `status` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '第三方API配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of third_party_api
-- ----------------------------

-- ----------------------------
-- Table structure for third_party_call_log
-- ----------------------------
DROP TABLE IF EXISTS `third_party_call_log`;
CREATE TABLE `third_party_call_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `api_id` bigint NOT NULL,
  `request_time` datetime NOT NULL,
  `response_status` int NULL DEFAULT NULL,
  `response_time_ms` int NULL DEFAULT NULL,
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '第三方API调用日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of third_party_call_log
-- ----------------------------

-- ----------------------------
-- Table structure for trace_alert
-- ----------------------------
DROP TABLE IF EXISTS `trace_alert`;
CREATE TABLE `trace_alert`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `alert_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `metric_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'RESPONSE_TIME/ERROR_RATE/THROUGHPUT',
  `threshold` decimal(18, 4) NOT NULL,
  `condition_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GT',
  `service_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `enabled` int NOT NULL DEFAULT 1,
  `notify_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'LOG',
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '链路追踪告警表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trace_alert
-- ----------------------------

-- ----------------------------
-- Table structure for trace_config
-- ----------------------------
DROP TABLE IF EXISTS `trace_config`;
CREATE TABLE `trace_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `service_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sample_rate` decimal(5, 4) NOT NULL DEFAULT 1.0000,
  `enabled` int NOT NULL DEFAULT 1,
  `deleted` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '链路追踪配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of trace_config
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

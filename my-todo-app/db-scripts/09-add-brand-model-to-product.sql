-- 为 erp_product 表添加品牌和型号字段
-- 执行时间：2026-04-19

ALTER TABLE `erp_product` 
ADD COLUMN `brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌' AFTER `product_name`,
ADD COLUMN `model` VARCHAR(100) DEFAULT NULL COMMENT '型号' AFTER `brand`;

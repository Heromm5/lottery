/*
 Navicat Premium Dump SQL

 Source Server         : Local
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : localhost:3306
 Source Schema         : my_thought

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 05/02/2026 08:51:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for prediction_method_weight
-- ----------------------------
DROP TABLE IF EXISTS `prediction_method_weight`;
CREATE TABLE `prediction_method_weight`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `method_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方法代码(HOT/MISSING/BALANCED/ML/COMBINED)',
  `method_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方法名称',
  `weight` decimal(5, 4) NULL DEFAULT 0.2000 COMMENT '当前权重(0-1,所有方法权重之和为1)',
  `total_predictions` int NULL DEFAULT 0 COMMENT '总预测次数',
  `total_hits` int NULL DEFAULT 0 COMMENT '命中次数(前区>=3或后区>=1)',
  `hit_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '平滑后的命中率(EMA)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_method_code`(`method_code` ASC) USING BTREE COMMENT '方法代码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预测方法权重表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

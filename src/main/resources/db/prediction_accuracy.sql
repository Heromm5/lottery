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

 Date: 05/02/2026 08:51:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for prediction_accuracy
-- ----------------------------
DROP TABLE IF EXISTS `prediction_accuracy`;
CREATE TABLE `prediction_accuracy`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `predict_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预测方法(HOT/MISSING/BALANCED/ML/COMBINED)',
  `total_predictions` int NULL DEFAULT 0 COMMENT '该方法总预测次数',
  `front_avg_hit` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '前区平均命中数',
  `back_avg_hit` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '后区平均命中数',
  `prize_count_1` int NULL DEFAULT 0 COMMENT '一等奖次数(前5+后2)',
  `prize_count_2` int NULL DEFAULT 0 COMMENT '二等奖次数(前5+后1)',
  `prize_count_3` int NULL DEFAULT 0 COMMENT '三等奖次数(前5+后0 或 前4+后2)',
  `prize_count_4` int NULL DEFAULT 0 COMMENT '四等奖次数(前4+后1)',
  `prize_count_5` int NULL DEFAULT 0 COMMENT '五等奖次数(前4+后0 或 前3+后2)',
  `prize_count_6` int NULL DEFAULT 0 COMMENT '六等奖次数(前3+后1 或 前2+后2)',
  `prize_count_7` int NULL DEFAULT 0 COMMENT '七等奖次数(前3+后0 或 前2+后1 或 前1+后2 或 前0+后2)',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_method`(`predict_method` ASC) USING BTREE COMMENT '预测方法唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预测准确率统计表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

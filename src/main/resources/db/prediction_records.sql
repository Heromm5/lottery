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

 Date: 05/02/2026 08:51:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for prediction_records
-- ----------------------------
DROP TABLE IF EXISTS `prediction_records`;
CREATE TABLE `prediction_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_issue` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预测目标期号',
  `predict_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预测方法(HOT-热号优先/MISSING-遗漏回补/BALANCED-冷热均衡/ML-机器学习/COMBINED-综合推荐)',
  `front_balls` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预测前区号码(逗号分隔)',
  `back_balls` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预测后区号码(逗号分隔)',
  `front_ball1` int NULL DEFAULT NULL COMMENT '前区第1个号码(1-35)',
  `front_ball2` int NULL DEFAULT NULL COMMENT '前区第2个号码(1-35)',
  `front_ball3` int NULL DEFAULT NULL COMMENT '前区第3个号码(1-35)',
  `front_ball4` int NULL DEFAULT NULL COMMENT '前区第4个号码(1-35)',
  `front_ball5` int NULL DEFAULT NULL COMMENT '前区第5个号码(1-35)',
  `back_ball1` int NULL DEFAULT NULL COMMENT '后区第1个号码(1-12)',
  `back_ball2` int NULL DEFAULT NULL COMMENT '后区第2个号码(1-12)',
  `is_verified` tinyint NULL DEFAULT 0 COMMENT '是否已验证(0-未验证,1-已验证)',
  `front_hit_count` int NULL DEFAULT NULL COMMENT '前区命中数(0-5)',
  `back_hit_count` int NULL DEFAULT NULL COMMENT '后区命中数(0-2)',
  `prize_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '中奖等级(一等奖~七等奖/未中奖)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `verified_at` timestamp NULL DEFAULT NULL COMMENT '验证时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target_issue`(`target_issue` ASC) USING BTREE COMMENT '目标期号索引',
  INDEX `idx_predict_method`(`predict_method` ASC) USING BTREE COMMENT '预测方法索引',
  INDEX `idx_is_verified`(`is_verified` ASC) USING BTREE COMMENT '验证状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1326 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预测记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

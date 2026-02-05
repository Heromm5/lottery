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

 Date: 05/02/2026 08:51:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for lottery_results
-- ----------------------------
DROP TABLE IF EXISTS `lottery_results`;
CREATE TABLE `lottery_results`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `issue` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '期号(如25001表示2025年第1期)',
  `draw_date` date NOT NULL COMMENT '开奖日期',
  `front_ball1` int NULL DEFAULT NULL COMMENT '前区第1个号码(1-35)',
  `front_ball2` int NULL DEFAULT NULL COMMENT '前区第2个号码(1-35)',
  `front_ball3` int NULL DEFAULT NULL COMMENT '前区第3个号码(1-35)',
  `front_ball4` int NULL DEFAULT NULL COMMENT '前区第4个号码(1-35)',
  `front_ball5` int NULL DEFAULT NULL COMMENT '前区第5个号码(1-35)',
  `back_ball1` int NULL DEFAULT NULL COMMENT '后区第1个号码(1-12)',
  `back_ball2` int NULL DEFAULT NULL COMMENT '后区第2个号码(1-12)',
  `front_balls` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前区号码(逗号分隔,如1,7,19,20,35)',
  `back_balls` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '后区号码(逗号分隔,如3,9)',
  `front_sum` int NULL DEFAULT NULL COMMENT '前区号码和值(5个号码之和)',
  `back_sum` int NULL DEFAULT NULL COMMENT '后区号码和值(2个号码之和)',
  `odd_count_front` int NULL DEFAULT NULL COMMENT '前区奇数个数(0-5)',
  `odd_count_back` int NULL DEFAULT NULL COMMENT '后区奇数个数(0-2)',
  `ac_value` int NULL DEFAULT NULL COMMENT 'AC值(号码复杂度指标)',
  `consecutive_count_front` int NULL DEFAULT NULL COMMENT '前区连号组数(如12,13算1组连号)',
  `consecutive_count_back` int NULL DEFAULT NULL COMMENT '后区连号组数(如1,2算1组连号)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `issue`(`issue` ASC) USING BTREE COMMENT '期号唯一索引',
  INDEX `idx_issue`(`issue` ASC) USING BTREE COMMENT '期号查询索引',
  INDEX `idx_draw_date`(`draw_date` ASC) USING BTREE COMMENT '开奖日期查询索引',
  INDEX `idx_front_sum`(`front_sum` ASC) USING BTREE COMMENT '前区和值查询索引'
) ENGINE = InnoDB AUTO_INCREMENT = 266 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '大乐透开奖结果表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

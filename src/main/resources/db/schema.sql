-- =====================================================
-- 大乐透数据分析与预测系统 - 权威数据库表结构
-- 统一维护，作为唯一 DDL 来源
-- =====================================================

-- 开奖结果表
DROP TABLE IF EXISTS `lottery_results`;
CREATE TABLE `lottery_results` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `issue` VARCHAR(20) NOT NULL COMMENT '期号(如25001表示2025年第1期)',
    `draw_date` DATE NOT NULL COMMENT '开奖日期',
    `front_ball1` INT NULL COMMENT '前区第1个号码(1-35)',
    `front_ball2` INT NULL COMMENT '前区第2个号码(1-35)',
    `front_ball3` INT NULL COMMENT '前区第3个号码(1-35)',
    `front_ball4` INT NULL COMMENT '前区第4个号码(1-35)',
    `front_ball5` INT NULL COMMENT '前区第5个号码(1-35)',
    `back_ball1` INT NULL COMMENT '后区第1个号码(1-12)',
    `back_ball2` INT NULL COMMENT '后区第2个号码(1-12)',
    `front_balls` VARCHAR(50) NULL COMMENT '前区号码(逗号分隔,如1,7,19,20,35)',
    `back_balls` VARCHAR(20) NULL COMMENT '后区号码(逗号分隔,如3,9)',
    `front_sum` INT NULL COMMENT '前区号码和值',
    `back_sum` INT NULL COMMENT '后区号码和值',
    `odd_count_front` INT NULL COMMENT '前区奇数个数(0-5)',
    `odd_count_back` INT NULL COMMENT '后区奇数个数(0-2)',
    `ac_value` INT NULL COMMENT 'AC值(号码复杂度指标)',
    `consecutive_count_front` INT NULL COMMENT '前区连号组数',
    `consecutive_count_back` INT NULL COMMENT '后区连号组数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    UNIQUE INDEX `uk_issue` (`issue`) COMMENT '期号唯一索引',
    INDEX `idx_draw_date` (`draw_date`) COMMENT '开奖日期查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='开奖结果表';

-- 预测记录表
DROP TABLE IF EXISTS `prediction_records`;
CREATE TABLE `prediction_records` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `target_issue` VARCHAR(20) NOT NULL COMMENT '预测目标期号',
    `predict_method` VARCHAR(50) NOT NULL COMMENT '预测方法代码',
    `front_balls` VARCHAR(50) NOT NULL COMMENT '预测前区号码(逗号分隔)',
    `back_balls` VARCHAR(20) NOT NULL COMMENT '预测后区号码(逗号分隔)',
    `front_ball1` INT NULL COMMENT '前区第1个号码(1-35)',
    `front_ball2` INT NULL COMMENT '前区第2个号码(1-35)',
    `front_ball3` INT NULL COMMENT '前区第3个号码(1-35)',
    `front_ball4` INT NULL COMMENT '前区第4个号码(1-35)',
    `front_ball5` INT NULL COMMENT '前区第5个号码(1-35)',
    `back_ball1` INT NULL COMMENT '后区第1个号码(1-12)',
    `back_ball2` INT NULL COMMENT '后区第2个号码(1-12)',
    `is_verified` TINYINT DEFAULT 0 COMMENT '是否已验证(0-未验证,1-已验证)',
    `is_final` TINYINT DEFAULT 0 COMMENT '是否当次最终预测(0-否,1-是)',
    `front_hit_count` INT DEFAULT NULL COMMENT '前区命中数(0-5)',
    `back_hit_count` INT DEFAULT NULL COMMENT '后区命中数(0-2)',
    `prize_level` VARCHAR(20) DEFAULT NULL COMMENT '中奖等级(一等奖~七等奖/未中奖)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `verified_at` TIMESTAMP NULL COMMENT '验证时间',
    INDEX `idx_target_issue` (`target_issue`) COMMENT '目标期号索引',
    INDEX `idx_pr_issue_verified` (`target_issue`, `is_verified`) COMMENT '期号+验证状态复合索引',
    INDEX `idx_pr_issue_final` (`target_issue`, `is_final` DESC) COMMENT '期号+最终预测复合索引',
    INDEX `idx_pr_method_verified` (`predict_method`, `is_verified`) COMMENT '方法+验证状态复合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预测记录表';

-- 预测准确率统计表
DROP TABLE IF EXISTS `prediction_accuracy`;
CREATE TABLE `prediction_accuracy` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `predict_method` VARCHAR(50) NOT NULL COMMENT '预测方法代码',
    `total_predictions` INT DEFAULT 0 COMMENT '该方法总预测次数',
    `front_avg_hit` DECIMAL(5,2) DEFAULT 0.00 COMMENT '前区平均命中数',
    `back_avg_hit` DECIMAL(5,2) DEFAULT 0.00 COMMENT '后区平均命中数',
    `prize_count_1` INT DEFAULT 0 COMMENT '一等奖次数',
    `prize_count_2` INT DEFAULT 0 COMMENT '二等奖次数',
    `prize_count_3` INT DEFAULT 0 COMMENT '三等奖次数',
    `prize_count_4` INT DEFAULT 0 COMMENT '四等奖次数',
    `prize_count_5` INT DEFAULT 0 COMMENT '五等奖次数',
    `prize_count_6` INT DEFAULT 0 COMMENT '六等奖次数',
    `prize_count_7` INT DEFAULT 0 COMMENT '七等奖次数',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    UNIQUE INDEX `uk_method` (`predict_method`) COMMENT '预测方法唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预测准确率统计表';

-- 预测方法权重表（持续学习）
DROP TABLE IF EXISTS `prediction_method_weight`;
CREATE TABLE `prediction_method_weight` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `method_code` VARCHAR(32) NOT NULL COMMENT '方法代码',
    `method_name` VARCHAR(50) NOT NULL COMMENT '方法名称',
    `weight` DECIMAL(5,4) DEFAULT 0.2000 COMMENT '当前权重(0-1)',
    `total_predictions` INT DEFAULT 0 COMMENT '总预测次数',
    `total_hits` INT DEFAULT 0 COMMENT '命中次数',
    `hit_rate` DECIMAL(5,4) DEFAULT 0.0000 COMMENT '平滑后的命中率(EMA)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX `uk_method_code` (`method_code`) COMMENT '方法代码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预测方法权重表';

-- 初始化方法权重数据
INSERT IGNORE INTO `prediction_method_weight` (`method_code`, `method_name`, `weight`) VALUES
('HOT', '热号优先', 0.1000),
('MISSING', '遗漏回补', 0.1000),
('BALANCED', '冷热均衡', 0.1000),
('ML', '机器学习', 0.1000),
('ADAPTIVE', '自适应预测', 0.1000),
('BAYESIAN', '贝叶斯预测', 0.1000),
('MARKOV', '马尔可夫预测', 0.1000),
('MONTECARLO', '蒙特卡洛预测', 0.1000),
('GRADIENT_BOOST', '梯度提升预测', 0.1000),
('ENSEMBLE', '集成预测', 0.1000);

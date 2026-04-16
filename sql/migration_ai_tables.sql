-- =====================================================
-- AI 增强功能数据库迁移脚本
-- 执行方式：mysql -u root -p my_thought < sql/migration_ai_tables.sql
-- =====================================================

-- AI 分析报告表
-- 存储每日/每周/每期 AI 分析报告
CREATE TABLE IF NOT EXISTS ai_analysis_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(20) NOT NULL COMMENT '报告类型: daily-每日|weekly-每周|issue-每期',
    summary TEXT COMMENT '报告摘要',
    content JSON COMMENT '报告完整内容',
    insights JSON COMMENT 'AI洞察',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_report_type (report_type) COMMENT '报告类型索引',
    INDEX idx_created_at (created_at) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI分析报告表';

-- 异常报警表
-- 存储号码异常、模式异常等报警信息
CREATE TABLE IF NOT EXISTS anomaly_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL COMMENT '报警类型',
    severity VARCHAR(10) NOT NULL COMMENT '严重程度: LOW-低|MEDIUM-中|HIGH-高|CRITICAL-严重',
    description TEXT COMMENT '报警描述',
    detected_data JSON COMMENT '检测到的异常数据',
    acknowledged BOOLEAN DEFAULT FALSE COMMENT '是否已确认',
    acknowledged_at TIMESTAMP NULL COMMENT '确认时间',
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    INDEX idx_alert_type (alert_type) COMMENT '报警类型索引',
    INDEX idx_severity (severity) COMMENT '严重程度索引',
    INDEX idx_detected_at (detected_at) COMMENT '检测时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异常报警表';

-- 号码规律表
-- 存储 AI 发现的号码规律
CREATE TABLE IF NOT EXISTS number_pattern (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pattern_type VARCHAR(30) NOT NULL COMMENT '规律类型: frequency-频率|missing-遗漏|trend-趋势|association-关联',
    pattern_desc TEXT COMMENT '规律描述',
    confidence DECIMAL(5,4) COMMENT '置信度0-1',
    evidence JSON COMMENT '支持证据',
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
    INDEX idx_pattern_type (pattern_type) COMMENT '规律类型索引',
    INDEX idx_confidence (confidence) COMMENT '置信度索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='号码规律表';

-- 扩展 prediction_records 表 - 添加 AI 相关字段
-- 注意：MySQL 不支持 ADD COLUMN IF NOT EXISTS 语法，需手动检查后执行
-- 以下为标准 ALTER 语句，如已存在同名列请跳过

ALTER TABLE prediction_records
    ADD COLUMN IF NOT EXISTS ai_model VARCHAR(50) COMMENT '使用的AI模型',
    ADD COLUMN IF NOT EXISTS ai_confidence DECIMAL(5,4) COMMENT 'AI置信度',
    ADD COLUMN IF NOT EXISTS ai_reasoning TEXT COMMENT 'AI推理过程';
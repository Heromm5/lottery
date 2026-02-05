-- 性能优化：添加数据库索引
-- 执行方式：mysql -u root -p my_thought < sql/init-index.sql

-- lottery_results 表索引
-- 期号查询（最频繁）
CREATE INDEX idx_lottery_results_issue ON lottery_results(issue);
-- 开奖日期排序
CREATE INDEX idx_lottery_results_draw_date ON lottery_results(draw_date DESC);
-- 复合索引：按日期和期号排序
CREATE INDEX idx_lottery_results_date_issue ON lottery_results(draw_date DESC, issue DESC);

-- prediction_records 表索引
-- 期号+验证状态查询（验证核心查询）
CREATE INDEX idx_prediction_records_issue_verified ON prediction_records(target_issue, is_verified);
-- 方法+验证状态查询（统计核心查询）
CREATE INDEX idx_prediction_records_method_verified ON prediction_records(predict_method, is_verified);
-- 创建时间排序
CREATE INDEX idx_prediction_records_created_at ON prediction_records(created_at DESC);
-- 目标期号查询
CREATE INDEX idx_prediction_records_target_issue ON prediction_records(target_issue);

-- prediction_accuracy 表索引
-- 方法唯一查询
CREATE INDEX idx_prediction_accuracy_method ON prediction_accuracy(predict_method);

-- method_weight 表索引
CREATE INDEX idx_method_weight_method ON method_weight(predict_method);

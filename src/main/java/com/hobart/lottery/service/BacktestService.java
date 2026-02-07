package com.hobart.lottery.service;

import com.hobart.lottery.dto.BacktestResultDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.predictor.*;
import com.hobart.lottery.service.analysis.FrequencyAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 批量历史回测服务
 * 使用历史开奖数据测试各预测算法的表现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestService {

    private final LotteryService lotteryService;
    private final AnalysisService analysisService;
    private final FrequencyAnalyzer frequencyAnalyzer;
    
    // 大乐透官方奖金（税前，元）
    private static final Map<String, Long> PRIZE_MONEY = new HashMap<>();
    private static final Map<String, Integer> PRIZE_LEVELS = new HashMap<>();
    private static final Map<String, String> METHOD_NAMES = new HashMap<>();
    
    static {
        PRIZE_MONEY.put("一等奖", 10_000_000L);
        PRIZE_MONEY.put("二等奖", 200_000L);
        PRIZE_MONEY.put("三等奖", 10_000L);
        PRIZE_MONEY.put("四等奖", 3_000L);
        PRIZE_MONEY.put("五等奖", 300L);
        PRIZE_MONEY.put("六等奖", 200L);
        PRIZE_MONEY.put("七等奖", 5L);
        PRIZE_MONEY.put("未中奖", 0L);
        
        PRIZE_LEVELS.put("一等奖", 7);
        PRIZE_LEVELS.put("二等奖", 6);
        PRIZE_LEVELS.put("三等奖", 5);
        PRIZE_LEVELS.put("四等奖", 4);
        PRIZE_LEVELS.put("五等奖", 3);
        PRIZE_LEVELS.put("六等奖", 2);
        PRIZE_LEVELS.put("七等奖", 1);
        PRIZE_LEVELS.put("未中奖", 0);
        
        METHOD_NAMES.put("HOT", "热号优先");
        METHOD_NAMES.put("MISSING", "遗漏回补");
        METHOD_NAMES.put("BALANCED", "冷热均衡");
        METHOD_NAMES.put("ML", "机器学习");
        METHOD_NAMES.put("BAYESIAN", "贝叶斯预测");
        METHOD_NAMES.put("MARKOV", "马尔可夫预测");
        METHOD_NAMES.put("MONTECARLO", "蒙特卡洛预测");
        METHOD_NAMES.put("ENSEMBLE", "集成预测");
    }

    /**
     * 批量回测
     * @param method 预测方法（null表示所有方法）
     * @param issueCount 回测最近多少期
     * @param predictionsPerIssue 每期每种方法生成多少注
     * @return 各方法的回测结果
     */
    public List<BacktestResultDTO> runBacktest(String method, int issueCount, int predictionsPerIssue) {
        log.info("开始批量回测：方法={}, 期数={}, 每期注数={}", method, issueCount, predictionsPerIssue);
        
        // 获取最近的开奖结果
        List<LotteryResult> historicalResults = lotteryService.getRecentResults(issueCount);
        if (historicalResults.isEmpty()) {
            log.warn("没有历史开奖数据可供回测");
            return Collections.emptyList();
        }
        
        // 确定要回测的方法
        List<String> methodsToTest;
        if (method != null && !method.isEmpty()) {
            methodsToTest = Collections.singletonList(method.toUpperCase());
        } else {
            methodsToTest = Arrays.asList("HOT", "MISSING", "BALANCED", "ML", "BAYESIAN", "MARKOV", "MONTECARLO", "ENSEMBLE");
        }
        
        List<BacktestResultDTO> results = new ArrayList<>();
        
        for (String testMethod : methodsToTest) {
            try {
                BacktestResultDTO result = backtestMethod(testMethod, historicalResults, predictionsPerIssue);
                if (result != null) {
                    results.add(result);
                    log.info("方法 {} 回测完成：中奖率={}%, ROI={}%", 
                        testMethod, 
                        String.format("%.2f", result.getPrizeRate()),
                        String.format("%.2f", result.getRoi()));
                }
            } catch (Exception e) {
                log.error("回测方法 {} 失败: {}", testMethod, e.getMessage());
            }
        }
        
        // 按ROI排序
        results.sort((a, b) -> Double.compare(b.getRoi(), a.getRoi()));
        
        log.info("批量回测完成，共测试 {} 种方法", results.size());
        return results;
    }

    /**
     * 回测单个方法
     */
    private BacktestResultDTO backtestMethod(String method, List<LotteryResult> historicalResults, int predictionsPerIssue) {
        BasePredictor predictor = createPredictor(method);
        if (predictor == null) {
            return null;
        }
        
        List<BacktestResultDTO.BacktestDetailDTO> details = new ArrayList<>();
        int totalPredictions = 0;
        int totalFrontHits = 0;
        int totalBackHits = 0;
        Map<String, Integer> prizeCounts = new HashMap<>();
        long totalPrizeMoney = 0;
        String bestIssue = null;
        String bestPrize = "未中奖";
        
        // 从后往前回测（最新的期号到最后）
        for (int i = historicalResults.size() - 1; i >= 0; i--) {
            LotteryResult actualResult = historicalResults.get(i);
            
            // 生成预测
            List<int[][]> predictions = predictor.predictMultiple(predictionsPerIssue);
            
            int[] actualFront = actualResult.getFrontBallArray();
            int[] actualBack = actualResult.getBackBallArray();
            Set<Integer> actualFrontSet = arrayToSet(actualFront);
            Set<Integer> actualBackSet = arrayToSet(actualBack);
            
            // 验证每一注预测
            for (int[][] prediction : predictions) {
                totalPredictions++;
                
                int frontHit = countHits(prediction[0], actualFrontSet);
                int backHit = countHits(prediction[1], actualBackSet);
                totalFrontHits += frontHit;
                totalBackHits += backHit;
                
                String prizeLevel = determinePrizeLevel(frontHit, backHit);
                prizeCounts.merge(prizeLevel, 1, Integer::sum);
                
                long prizeMoney = PRIZE_MONEY.getOrDefault(prizeLevel, 0L);
                totalPrizeMoney += prizeMoney;
                
                // 记录最佳表现
                if (comparePrizeLevel(prizeLevel, bestPrize) > 0) {
                    bestPrize = prizeLevel;
                    bestIssue = actualResult.getIssue();
                }
                
                // 只记录中奖的详情（减少数据量）
                if (!"未中奖".equals(prizeLevel)) {
                    BacktestResultDTO.BacktestDetailDTO detail = new BacktestResultDTO.BacktestDetailDTO();
                    detail.setIssue(actualResult.getIssue());
                    detail.setPrediction(formatBalls(prediction));
                    detail.setActualResult(formatBalls(actualFront, actualBack));
                    detail.setFrontHit(frontHit);
                    detail.setBackHit(backHit);
                    detail.setPrizeLevel(prizeLevel);
                    detail.setPrizeMoney(prizeMoney);
                    details.add(detail);
                }
            }
        }
        
        if (totalPredictions == 0) {
            return null;
        }
        
        // 计算统计指标
        int totalIssues = historicalResults.size();
        double avgFrontHit = (double) totalFrontHits / totalPredictions;
        double avgBackHit = (double) totalBackHits / totalPredictions;
        double frontHitRate = avgFrontHit * 100.0 / 5.0;
        double backHitRate = avgBackHit * 100.0 / 2.0;
        
        int totalPrizeCount = prizeCounts.values().stream().mapToInt(Integer::intValue).sum();
        double prizeRate = totalPrizeCount * 100.0 / totalPredictions;
        
        int highPrizeCount = prizeCounts.getOrDefault("一等奖", 0) 
            + prizeCounts.getOrDefault("二等奖", 0) 
            + prizeCounts.getOrDefault("三等奖", 0);
        
        int totalCost = totalPredictions * 2; // 每注2元
        long profitLoss = totalPrizeMoney - totalCost;
        double roi = totalCost > 0 ? (profitLoss * 100.0 / totalCost) : 0;
        
        // 生成评价
        String evaluation = generateEvaluation(roi, prizeRate, highPrizeCount, totalPredictions);
        
        // 限制详情数量，只保留高等奖记录
        details.sort((a, b) -> comparePrizeLevel(b.getPrizeLevel(), a.getPrizeLevel()));
        if (details.size() > 50) {
            details = details.subList(0, 50);
        }
        
        BacktestResultDTO result = new BacktestResultDTO();
        result.setMethod(method);
        result.setMethodName(getMethodDisplayName(method));
        result.setTotalIssues(totalIssues);
        result.setTotalPredictions(totalPredictions);
        result.setAvgFrontHit(BigDecimal.valueOf(avgFrontHit).setScale(2, RoundingMode.HALF_UP));
        result.setAvgBackHit(BigDecimal.valueOf(avgBackHit).setScale(2, RoundingMode.HALF_UP));
        result.setFrontHitRate(frontHitRate);
        result.setBackHitRate(backHitRate);
        result.setPrizeCount1(prizeCounts.getOrDefault("一等奖", 0));
        result.setPrizeCount2(prizeCounts.getOrDefault("二等奖", 0));
        result.setPrizeCount3(prizeCounts.getOrDefault("三等奖", 0));
        result.setPrizeCount4(prizeCounts.getOrDefault("四等奖", 0));
        result.setPrizeCount5(prizeCounts.getOrDefault("五等奖", 0));
        result.setPrizeCount6(prizeCounts.getOrDefault("六等奖", 0));
        result.setPrizeCount7(prizeCounts.getOrDefault("七等奖", 0));
        result.setTotalPrizeCount(totalPrizeCount);
        result.setPrizeRate(prizeRate);
        result.setHighPrizeCount(highPrizeCount);
        result.setTotalCost(totalCost);
        result.setTotalPrizeMoney(totalPrizeMoney);
        result.setProfitLoss(profitLoss);
        result.setRoi(roi);
        result.setBestIssue(bestIssue);
        result.setBestPrize(bestPrize);
        result.setDetails(details);
        result.setEvaluation(evaluation);
        
        return result;
    }

    /**
     * 创建预测器
     */
    private BasePredictor createPredictor(String method) {
        switch (method.toUpperCase()) {
            case "HOT": return new HotNumberPredictor(analysisService);
            case "MISSING": return new MissingPredictor(analysisService);
            case "BALANCED": return new BalancedPredictor(analysisService);
            case "ML": return new MLPredictor(analysisService, lotteryService);
            case "BAYESIAN": return new BayesianPredictor(analysisService, lotteryService);
            case "MARKOV": return new MarkovPredictor(analysisService, lotteryService);
            case "MONTECARLO": return new MonteCarloPredictor(analysisService, lotteryService);
            case "ENSEMBLE": return new EnsemblePredictor(analysisService, lotteryService);
            default: return null;
        }
    }

    /**
     * 获取方法显示名称
     */
    private String getMethodDisplayName(String method) {
        return METHOD_NAMES.getOrDefault(method.toUpperCase(), method);
    }

    /**
     * 数组转Set
     */
    private Set<Integer> arrayToSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int num : arr) {
            set.add(num);
        }
        return set;
    }

    /**
     * 计算命中数
     */
    private int countHits(int[] prediction, Set<Integer> actual) {
        int hits = 0;
        for (int num : prediction) {
            if (actual.contains(num)) {
                hits++;
            }
        }
        return hits;
    }

    /**
     * 判定中奖等级
     */
    private String determinePrizeLevel(int frontHit, int backHit) {
        if (frontHit == 5 && backHit == 2) return "一等奖";
        if (frontHit == 5 && backHit == 1) return "二等奖";
        if (frontHit == 5 && backHit == 0) return "三等奖";
        if (frontHit == 4 && backHit == 2) return "三等奖";
        if (frontHit == 4 && backHit == 1) return "四等奖";
        if (frontHit == 4 && backHit == 0) return "五等奖";
        if (frontHit == 3 && backHit == 2) return "五等奖";
        if (frontHit == 3 && backHit == 1) return "六等奖";
        if (frontHit == 2 && backHit == 2) return "六等奖";
        if (frontHit == 3 && backHit == 0) return "七等奖";
        if (frontHit == 2 && backHit == 1) return "七等奖";
        if (frontHit == 1 && backHit == 2) return "七等奖";
        if (frontHit == 0 && backHit == 2) return "七等奖";
        return "未中奖";
    }

    /**
     * 比较中奖等级（用于排序）
     * 返回值：正数表示level1更好，负数表示level2更好
     */
    private int comparePrizeLevel(String level1, String level2) {
        return PRIZE_LEVELS.getOrDefault(level1, 0) - PRIZE_LEVELS.getOrDefault(level2, 0);
    }

    /**
     * 格式化号码显示
     */
    private String formatBalls(int[][] balls) {
        return formatBalls(balls[0], balls[1]);
    }

    private String formatBalls(int[] front, int[] back) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < front.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%02d", front[i]));
        }
        sb.append("] + [");
        for (int i = 0; i < back.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%02d", back[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 生成评价
     */
    private String generateEvaluation(double roi, double prizeRate, int highPrizeCount, int totalPredictions) {
        StringBuilder sb = new StringBuilder();
        
        // ROI评价
        if (roi > 0) {
            sb.append("盈利").append(String.format("%.1f", roi)).append("%，表现优秀！");
        } else if (roi > -30) {
            sb.append("亏损").append(String.format("%.1f", Math.abs(roi))).append("%，表现尚可。");
        } else {
            sb.append("亏损").append(String.format("%.1f", Math.abs(roi))).append("%，表现较差。");
        }
        
        // 中奖率评价
        sb.append("中奖率").append(String.format("%.1f", prizeRate)).append("%，");
        if (prizeRate > 10) {
            sb.append("中奖频率较高。");
        } else if (prizeRate > 5) {
            sb.append("中奖频率一般。");
        } else {
            sb.append("中奖频率较低。");
        }
        
        // 高等奖评价
        if (highPrizeCount > 0) {
            sb.append("期间中过").append(highPrizeCount).append("次高等奖（三等及以上），");
            sb.append("运气不错！");
        } else {
            sb.append("未中过高等奖。");
        }
        
        // 总体建议
        sb.append("共测试").append(totalPredictions).append("注。");
        if (roi < -50 && highPrizeCount == 0) {
            sb.append("建议更换预测方法。");
        } else if (roi > 0 || highPrizeCount > 0) {
            sb.append("该策略值得继续使用。");
        }
        
        return sb.toString();
    }
}

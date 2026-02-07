package com.hobart.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.dto.VerificationHistoryDTO;
import com.hobart.lottery.domain.model.PredictionMethod;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.entity.PredictionAccuracy;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.mapper.PredictionAccuracyMapper;
import com.hobart.lottery.mapper.PredictionRecordMapper;
import com.hobart.lottery.service.learning.WeightAdjuster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 验证服务 - 比对预测结果与实际开奖结果
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService extends ServiceImpl<PredictionAccuracyMapper, PredictionAccuracy> {

    private final PredictionRecordMapper predictionRecordMapper;
    private final LotteryService lotteryService;
    private final WeightAdjuster weightAdjuster;

    /**
     * 中奖等级判定（根据大乐透官方规则）
     * 一等奖：5+2（前区5个+后区2个全中）
     * 二等奖：5+1（前区5个+后区1个）
     * 三等奖：5+0 或 4+2
     * 四等奖：4+1
     * 五等奖：4+0 或 3+2
     * 六等奖：3+1 或 2+2
     * 七等奖：3+0 或 2+1 或 1+2 或 0+2
     */
    public static String determinePrizeLevel(int frontHit, int backHit) {
        // 一等奖：5+2
        if (frontHit == 5 && backHit == 2) return "一等奖";
        // 二等奖：5+1
        if (frontHit == 5 && backHit == 1) return "二等奖";
        // 三等奖：5+0 或 4+2
        if (frontHit == 5 && backHit == 0) return "三等奖";
        if (frontHit == 4 && backHit == 2) return "三等奖";
        // 四等奖：4+1
        if (frontHit == 4 && backHit == 1) return "四等奖";
        // 五等奖：4+0 或 3+2
        if (frontHit == 4 && backHit == 0) return "五等奖";
        if (frontHit == 3 && backHit == 2) return "五等奖";
        // 六等奖：3+1 或 2+2
        if (frontHit == 3 && backHit == 1) return "六等奖";
        if (frontHit == 2 && backHit == 2) return "六等奖";
        // 七等奖：3+0 或 2+1 或 1+2 或 0+2
        if (frontHit == 3 && backHit == 0) return "七等奖";
        if (frontHit == 2 && backHit == 1) return "七等奖";
        if (frontHit == 1 && backHit == 2) return "七等奖";
        if (frontHit == 0 && backHit == 2) return "七等奖";
        // 未中奖
        return "未中奖";
    }

    /**
     * 验证某期的所有预测记录
     * @param issue 期号
     * @return 验证结果列表
     */
    @Transactional
    public List<PredictionResultDTO> verifyPredictions(String issue) {
        // 获取该期的开奖结果
        LotteryResult result = lotteryService.getByIssue(issue);
        if (result == null) {
            throw new RuntimeException("未找到期号 " + issue + " 的开奖结果");
        }

        // 获取该期未验证的预测记录
        List<PredictionRecord> unverified = predictionRecordMapper.selectUnverifiedByIssue(issue);
        if (unverified.isEmpty()) {
            return Collections.emptyList();
        }

        int[] actualFront = result.getFrontBallArray();
        int[] actualBack = result.getBackBallArray();
        Set<Integer> actualFrontSet = Arrays.stream(actualFront).boxed().collect(Collectors.toSet());
        Set<Integer> actualBackSet = Arrays.stream(actualBack).boxed().collect(Collectors.toSet());

        List<PredictionResultDTO> verifiedResults = new ArrayList<>();

        for (PredictionRecord record : unverified) {
            // 计算命中数
            int frontHit = 0, backHit = 0;

            int[] predictFront = record.getFrontBallArray();
            int[] predictBack = record.getBackBallArray();

            for (int ball : predictFront) {
                if (actualFrontSet.contains(ball)) {
                    frontHit++;
                }
            }

            for (int ball : predictBack) {
                if (actualBackSet.contains(ball)) {
                    backHit++;
                }
            }

            // 判定中奖等级
            String prizeLevel = determinePrizeLevel(frontHit, backHit);

            // 更新记录
            record.setFrontHitCount(frontHit);
            record.setBackHitCount(backHit);
            record.setPrizeLevel(prizeLevel);
            record.setIsVerified(1);
            record.setVerifiedAt(LocalDateTime.now());
            predictionRecordMapper.updateById(record);

            // 转换为DTO
            PredictionResultDTO dto = new PredictionResultDTO();
            dto.setId(record.getId());
            dto.setTargetIssue(record.getTargetIssue());
            dto.setPredictMethod(record.getPredictMethod());
            dto.setMethodName(PredictionMethod.getDisplayName(record.getPredictMethod()));
            dto.setFrontBalls(predictFront);
            dto.setBackBalls(predictBack);
            dto.setFrontBallsStr(record.getFrontBalls());
            dto.setBackBallsStr(record.getBackBalls());
            dto.setVerified(true);
            dto.setFrontHitCount(frontHit);
            dto.setBackHitCount(backHit);
            dto.setPrizeLevel(prizeLevel);

            verifiedResults.add(dto);
        }

        // 更新准确率统计
        updateAccuracyStats();
        
        // 更新持续学习权重
        try {
            weightAdjuster.adjustWeightsBatch(unverified);
            log.info("期号 {} 验证完成，已更新 {} 条预测的方法权重", issue, unverified.size());
        } catch (Exception e) {
            log.error("更新方法权重失败: {}", e.getMessage());
        }

        return verifiedResults;
    }

    /**
     * 更新准确率统计表
     */
    @Transactional
    public void updateAccuracyStats() {
        // 获取所有预测方法（从注册表动态获取，支持扩展）
        List<String> methods = PredictionMethod.getAllCodes();

        for (String method : methods) {
            // 获取该方法的所有已验证记录
            List<PredictionRecord> verified = predictionRecordMapper.selectVerifiedByMethod(method);

            if (verified.isEmpty()) {
                continue;
            }

            // 计算统计数据
            int total = verified.size();
            double frontAvg = verified.stream()
                    .mapToInt(r -> r.getFrontHitCount() != null ? r.getFrontHitCount() : 0)
                    .average().orElse(0);
            double backAvg = verified.stream()
                    .mapToInt(r -> r.getBackHitCount() != null ? r.getBackHitCount() : 0)
                    .average().orElse(0);

            // 统计各等级中奖次数
            Map<String, Long> prizeCounts = verified.stream()
                    .filter(r -> r.getPrizeLevel() != null && !r.getPrizeLevel().equals("未中奖"))
                    .collect(Collectors.groupingBy(PredictionRecord::getPrizeLevel, Collectors.counting()));

            // 查询或创建统计记录
            PredictionAccuracy accuracy = baseMapper.selectByMethod(method);
            if (accuracy == null) {
                accuracy = new PredictionAccuracy();
                accuracy.setPredictMethod(method);
            }

            accuracy.setTotalPredictions(total);
            accuracy.setFrontAvgHit(BigDecimal.valueOf(frontAvg).setScale(2, RoundingMode.HALF_UP));
            accuracy.setBackAvgHit(BigDecimal.valueOf(backAvg).setScale(2, RoundingMode.HALF_UP));
            accuracy.setPrizeCount1(prizeCounts.getOrDefault("一等奖", 0L).intValue());
            accuracy.setPrizeCount2(prizeCounts.getOrDefault("二等奖", 0L).intValue());
            accuracy.setPrizeCount3(prizeCounts.getOrDefault("三等奖", 0L).intValue());
            accuracy.setPrizeCount4(prizeCounts.getOrDefault("四等奖", 0L).intValue());
            accuracy.setPrizeCount5(prizeCounts.getOrDefault("五等奖", 0L).intValue());
            accuracy.setPrizeCount6(prizeCounts.getOrDefault("六等奖", 0L).intValue());
            accuracy.setPrizeCount7(prizeCounts.getOrDefault("七等奖", 0L).intValue());

            if (accuracy.getId() == null) {
                save(accuracy);
            } else {
                updateById(accuracy);
            }
        }
    }

    /**
     * 获取所有方法的准确率统计（带完整计算）
     */
    public List<AccuracyStatsDTO> getAllAccuracyStats() {
        return getAllAccuracyStats("composite", false);
    }

    /**
     * 获取准确率排行榜
     * @param sortBy 排序字段：composite(综合得分), hit(平均命中), prize(中奖率), high(高等奖)
     * @param ascending 是否升序
     */
    public List<AccuracyStatsDTO> getAllAccuracyStats(String sortBy, boolean ascending) {
        List<PredictionAccuracy> stats = baseMapper.selectAllStats();
        List<AccuracyStatsDTO> dtos = new ArrayList<>();

        for (PredictionAccuracy stat : stats) {
            AccuracyStatsDTO dto = convertToDTO(stat);
            dtos.add(dto);
        }

        // 排序
        sortAccuracyStats(dtos, sortBy, ascending);

        // 设置排名
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRank(i + 1);
        }

        return dtos;
    }

    /**
     * 将实体转换为DTO并计算所有派生指标
     */
    private AccuracyStatsDTO convertToDTO(PredictionAccuracy stat) {
        AccuracyStatsDTO dto = new AccuracyStatsDTO();
        dto.setPredictMethod(stat.getPredictMethod());
        dto.setMethodName(PredictionMethod.getDisplayName(stat.getPredictMethod()));
        dto.setTotalPredictions(stat.getTotalPredictions());
        dto.setFrontAvgHit(stat.getFrontAvgHit());
        dto.setBackAvgHit(stat.getBackAvgHit());
        dto.setPrizeCount1(stat.getPrizeCount1());
        dto.setPrizeCount2(stat.getPrizeCount2());
        dto.setPrizeCount3(stat.getPrizeCount3());
        dto.setPrizeCount4(stat.getPrizeCount4());
        dto.setPrizeCount5(stat.getPrizeCount5());
        dto.setPrizeCount6(stat.getPrizeCount6());
        dto.setPrizeCount7(stat.getPrizeCount7());

        // 计算总中奖次数
        int totalPrize = (stat.getPrizeCount1() != null ? stat.getPrizeCount1() : 0)
                + (stat.getPrizeCount2() != null ? stat.getPrizeCount2() : 0)
                + (stat.getPrizeCount3() != null ? stat.getPrizeCount3() : 0)
                + (stat.getPrizeCount4() != null ? stat.getPrizeCount4() : 0)
                + (stat.getPrizeCount5() != null ? stat.getPrizeCount5() : 0)
                + (stat.getPrizeCount6() != null ? stat.getPrizeCount6() : 0)
                + (stat.getPrizeCount7() != null ? stat.getPrizeCount7() : 0);
        dto.setTotalPrizeCount(totalPrize);

        // 计算中奖率
        double prizeRate = 0.0;
        if (stat.getTotalPredictions() != null && stat.getTotalPredictions() > 0) {
            prizeRate = totalPrize * 100.0 / stat.getTotalPredictions();
        }
        dto.setPrizeRate(prizeRate);

        // 计算前区命中率
        double frontHitRate = 0.0;
        if (stat.getFrontAvgHit() != null) {
            frontHitRate = stat.getFrontAvgHit().doubleValue() * 100.0 / 5.0;
        }
        dto.setFrontHitRate(frontHitRate);

        // 计算后区命中率
        double backHitRate = 0.0;
        if (stat.getBackAvgHit() != null) {
            backHitRate = stat.getBackAvgHit().doubleValue() * 100.0 / 2.0;
        }
        dto.setBackHitRate(backHitRate);

        // 计算高等奖次数
        int highPrize = (stat.getPrizeCount1() != null ? stat.getPrizeCount1() : 0)
                + (stat.getPrizeCount2() != null ? stat.getPrizeCount2() : 0)
                + (stat.getPrizeCount3() != null ? stat.getPrizeCount3() : 0);
        dto.setHighPrizeCount(highPrize);

        // 计算综合得分（加权：命中率40% + 中奖率30% + 高等奖20% + 样本量10%）
        double sampleWeight = Math.min(stat.getTotalPredictions() / 100.0, 1.0); // 样本量权重，最多100期满分
        double compositeScore = frontHitRate * 0.25 + backHitRate * 0.15 + prizeRate * 0.30 + highPrize * 2.0 + sampleWeight * 10.0;
        dto.setCompositeScore(compositeScore);

        return dto;
    }

    /**
     * 排序准确率统计
     */
    private void sortAccuracyStats(List<AccuracyStatsDTO> dtos, String sortBy, boolean ascending) {
        Comparator<AccuracyStatsDTO> comparator;

        switch (sortBy) {
            case "hit":
                comparator = Comparator.comparing(d -> d.getFrontAvgHit().doubleValue() + d.getBackAvgHit().doubleValue(), Comparator.reverseOrder());
                break;
            case "prize":
                comparator = Comparator.comparing(AccuracyStatsDTO::getPrizeRate, Comparator.reverseOrder());
                break;
            case "high":
                comparator = Comparator.comparing(AccuracyStatsDTO::getHighPrizeCount, Comparator.reverseOrder());
                break;
            case "composite":
            default:
                comparator = Comparator.comparing(AccuracyStatsDTO::getCompositeScore, Comparator.reverseOrder());
                break;
        }

        if (ascending) {
            comparator = comparator.reversed();
        }

        dtos.sort(comparator);
    }

    /**
     * 获取某方法的已验证预测记录
     */
    public List<PredictionResultDTO> getVerifiedByMethod(String method) {
        List<PredictionRecord> records = predictionRecordMapper.selectVerifiedByMethod(method);
        return records.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 获取所有未验证的期号列表（只返回有开奖结果的期号）
     */
    public List<String> getUnverifiedIssues() {
        // 查询所有未验证的预测记录
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getIsVerified, 0)
                .orderByDesc(PredictionRecord::getTargetIssue);

        List<PredictionRecord> unverified = predictionRecordMapper.selectList(wrapper);

        // 过滤出有开奖结果的期号
        return unverified.stream()
                .map(PredictionRecord::getTargetIssue)
                .distinct()
                .filter(issue -> lotteryService.getByIssue(issue) != null)
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());
    }

    /**
     * 获取验证历史记录（分页）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 验证历史列表
     */
    public List<VerificationHistoryDTO> getVerificationHistory(int page, int size) {
        // 查询已验证的预测记录
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getIsVerified, 1)
                .orderByDesc(PredictionRecord::getVerifiedAt)
                .orderByDesc(PredictionRecord::getTargetIssue);

        // 手动分页（因为需要联表查询）
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<PredictionRecord> records = predictionRecordMapper.selectList(wrapper);
        List<VerificationHistoryDTO> result = new ArrayList<>();

        for (PredictionRecord record : records) {
            // 获取该期的实际开奖结果
            LotteryResult drawResult = lotteryService.getByIssue(record.getTargetIssue());

            VerificationHistoryDTO dto = new VerificationHistoryDTO();
            dto.setId(record.getId());
            dto.setTargetIssue(record.getTargetIssue());
            dto.setPredictMethod(record.getPredictMethod());
            dto.setMethodName(PredictionMethod.getDisplayName(record.getPredictMethod()));
            dto.setFrontBallsStr(record.getFrontBalls());
            dto.setBackBallsStr(record.getBackBalls());
            dto.setFrontHitCount(record.getFrontHitCount());
            dto.setBackHitCount(record.getBackHitCount());
            dto.setPrizeLevel(record.getPrizeLevel());
            dto.setCreatedAt(record.getCreatedAt() != null ? record.getCreatedAt().toString() : "");
            dto.setVerifiedAt(record.getVerifiedAt() != null ? record.getVerifiedAt().toString() : "");

            if (drawResult != null) {
                dto.setActualFrontBallsStr(drawResult.getFrontBalls());
                dto.setActualBackBallsStr(drawResult.getBackBalls());
            } else {
                dto.setActualFrontBallsStr("-");
                dto.setActualBackBallsStr("-");
            }

            result.add(dto);
        }

        return result;
    }

    /**
     * 获取验证历史记录总数
     */
    public int countVerificationHistory() {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getIsVerified, 1);
        return Math.toIntExact(predictionRecordMapper.selectCount(wrapper));
    }

    /**
     * 检查某期是否有开奖结果
     */
    public boolean hasDrawResult(String issue) {
        return lotteryService.getByIssue(issue) != null;
    }

    private PredictionResultDTO convertToDTO(PredictionRecord record) {
        PredictionResultDTO dto = new PredictionResultDTO();
        dto.setId(record.getId());
        dto.setTargetIssue(record.getTargetIssue());
        dto.setPredictMethod(record.getPredictMethod());
        dto.setMethodName(PredictionResultDTO.getMethodDisplayName(record.getPredictMethod()));
        dto.setFrontBalls(record.getFrontBallArray());
        dto.setBackBalls(record.getBackBallArray());
        dto.setFrontBallsStr(record.getFrontBalls());
        dto.setBackBallsStr(record.getBackBalls());
        dto.setVerified(record.getIsVerified() == 1);
        dto.setFrontHitCount(record.getFrontHitCount());
        dto.setBackHitCount(record.getBackHitCount());
        dto.setPrizeLevel(record.getPrizeLevel());
        return dto;
    }
}

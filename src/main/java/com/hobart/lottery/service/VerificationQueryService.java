package com.hobart.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hobart.lottery.domain.model.PredictionMethod;
import com.hobart.lottery.dto.AccuracyStatsDTO;
import com.hobart.lottery.dto.VerificationHistoryDTO;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.entity.PredictionAccuracy;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.mapper.PredictionAccuracyMapper;
import com.hobart.lottery.mapper.PredictionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 验证查询服务 - 负责查询类操作（历史、排行、统计）
 * 从 VerificationService 拆分出来，保持单一职责
 */
@Service
@RequiredArgsConstructor
public class VerificationQueryService {

    private final PredictionAccuracyMapper accuracyMapper;
    private final PredictionRecordMapper predictionRecordMapper;
    private final LotteryService lotteryService;

    public List<AccuracyStatsDTO> getAllAccuracyStats() {
        return getAllAccuracyStats("composite", false);
    }

    public List<AccuracyStatsDTO> getAllAccuracyStats(String sortBy, boolean ascending) {
        List<PredictionAccuracy> stats = accuracyMapper.selectAllStats();
        List<AccuracyStatsDTO> dtos = stats.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        sortAccuracyStats(dtos, sortBy, ascending);

        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRank(i + 1);
        }

        return dtos;
    }

    public List<VerificationHistoryDTO> getVerificationHistory(int page, int size) {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getIsVerified, 1)
                .orderByDesc(PredictionRecord::getTargetIssue)
                .orderByDesc(PredictionRecord::getIsFinal)
                .orderByDesc(PredictionRecord::getVerifiedAt);

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<PredictionRecord> records = predictionRecordMapper.selectList(wrapper);
        List<VerificationHistoryDTO> result = new ArrayList<>();

        for (PredictionRecord record : records) {
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
            dto.setIsFinal(record.getIsFinal() != null ? record.getIsFinal() : 0);

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

    public int countVerificationHistory() {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getIsVerified, 1);
        return Math.toIntExact(predictionRecordMapper.selectCount(wrapper));
    }

    public List<String> getUnverifiedIssues() {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PredictionRecord::getIsVerified, 0)
                .orderByDesc(PredictionRecord::getTargetIssue);

        List<PredictionRecord> unverified = predictionRecordMapper.selectList(wrapper);

        return unverified.stream()
                .map(PredictionRecord::getTargetIssue)
                .distinct()
                .filter(issue -> lotteryService.getByIssue(issue) != null)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public boolean hasDrawResult(String issue) {
        return lotteryService.getByIssue(issue) != null;
    }

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

        int totalPrize = safeInt(stat.getPrizeCount1()) + safeInt(stat.getPrizeCount2())
                + safeInt(stat.getPrizeCount3()) + safeInt(stat.getPrizeCount4())
                + safeInt(stat.getPrizeCount5()) + safeInt(stat.getPrizeCount6())
                + safeInt(stat.getPrizeCount7());
        dto.setTotalPrizeCount(totalPrize);

        double prizeRate = (stat.getTotalPredictions() != null && stat.getTotalPredictions() > 0)
                ? totalPrize * 100.0 / stat.getTotalPredictions() : 0.0;
        dto.setPrizeRate(prizeRate);

        double frontHitRate = stat.getFrontAvgHit() != null ? stat.getFrontAvgHit().doubleValue() * 100.0 / 5.0 : 0.0;
        dto.setFrontHitRate(frontHitRate);

        double backHitRate = stat.getBackAvgHit() != null ? stat.getBackAvgHit().doubleValue() * 100.0 / 2.0 : 0.0;
        dto.setBackHitRate(backHitRate);

        int highPrize = safeInt(stat.getPrizeCount1()) + safeInt(stat.getPrizeCount2()) + safeInt(stat.getPrizeCount3());
        dto.setHighPrizeCount(highPrize);

        double sampleWeight = Math.min(stat.getTotalPredictions() / 100.0, 1.0);
        double compositeScore = frontHitRate * 0.25 + backHitRate * 0.15 + prizeRate * 0.30 + highPrize * 2.0 + sampleWeight * 10.0;
        dto.setCompositeScore(compositeScore);

        return dto;
    }

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

    private static int safeInt(Integer val) {
        return val != null ? val : 0;
    }
}

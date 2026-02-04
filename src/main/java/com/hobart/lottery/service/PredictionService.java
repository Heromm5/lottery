package com.hobart.lottery.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.mapper.PredictionRecordMapper;
import com.hobart.lottery.predictor.*;
import com.hobart.lottery.service.learning.AdaptivePredictor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 预测服务
 */
@Service
@RequiredArgsConstructor
public class PredictionService extends ServiceImpl<PredictionRecordMapper, PredictionRecord> {

    private final AnalysisService analysisService;
    private final LotteryService lotteryService;
    private final AdaptivePredictor adaptivePredictor;
    private final PredictionScorer predictionScorer;

    /**
     * 预测方法枚举
     */
    public enum PredictMethod {
        HOT("热号优先"),
        MISSING("遗漏回补"),
        BALANCED("冷热均衡"),
        ML("机器学习"),
        ADAPTIVE("自适应预测");

        private final String name;

        PredictMethod(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 全部生成并每方法推荐一注：每种方法生成 count 注并保存，再基于历史命中相似度为每种方法选出推荐的一注。
     *
     * @param count 每种方法生成的注数
     * @param targetIssue 目标期号
     * @return Map 含 "allPredictions"（全部 DTO）、"recommendations"（每种方法推荐的一注 DTO）
     */
    @Transactional
    public Map<String, Object> generateAllThenRecommend(int count, String targetIssue) {
        if (targetIssue == null || targetIssue.isEmpty()) {
            targetIssue = lotteryService.generateNextIssue();
        }
        List<PredictionResultDTO> allPredictions = new ArrayList<>();
        List<PredictionResultDTO> recommendations = new ArrayList<>();

        for (PredictMethod pm : PredictMethod.values()) {
            List<PredictionResultDTO> methodResults = generateByMethod(count, pm.name(), targetIssue);
            allPredictions.addAll(methodResults);
            if (methodResults.isEmpty()) {
                continue;
            }
            List<int[][]> candidates = new ArrayList<>();
            for (PredictionResultDTO dto : methodResults) {
                candidates.add(new int[][]{ dto.getFrontBalls(), dto.getBackBalls() });
            }
            int bestIndex = predictionScorer.selectBestPrediction(candidates);
            recommendations.add(methodResults.get(bestIndex));
        }

        Map<String, Object> out = new HashMap<>();
        out.put("allPredictions", allPredictions);
        out.put("recommendations", recommendations);
        return out;
    }

    /**
     * 生成预测并保存到数据库
     * @param count 生成注数
     * @param method 预测方法（null则使用所有方法）
     * @param targetIssue 目标期号
     * @return 预测结果列表
     */
    @Transactional
    public List<PredictionResultDTO> generateAndSavePredictions(int count, String method, String targetIssue) {
        List<PredictionResultDTO> results = new ArrayList<>();
        
        if (targetIssue == null || targetIssue.isEmpty()) {
            targetIssue = lotteryService.generateNextIssue();
        }
        
        if (method == null || method.isEmpty() || "ALL".equalsIgnoreCase(method)) {
            // 使用所有方法，每种方法生成 count 注
            for (PredictMethod pm : PredictMethod.values()) {
                List<PredictionResultDTO> methodResults = generateByMethod(count, pm.name(), targetIssue);
                results.addAll(methodResults);
            }
        } else {
            // 使用指定方法
            results = generateByMethod(count, method.toUpperCase(), targetIssue);
        }
        
        return results;
    }

    /**
     * 生成预测并从中选择最优的一注
     * 每种方法先生成多注，然后基于历史命中相似度选择最优的一注保存
     * 
     * @param candidateCount 每种方法生成的候选注数
     * @param targetIssue 目标期号
     * @return 每种方法的最优预测结果
     */
    @Transactional
    public List<PredictionResultDTO> generateBestPredictions(int candidateCount, String targetIssue) {
        List<PredictionResultDTO> results = new ArrayList<>();
        
        if (targetIssue == null || targetIssue.isEmpty()) {
            targetIssue = lotteryService.generateNextIssue();
        }
        
        for (PredictMethod pm : PredictMethod.values()) {
            PredictionResultDTO best = generateBestByMethod(candidateCount, pm.name(), targetIssue);
            if (best != null) {
                results.add(best);
            }
        }
        
        return results;
    }

    /**
     * 使用指定方法生成多注预测，并选择最优的一注保存
     */
    private PredictionResultDTO generateBestByMethod(int candidateCount, String method, String targetIssue) {
        BasePredictor predictor = createPredictor(method);
        if (predictor == null) {
            return null;
        }
        
        // 生成候选预测
        List<int[][]> candidates = predictor.predictMultiple(candidateCount);
        if (candidates.isEmpty()) {
            return null;
        }
        
        // 选择最优的一注
        int bestIndex = predictionScorer.selectBestPrediction(candidates);
        int[][] bestPrediction = candidates.get(bestIndex);
        
        // 保存最优预测到数据库
        PredictionRecord record = new PredictionRecord();
        record.setTargetIssue(targetIssue);
        record.setPredictMethod(method);
        record.setFrontBallArray(bestPrediction[0]);
        record.setBackBallArray(bestPrediction[1]);
        record.setIsVerified(0);
        
        save(record);
        
        // 转换为DTO
        PredictionResultDTO dto = new PredictionResultDTO();
        dto.setId(record.getId());
        dto.setTargetIssue(targetIssue);
        dto.setPredictMethod(method);
        dto.setMethodName(PredictionResultDTO.getMethodDisplayName(method));
        dto.setFrontBalls(bestPrediction[0]);
        dto.setBackBalls(bestPrediction[1]);
        dto.setFrontBallsStr(record.getFrontBalls());
        dto.setBackBallsStr(record.getBackBalls());
        dto.setVerified(false);
        
        return dto;
    }

    /**
     * 使用指定方法生成预测
     */
    private List<PredictionResultDTO> generateByMethod(int count, String method, String targetIssue) {
        BasePredictor predictor = createPredictor(method);
        if (predictor == null) {
            return Collections.emptyList();
        }
        
        List<int[][]> predictions = predictor.predictMultiple(count);
        List<PredictionResultDTO> results = new ArrayList<>();
        
        for (int[][] prediction : predictions) {
            // 创建记录
            PredictionRecord record = new PredictionRecord();
            record.setTargetIssue(targetIssue);
            record.setPredictMethod(method);
            record.setFrontBallArray(prediction[0]);
            record.setBackBallArray(prediction[1]);
            record.setIsVerified(0);
            
            // 保存到数据库
            save(record);
            
            // 转换为DTO
            PredictionResultDTO dto = new PredictionResultDTO();
            dto.setId(record.getId());
            dto.setTargetIssue(targetIssue);
            dto.setPredictMethod(method);
            dto.setMethodName(PredictionResultDTO.getMethodDisplayName(method));
            dto.setFrontBalls(prediction[0]);
            dto.setBackBalls(prediction[1]);
            dto.setFrontBallsStr(record.getFrontBalls());
            dto.setBackBallsStr(record.getBackBalls());
            dto.setVerified(false);
            
            results.add(dto);
        }
        
        return results;
    }

    /**
     * 创建预测器
     */
    private BasePredictor createPredictor(String method) {
        return switch (method.toUpperCase()) {
            case "HOT" -> new HotNumberPredictor(analysisService);
            case "MISSING" -> new MissingPredictor(analysisService);
            case "BALANCED" -> new BalancedPredictor(analysisService);
            case "ML" -> new MLPredictor(analysisService, lotteryService);
            case "ADAPTIVE" -> new AdaptivePredictorWrapper(adaptivePredictor);
            default -> null;
        };
    }

    /**
     * 获取某期的预测记录
     */
    public List<PredictionResultDTO> getPredictionsByIssue(String targetIssue) {
        List<PredictionRecord> records = baseMapper.selectByTargetIssue(targetIssue);
        return convertToDTO(records);
    }

    /**
     * 获取未验证的预测记录
     */
    public List<PredictionRecord> getUnverifiedByIssue(String targetIssue) {
        return baseMapper.selectUnverifiedByIssue(targetIssue);
    }

    /**
     * 获取最近的预测记录
     */
    public List<PredictionResultDTO> getRecentPredictions(int limit) {
        List<PredictionRecord> records = baseMapper.selectRecentRecords(limit);
        return convertToDTO(records);
    }

    /**
     * 获取所有预测方法
     */
    public List<Map<String, String>> getAllMethods() {
        List<Map<String, String>> methods = new ArrayList<>();
        for (PredictMethod pm : PredictMethod.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("code", pm.name());
            map.put("name", pm.getName());
            methods.add(map);
        }
        return methods;
    }

    /**
     * 转换为DTO列表
     */
    private List<PredictionResultDTO> convertToDTO(List<PredictionRecord> records) {
        List<PredictionResultDTO> dtos = new ArrayList<>();
        for (PredictionRecord record : records) {
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
            dtos.add(dto);
        }
        return dtos;
    }
}

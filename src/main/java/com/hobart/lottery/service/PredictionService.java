package com.hobart.lottery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hobart.lottery.domain.model.PredictionMethod;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.mapper.PredictionRecordMapper;
import com.hobart.lottery.predictor.BasePredictor;
import com.hobart.lottery.predictor.PredictorRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PredictionService extends ServiceImpl<PredictionRecordMapper, PredictionRecord> {

    private final LotteryService lotteryService;
    private final PredictionScorer predictionScorer;
    private final PredictorRegistry predictorRegistry;

    @Transactional
    public Map<String, Object> generateAllThenRecommend(int count, String targetIssue) {
        if (targetIssue == null || targetIssue.isEmpty()) {
            targetIssue = lotteryService.generateNextIssue();
        }
        List<PredictionResultDTO> allPredictions = new ArrayList<>();
        List<PredictionResultDTO> recommendations = new ArrayList<>();

        for (PredictionMethod pm : PredictionMethod.getAllMethods()) {
            List<PredictionResultDTO> methodResults = generateByMethod(count, pm.getCode(), targetIssue);
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

    @Transactional
    public List<PredictionResultDTO> generateAndSavePredictions(int count, String method, String targetIssue) {
        List<PredictionResultDTO> results = new ArrayList<>();
        
        if (targetIssue == null || targetIssue.isEmpty()) {
            targetIssue = lotteryService.generateNextIssue();
        }
        
        if (method == null || method.isEmpty() || "ALL".equalsIgnoreCase(method)) {
            for (PredictionMethod pm : PredictionMethod.getAllMethods()) {
                List<PredictionResultDTO> methodResults = generateByMethod(count, pm.getCode(), targetIssue);
                results.addAll(methodResults);
            }
        } else {
            results = generateByMethod(count, method.toUpperCase(), targetIssue);
        }
        
        return results;
    }

    @Transactional
    public List<PredictionResultDTO> generateBestPredictions(int candidateCount, String targetIssue) {
        List<PredictionResultDTO> results = new ArrayList<>();
        
        if (targetIssue == null || targetIssue.isEmpty()) {
            targetIssue = lotteryService.generateNextIssue();
        }
        
        for (PredictionMethod pm : PredictionMethod.getAllMethods()) {
            PredictionResultDTO best = generateBestByMethod(candidateCount, pm.getCode(), targetIssue);
            if (best != null) {
                results.add(best);
            }
        }
        
        return results;
    }

    private PredictionResultDTO generateBestByMethod(int candidateCount, String method, String targetIssue) {
        BasePredictor predictor = predictorRegistry.get(method);
        if (predictor == null) {
            return null;
        }
        
        List<int[][]> candidates = predictor.predictMultiple(candidateCount);
        if (candidates.isEmpty()) {
            return null;
        }
        
        int bestIndex = predictionScorer.selectBestPrediction(candidates);
        int[][] bestPrediction = candidates.get(bestIndex);
        
        PredictionRecord record = new PredictionRecord();
        record.setTargetIssue(targetIssue);
        record.setPredictMethod(method);
        record.setFrontBallArray(bestPrediction[0]);
        record.setBackBallArray(bestPrediction[1]);
        record.setIsVerified(0);
        
        save(record);
        
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

    private List<PredictionResultDTO> generateByMethod(int count, String method, String targetIssue) {
        BasePredictor predictor = predictorRegistry.get(method);
        if (predictor == null) {
            return Collections.emptyList();
        }
        
        List<int[][]> predictions = predictor.predictMultiple(count);
        List<PredictionResultDTO> results = new ArrayList<>();
        
        for (int[][] prediction : predictions) {
            PredictionRecord record = new PredictionRecord();
            record.setTargetIssue(targetIssue);
            record.setPredictMethod(method);
            record.setFrontBallArray(prediction[0]);
            record.setBackBallArray(prediction[1]);
            record.setIsVerified(0);
            
            save(record);
            
            PredictionResultDTO dto = new PredictionResultDTO();
            dto.setId(record.getId());
            dto.setTargetIssue(targetIssue);
            dto.setPredictMethod(method);
            dto.setMethodName(PredictionMethod.getDisplayName(method));
            dto.setFrontBalls(prediction[0]);
            dto.setBackBalls(prediction[1]);
            dto.setFrontBallsStr(record.getFrontBalls());
            dto.setBackBallsStr(record.getBackBalls());
            dto.setVerified(false);
            
            results.add(dto);
        }
        
        return results;
    }

    public List<PredictionResultDTO> getPredictionsByIssue(String targetIssue) {
        List<PredictionRecord> records = baseMapper.selectByTargetIssue(targetIssue);
        return convertToDTO(records);
    }

    public List<PredictionRecord> getUnverifiedByIssue(String targetIssue) {
        return baseMapper.selectUnverifiedByIssue(targetIssue);
    }

    public List<PredictionResultDTO> getRecentPredictions(int limit) {
        List<PredictionRecord> records = baseMapper.selectRecentRecords(limit);
        return convertToDTO(records);
    }

    public String getNextPredictionIssue() {
        String maxTarget = baseMapper.selectMaxTargetIssue();
        if (maxTarget != null && !maxTarget.isEmpty()) {
            try {
                int num = Integer.parseInt(maxTarget.trim());
                return String.valueOf(num + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        return lotteryService.generateNextIssue();
    }

    public IPage<PredictionRecord> pageByFilter(Page<PredictionRecord> pageParam, String status) {
        LambdaQueryWrapper<PredictionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PredictionRecord::getTargetIssue)
                .orderByDesc(PredictionRecord::getIsFinal);
        if ("verified".equalsIgnoreCase(status)) {
            wrapper.eq(PredictionRecord::getIsVerified, 1);
        } else if ("unverified".equalsIgnoreCase(status)) {
            wrapper.eq(PredictionRecord::getIsVerified, 0);
        }
        return page(pageParam, wrapper);
    }

    @Transactional
    public void markAsFinal(List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return;
        }
        for (Long id : recordIds) {
            PredictionRecord record = getById(id);
            if (record != null) {
                record.setIsFinal(1);
                updateById(record);
            }
        }
    }

    public List<Map<String, String>> getAllMethods() {
        List<Map<String, String>> methods = new ArrayList<>();
        for (PredictionMethod pm : PredictionMethod.getAllMethods()) {
            Map<String, String> map = new HashMap<>();
            map.put("code", pm.getCode());
            map.put("name", pm.getDisplayName());
            methods.add(map);
        }
        return methods;
    }

    private List<PredictionResultDTO> convertToDTO(List<PredictionRecord> records) {
        List<PredictionResultDTO> dtos = new ArrayList<>();
        for (PredictionRecord record : records) {
            PredictionResultDTO dto = new PredictionResultDTO();
            dto.setId(record.getId());
            dto.setTargetIssue(record.getTargetIssue());
            dto.setPredictMethod(record.getPredictMethod());
            dto.setMethodName(PredictionMethod.getDisplayName(record.getPredictMethod()));
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

package com.hobart.lottery.service.learning;

import com.hobart.lottery.config.LotteryConfig;
import com.hobart.lottery.entity.MethodWeight;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.mapper.MethodWeightMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权重调整器
 * 实现持续学习机制：根据预测结果动态调整各方法的权重
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeightAdjuster {
    
    private final MethodWeightMapper weightMapper;
    private final LotteryConfig config;
    
    /**
     * 根据验证结果调整权重
     * 
     * @param record 已验证的预测记录
     */
    @Transactional
    public void adjustWeight(PredictionRecord record) {
        if (record.getIsVerified() != 1) {
            log.warn("预测记录未验证，跳过权重调整: {}", record.getId());
            return;
        }
        
        String methodCode = record.getPredictMethod();
        MethodWeight mw = weightMapper.selectByMethodCode(methodCode);
        
        if (mw == null) {
            log.warn("未找到方法权重记录: {}", methodCode);
            return;
        }
        
        // 判断是否命中
        boolean isHit = isHit(record);
        
        // 更新统计
        mw.setTotalPredictions(mw.getTotalPredictions() + 1);
        if (isHit) {
            mw.setTotalHits(mw.getTotalHits() + 1);
        }
        
        // EMA 更新命中率
        double alpha = config.getLearning().getEmaAlpha();
        double newHitRate = isHit ? 1.0 : 0.0;
        double currentRate = mw.getHitRate() != null ? mw.getHitRate().doubleValue() : 0.0;
        double smoothedRate = alpha * newHitRate + (1 - alpha) * currentRate;
        
        mw.setHitRate(BigDecimal.valueOf(smoothedRate).setScale(4, RoundingMode.HALF_UP));
        mw.setUpdatedAt(LocalDateTime.now());
        
        weightMapper.updateById(mw);
        
        log.info("更新方法 {} 的命中率: {} -> {}, 是否命中: {}", 
            methodCode, currentRate, smoothedRate, isHit);
        
        // 重新计算所有权重
        recalculateAllWeights();
    }
    
    /**
     * 批量调整权重（验证一期所有预测后调用）
     * 
     * @param records 同一期的所有预测记录
     */
    @Transactional
    public void adjustWeightsBatch(List<PredictionRecord> records) {
        for (PredictionRecord record : records) {
            if (record.getIsVerified() == 1) {
                adjustWeightInternal(record);
            }
        }
        recalculateAllWeights();
    }
    
    /**
     * 内部调整方法（不重新计算权重）
     */
    private void adjustWeightInternal(PredictionRecord record) {
        String methodCode = record.getPredictMethod();
        MethodWeight mw = weightMapper.selectByMethodCode(methodCode);
        
        if (mw == null) return;
        
        boolean isHit = isHit(record);
        
        mw.setTotalPredictions(mw.getTotalPredictions() + 1);
        if (isHit) {
            mw.setTotalHits(mw.getTotalHits() + 1);
        }
        
        double alpha = config.getLearning().getEmaAlpha();
        double newHitRate = isHit ? 1.0 : 0.0;
        double currentRate = mw.getHitRate() != null ? mw.getHitRate().doubleValue() : 0.0;
        double smoothedRate = alpha * newHitRate + (1 - alpha) * currentRate;
        
        mw.setHitRate(BigDecimal.valueOf(smoothedRate).setScale(4, RoundingMode.HALF_UP));
        mw.setUpdatedAt(LocalDateTime.now());
        
        weightMapper.updateById(mw);
    }
    
    /**
     * 判断预测是否命中
     */
    private boolean isHit(PredictionRecord record) {
        int frontHit = record.getFrontHitCount() != null ? record.getFrontHitCount() : 0;
        int backHit = record.getBackHitCount() != null ? record.getBackHitCount() : 0;
        
        int frontThreshold = config.getLearning().getFrontHitThreshold();
        int backThreshold = config.getLearning().getBackHitThreshold();
        
        return frontHit >= frontThreshold || backHit >= backThreshold;
    }
    
    /**
     * 重新计算所有方法的权重（归一化）
     */
    @Transactional
    public void recalculateAllWeights() {
        List<MethodWeight> all = weightMapper.selectAllOrderByWeight();
        
        // 计算命中率总和
        double totalRate = all.stream()
            .mapToDouble(mw -> mw.getHitRate() != null ? mw.getHitRate().doubleValue() : 0.0)
            .sum();
        
        // 如果总和为0（初始状态），使用均等权重
        double defaultWeight = config.getLearning().getInitialWeight();
        
        for (MethodWeight mw : all) {
            double newWeight;
            if (totalRate > 0) {
                double rate = mw.getHitRate() != null ? mw.getHitRate().doubleValue() : 0.0;
                newWeight = rate / totalRate;
            } else {
                newWeight = defaultWeight;
            }
            
            mw.setWeight(BigDecimal.valueOf(newWeight).setScale(4, RoundingMode.HALF_UP));
            mw.setUpdatedAt(LocalDateTime.now());
            weightMapper.updateById(mw);
        }
        
        log.info("重新计算所有方法权重完成");
    }
    
    /**
     * 获取所有方法的当前权重
     * 
     * @return 方法代码 -> 权重 映射
     */
    public Map<String, Double> getMethodWeights() {
        List<MethodWeight> all = weightMapper.selectAllOrderByWeight();
        Map<String, Double> weights = new HashMap<>();
        
        for (MethodWeight mw : all) {
            double weight = mw.getWeight() != null ? mw.getWeight().doubleValue() : 0.2;
            weights.put(mw.getMethodCode(), weight);
        }
        
        return weights;
    }
    
    /**
     * 获取所有方法的权重详情
     */
    public List<MethodWeight> getAllMethodWeights() {
        return weightMapper.selectAllOrderByWeight();
    }
    
    /**
     * 重置所有权重为初始值
     */
    @Transactional
    public void resetWeights() {
        List<MethodWeight> all = weightMapper.selectAllOrderByWeight();
        double initialWeight = config.getLearning().getInitialWeight();
        
        for (MethodWeight mw : all) {
            mw.setWeight(BigDecimal.valueOf(initialWeight));
            mw.setHitRate(BigDecimal.ZERO);
            mw.setTotalPredictions(0);
            mw.setTotalHits(0);
            mw.setUpdatedAt(LocalDateTime.now());
            weightMapper.updateById(mw);
        }
        
        log.info("重置所有方法权重为初始值: {}", initialWeight);
    }
}

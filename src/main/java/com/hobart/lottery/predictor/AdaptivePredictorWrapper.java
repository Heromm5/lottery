package com.hobart.lottery.predictor;

import com.hobart.lottery.service.learning.AdaptivePredictor;

import java.util.List;

/**
 * 自适应预测器包装类
 * 将 AdaptivePredictor 包装为 BasePredictor 接口，统一预测入口
 */
public class AdaptivePredictorWrapper extends BasePredictor {

    private final AdaptivePredictor adaptivePredictor;

    public AdaptivePredictorWrapper(AdaptivePredictor adaptivePredictor) {
        super(null); // 不需要 AnalysisService，AdaptivePredictor 内部已有
        this.adaptivePredictor = adaptivePredictor;
    }

    @Override
    public String getMethodName() {
        return "自适应预测";
    }

    @Override
    public String getMethodCode() {
        return "ADAPTIVE";
    }

    @Override
    public int[][] predict() {
        return adaptivePredictor.predict();
    }

    @Override
    public List<int[][]> predictMultiple(int count) {
        return adaptivePredictor.predictMultiple(count);
    }
}

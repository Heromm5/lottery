package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.service.AnalysisService;

import java.util.List;

/**
 * 热号优先预测器
 * 选择近30期出现频率最高的号码
 */
public class HotNumberPredictor extends BasePredictor {

    public HotNumberPredictor(AnalysisService analysisService) {
        super(analysisService);
    }

    @Override
    public String getMethodName() {
        return "热号优先";
    }

    @Override
    public String getMethodCode() {
        return "HOT";
    }

    @Override
    public int[][] predict() {
        // 获取前区热号（取前15个作为候选池）
        List<Integer> hotFront = analysisService.getHotFrontNumbers(15);
        // 获取后区热号（取前6个作为候选池）
        List<Integer> hotBack = analysisService.getHotBackNumbers(6);
        
        // 从热号中随机选择
        int[] front = selectFromCandidates(hotFront, 5, 
            NumberZone.FRONT.getMin(), NumberZone.FRONT.getMax());
        int[] back = selectFromCandidates(hotBack, 2, 
            NumberZone.BACK.getMin(), NumberZone.BACK.getMax());
        
        return new int[][]{front, back};
    }
}

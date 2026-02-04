package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.service.AnalysisService;

import java.util.List;

/**
 * 遗漏回补预测器
 * 选择遗漏值接近平均遗漏的号码
 */
public class MissingPredictor extends BasePredictor {

    public MissingPredictor(AnalysisService analysisService) {
        super(analysisService);
    }

    @Override
    public String getMethodName() {
        return "遗漏回补";
    }

    @Override
    public String getMethodCode() {
        return "MISSING";
    }

    @Override
    public int[][] predict() {
        // 获取遗漏到期的前区号码（取前15个作为候选池）
        List<Integer> missingFront = analysisService.getMissingDueFrontNumbers(15);
        // 获取遗漏到期的后区号码（取前6个作为候选池）
        List<Integer> missingBack = analysisService.getMissingDueBackNumbers(6);
        
        // 从候选中随机选择
        int[] front = selectFromCandidates(missingFront, 5, 
            NumberZone.FRONT.getMin(), NumberZone.FRONT.getMax());
        int[] back = selectFromCandidates(missingBack, 2, 
            NumberZone.BACK.getMin(), NumberZone.BACK.getMax());
        
        return new int[][]{front, back};
    }
}

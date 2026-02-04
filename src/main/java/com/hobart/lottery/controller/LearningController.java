package com.hobart.lottery.controller;

import com.hobart.lottery.entity.MethodWeight;
import com.hobart.lottery.service.learning.AdaptivePredictor;
import com.hobart.lottery.service.learning.WeightAdjuster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习权重控制器
 * 展示和管理各预测方法的权重
 */
@Controller
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningController {

    private final WeightAdjuster weightAdjuster;
    private final AdaptivePredictor adaptivePredictor;

    /**
     * 权重展示页面
     */
    @GetMapping("/weights")
    public String weights(Model model) {
        List<MethodWeight> methodWeights = weightAdjuster.getAllMethodWeights();
        model.addAttribute("methodWeights", methodWeights);
        return "learning/weights";
    }

    /**
     * 获取所有方法权重API
     */
    @GetMapping("/api/weights")
    @ResponseBody
    public List<MethodWeight> getWeights() {
        return weightAdjuster.getAllMethodWeights();
    }

    /**
     * 获取权重概览数据（用于图表）
     */
    @GetMapping("/api/weights/chart")
    @ResponseBody
    public Map<String, Object> getWeightsChartData() {
        List<MethodWeight> weights = weightAdjuster.getAllMethodWeights();
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", weights.stream()
            .map(w -> w.getMethodName() != null ? w.getMethodName() : w.getMethodCode())
            .toArray());
        result.put("weights", weights.stream()
            .map(w -> w.getWeight() != null ? w.getWeight().doubleValue() : 0.2)
            .toArray());
        result.put("hitRates", weights.stream()
            .map(w -> {
                double rate = w.getHitRate() != null ? w.getHitRate().doubleValue() * 100 : 0.0;
                return Math.round(rate * 100.0) / 100.0;
            })
            .toArray());
        result.put("predictions", weights.stream()
            .map(w -> w.getTotalPredictions() != null ? w.getTotalPredictions() : 0)
            .toArray());
        result.put("hits", weights.stream()
            .map(w -> w.getTotalHits() != null ? w.getTotalHits() : 0)
            .toArray());
        
        return result;
    }

    /**
     * 重置所有权重
     */
    @PostMapping("/api/weights/reset")
    @ResponseBody
    public Map<String, Object> resetWeights() {
        weightAdjuster.resetWeights();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "权重已重置为初始值");
        
        return result;
    }

    /**
     * 使用自适应预测生成号码（仅预览，不保存）
     * 
     * 注意：此接口仅用于快速预览自适应预测效果
     * 如需保存预测记录并参与验证，请使用 /prediction/generate?method=ADAPTIVE
     */
    @GetMapping("/api/predict")
    @ResponseBody
    public Map<String, Object> adaptivePredict(@RequestParam(defaultValue = "1") Integer count) {
        Map<String, Object> result = new HashMap<>();
        result.put("predictions", adaptivePredictor.predictMultiple(count));
        result.put("weights", adaptivePredictor.getCurrentWeights());
        result.put("notice", "此为预览，如需保存记录请前往预测页面选择'自适应预测'方法");
        
        return result;
    }
}

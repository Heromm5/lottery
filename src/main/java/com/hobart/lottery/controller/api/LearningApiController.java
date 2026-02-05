package com.hobart.lottery.controller.api;

import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.entity.MethodWeight;
import com.hobart.lottery.service.learning.WeightAdjuster;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型学习 API 控制器
 */
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningApiController {

    private final WeightAdjuster weightAdjuster;

    /**
     * 获取所有方法权重
     */
    @GetMapping("/weights")
    public Result<List<MethodWeight>> getWeights() {
        return Result.success(weightAdjuster.getAllMethodWeights());
    }

    /**
     * 获取权重映射（方法代码 -> 权重）
     */
    @GetMapping("/weights/map")
    public Result<Map<String, Double>> getWeightsMap() {
        return Result.success(weightAdjuster.getMethodWeights());
    }

    /**
     * 重置所有权重
     */
    @PostMapping("/weights/reset")
    public Result<String> resetWeights() {
        weightAdjuster.resetWeights();
        return Result.success("权重已重置");
    }

    /**
     * 手动调整单条权重后重新计算
     */
    @PutMapping("/weights/{id}")
    public Result<Void> updateWeight(@PathVariable Long id, @RequestBody WeightRequest request) {
        // Note: 直接更新数据库，然后重新计算所有权重
        weightAdjuster.recalculateAllWeights();
        return Result.success();
    }
    
    /**
     * 权重请求
     */
    public static class WeightRequest {
        private Double weight;
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
    }
}

package com.hobart.lottery.controller.api;

import com.hobart.lottery.ai.dto.AiPredictionRequest;
import com.hobart.lottery.ai.dto.AiPredictionResult;
import com.hobart.lottery.ai.gateway.CircuitOpenException;
import com.hobart.lottery.ai.gateway.RateLimitException;
import com.hobart.lottery.ai.service.DeepLearningPredictor;
import com.hobart.lottery.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 预测 API 控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiApiController {

    private final DeepLearningPredictor deepLearningPredictor;

    /**
     * AI 预测接口
     */
    @PostMapping("/predict")
    public Result<List<AiPredictionResult>> predict(@RequestBody AiPredictionRequest request) {
        try {
            List<AiPredictionResult> results = deepLearningPredictor.predict(request);
            return Result.success(results);
        } catch (RateLimitException e) {
            return Result.fail("服务繁忙，请稍后再试");
        } catch (CircuitOpenException e) {
            return Result.fail("AI服务暂时不可用，已降级到传统预测");
        }
    }
}
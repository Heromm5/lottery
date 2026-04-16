package com.hobart.lottery.controller.api;

import com.hobart.lottery.ai.dto.AiPredictionRequest;
import com.hobart.lottery.ai.dto.AiPredictionResult;
import com.hobart.lottery.ai.dto.AnalyzePatternsRequest;
import com.hobart.lottery.ai.dto.AnomalyAlert;
import com.hobart.lottery.ai.dto.DiscoveredPattern;
import com.hobart.lottery.ai.dto.GenerateReportRequest;
import com.hobart.lottery.ai.dto.AiAnalysisReport;
import com.hobart.lottery.ai.gateway.CircuitOpenException;
import com.hobart.lottery.ai.gateway.RateLimitException;
import com.hobart.lottery.ai.automation.AnomalyDetector;
import com.hobart.lottery.ai.automation.ReportGenerator;
import com.hobart.lottery.ai.automation.RuleDiscovery;
import com.hobart.lottery.ai.service.DeepLearningPredictor;
import com.hobart.lottery.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 预测 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiApiController {

    private final DeepLearningPredictor deepLearningPredictor;
    private final RuleDiscovery ruleDiscovery;
    private final ReportGenerator reportGenerator;
    private final AnomalyDetector anomalyDetector;

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

    /**
     * 模式分析接口
     * 分析历史数据，发现号码规律和模式
     */
    @PostMapping("/analyze-patterns")
    public Result<List<DiscoveredPattern>> analyzePatterns(@RequestBody AnalyzePatternsRequest request) {
        try {
            List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(
                request.getPeriods() != null ? request.getPeriods() : 100
            );
            return Result.success(patterns);
        } catch (Exception e) {
            log.error("Pattern analysis failed", e);
            return Result.fail("模式分析失败: " + e.getMessage());
        }
    }

    /**
     * 报告生成接口
     * 生成 AI 分析报告（日报/周报/期次报告）
     */
    @PostMapping("/report")
    public Result<AiAnalysisReport> generateReport(@RequestBody GenerateReportRequest request) {
        try {
            AiAnalysisReport report = reportGenerator.generateReport(
                request.getReportType(),
                request.getStartIssue(),
                request.getEndIssue()
            );
            return Result.success(report);
        } catch (Exception e) {
            log.error("Report generation failed", e);
            return Result.fail("报告生成失败: " + e.getMessage());
        }
    }

    /**
     * 异常告警查询接口
     * 查询系统中的异常告警
     */
    @GetMapping("/anomaly-alerts")
    public Result<List<AnomalyAlert>> getAnomalyAlerts(
        @RequestParam(required = false) String severity,
        @RequestParam(required = false) Boolean acknowledged
    ) {
        List<AnomalyAlert> alerts = anomalyDetector.getAlerts(severity, acknowledged);
        return Result.success(alerts);
    }
}
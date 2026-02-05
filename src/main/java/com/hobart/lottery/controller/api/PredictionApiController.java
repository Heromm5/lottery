package com.hobart.lottery.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.service.PredictionService;
import com.hobart.lottery.service.PredictionScorer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预测 API 控制器
 */
@RestController
@RequestMapping("/api/prediction")
@RequiredArgsConstructor
public class PredictionApiController {

    private final PredictionService predictionService;
    private final PredictionScorer predictionScorer;

    /**
     * 获取所有预测方法
     */
    @GetMapping("/methods")
    public Result<List<Map<String, String>>> getMethods() {
        return Result.success(predictionService.getAllMethods());
    }

    /**
     * 生成预测并保存
     */
    @PostMapping("/generate")
    public Result<List<PredictionResultDTO>> generate(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String targetIssue) {
        List<PredictionResultDTO> results = predictionService.generateAndSavePredictions(count, method, targetIssue);
        return Result.success(results);
    }

    /**
     * 生成最优预测（每种方法选最优一注）
     */
    @PostMapping("/generate/best")
    public Result<List<PredictionResultDTO>> generateBest(
            @RequestParam(defaultValue = "10") int candidateCount,
            @RequestParam(required = false) String targetIssue) {
        List<PredictionResultDTO> results = predictionService.generateBestPredictions(candidateCount, targetIssue);
        return Result.success(results);
    }

    /**
     * 获取某期预测记录
     */
    @GetMapping("/issue/{issue}")
    public Result<List<PredictionResultDTO>> getByIssue(@PathVariable String issue) {
        return Result.success(predictionService.getPredictionsByIssue(issue));
    }

    /**
     * 获取最近的预测记录
     */
    @GetMapping("/recent")
    public Result<List<PredictionResultDTO>> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(predictionService.getRecentPredictions(limit));
    }

    /**
     * 分页查询预测记录
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PredictionRecord> pageParam = new Page<>(page, size);
        IPage<PredictionRecord> resultPage = predictionService.page(pageParam);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("size", resultPage.getSize());
        result.put("current", resultPage.getCurrent());
        result.put("pages", resultPage.getPages());
        
        return Result.success(result);
    }

    /**
     * 获取预测详情
     */
    @GetMapping("/{id}")
    public Result<PredictionRecord> getDetail(@PathVariable Long id) {
        PredictionRecord record = predictionService.getById(id);
        if (record != null) {
            return Result.success(record);
        }
        return Result.fail("预测记录不存在");
    }

    /**
     * 获取未验证的预测期号列表
     */
    @GetMapping("/unverified/issues")
    public Result<List<String>> getUnverifiedIssues() {
        List<PredictionRecord> unverified = predictionService.getUnverifiedByIssue(null);
        List<String> issues = unverified.stream()
                .map(PredictionRecord::getTargetIssue)
                .distinct()
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());
        return Result.success(issues);
    }

    /**
     * 为预测结果评分（独立评分，返回带分数的结果）
     */
    @PostMapping("/score")
    public Result<List<PredictionResultDTO>> scorePredictions(@RequestBody List<PredictionResultDTO> predictions) {
        List<PredictionResultDTO> scored = predictionScorer.scorePredictions(predictions);
        return Result.success(scored);
    }
}

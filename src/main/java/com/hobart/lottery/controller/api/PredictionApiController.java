package com.hobart.lottery.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.dto.GeneratePinnedRequest;
import com.hobart.lottery.dto.PredictionRecordListDTO;
import com.hobart.lottery.dto.PredictionResultDTO;
import com.hobart.lottery.entity.PredictionRecord;
import com.hobart.lottery.service.PredictionService;
import com.hobart.lottery.service.PredictionScorer;
import com.hobart.lottery.service.prediction.PredictionDisplayNames;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

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
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int count,
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
     * 定胆生成预测并保存（胆码必出现在每注中）
     */
    @PostMapping("/generate-pinned")
    public Result<List<PredictionResultDTO>> generatePinned(@Valid @RequestBody GeneratePinnedRequest body) {
        List<PredictionResultDTO> results = predictionService.generateAndSavePinnedPredictions(body);
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
     * 分页查询预测记录（按预测期号倒序，支持全部/已开奖/未开奖筛选）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @RequestParam(required = false, defaultValue = "all") String status) {
        String filterStatus = (status == null || status.isEmpty()) ? "all" : status;
        Page<PredictionRecord> pageParam = new Page<>(page, size);
        IPage<PredictionRecord> resultPage = predictionService.pageByFilter(pageParam, filterStatus);
        
        List<PredictionRecordListDTO> records = resultPage.getRecords().stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", resultPage.getTotal());
        result.put("size", resultPage.getSize());
        result.put("current", resultPage.getCurrent());
        result.put("pages", resultPage.getPages());
        
        return Result.success(result);
    }

    private PredictionRecordListDTO toListDTO(PredictionRecord r) {
        PredictionRecordListDTO dto = new PredictionRecordListDTO();
        dto.setId(r.getId());
        dto.setTargetIssue(r.getTargetIssue());
        dto.setPredictMethod(r.getPredictMethod());
        dto.setGenerationMode(r.getGenerationMode());
        dto.setLockedFrontBalls(r.getLockedFrontBalls());
        dto.setLockedBackBalls(r.getLockedBackBalls());
        dto.setMethodName(PredictionDisplayNames.forMethodAndMode(r.getPredictMethod(), r.getGenerationMode()));
        dto.setFrontBalls(r.getFrontBalls());
        dto.setBackBalls(r.getBackBalls());
        dto.setIsVerified(r.getIsVerified());
        dto.setIsFinal(r.getIsFinal() != null ? r.getIsFinal() : 0);
        dto.setFrontHitCount(r.getFrontHitCount());
        dto.setBackHitCount(r.getBackHitCount());
        dto.setPrizeLevel(r.getPrizeLevel());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setVerifiedAt(r.getVerifiedAt());
        return dto;
    }

    /**
     * 将指定预测记录标记为当次最终预测（生成时选中的 Top N 注）
     */
    @PostMapping("/mark-final")
    public Result<Void> markFinal(@RequestBody List<Long> recordIds) {
        predictionService.markAsFinal(recordIds);
        return Result.success();
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
     * 获取下一预测期号（最新预测期号+1；无预测时为最新开奖期号+1）
     */
    @GetMapping("/next-issue")
    public Result<String> getNextPredictionIssue() {
        return Result.success(predictionService.getNextPredictionIssue());
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

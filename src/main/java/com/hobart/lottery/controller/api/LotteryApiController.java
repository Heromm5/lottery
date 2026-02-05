package com.hobart.lottery.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hobart.lottery.common.result.Result;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 彩票数据 API 控制器
 */
@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryApiController {

    private final LotteryService lotteryService;

    /**
     * 获取最新开奖
     */
    @GetMapping("/latest")
    public Result<LotteryResult> getLatest() {
        return Result.success(lotteryService.getLatestResult());
    }

    /**
     * 获取近期开奖
     */
    @GetMapping("/recent")
    public Result<List<LotteryResult>> getRecent(@RequestParam(defaultValue = "10") int size) {
        return Result.success(lotteryService.getRecentResults(size));
    }

    /**
     * 分页查询，按期号倒序
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = lotteryService.getPageOrderByIssueDesc(page, size);
        return Result.success(result);
    }

    /**
     * 根据期号查询
     */
    @GetMapping("/issue/{issue}")
    public Result<LotteryResult> getByIssue(@PathVariable String issue) {
        return Result.success(lotteryService.getByIssue(issue));
    }

    /**
     * 添加开奖结果
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody LotteryResult lotteryResult) {
        lotteryService.saveWithCalculation(lotteryResult);
        return Result.success(lotteryResult.getId());
    }

    /**
     * 删除开奖结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        lotteryService.removeById(id);
        return Result.success();
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", lotteryService.count());
        stats.put("nextIssue", lotteryService.generateNextIssue());
        stats.put("latestResult", lotteryService.getLatestResult());
        return Result.success(stats);
    }
}

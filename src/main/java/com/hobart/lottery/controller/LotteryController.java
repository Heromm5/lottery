package com.hobart.lottery.controller;

import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 开奖数据管理控制器
 */
@Controller
@RequestMapping("/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    /**
     * 开奖数据列表页面
     */
    @GetMapping("/list")
    public String list(Model model, @RequestParam(defaultValue = "50") Integer limit) {
        model.addAttribute("results", lotteryService.getRecentResults(limit));
        model.addAttribute("limit", limit);
        return "lottery/list";
    }

    /**
     * 新增开奖结果页面
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        // 生成下一期期号
        model.addAttribute("nextIssue", lotteryService.generateNextIssue());
        model.addAttribute("today", LocalDate.now());
        return "lottery/add";
    }

    /**
     * 保存开奖结果
     */
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> save(@RequestBody LotteryResult lotteryResult) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证数据
            if (lotteryResult.getIssue() == null || lotteryResult.getIssue().isEmpty()) {
                result.put("success", false);
                result.put("message", "期号不能为空");
                return result;
            }
            
            // 检查期号是否已存在
            if (lotteryService.getByIssue(lotteryResult.getIssue()) != null) {
                result.put("success", false);
                result.put("message", "期号 " + lotteryResult.getIssue() + " 已存在");
                return result;
            }
            
            // 验证号码范围
            int[] frontBalls = {lotteryResult.getFrontBall1(), lotteryResult.getFrontBall2(), 
                               lotteryResult.getFrontBall3(), lotteryResult.getFrontBall4(), 
                               lotteryResult.getFrontBall5()};
            int[] backBalls = {lotteryResult.getBackBall1(), lotteryResult.getBackBall2()};
            
            // 检查前区号码
            Set<Integer> frontSet = new HashSet<>();
            for (int ball : frontBalls) {
                if (ball < 1 || ball > 35) {
                    result.put("success", false);
                    result.put("message", "前区号码必须在1-35之间");
                    return result;
                }
                if (!frontSet.add(ball)) {
                    result.put("success", false);
                    result.put("message", "前区号码不能重复");
                    return result;
                }
            }
            
            // 检查后区号码
            Set<Integer> backSet = new HashSet<>();
            for (int ball : backBalls) {
                if (ball < 1 || ball > 12) {
                    result.put("success", false);
                    result.put("message", "后区号码必须在1-12之间");
                    return result;
                }
                if (!backSet.add(ball)) {
                    result.put("success", false);
                    result.put("message", "后区号码不能重复");
                    return result;
                }
            }
            
            // 计算统计字段并保存
            lotteryService.saveWithCalculation(lotteryResult);
            
            result.put("success", true);
            result.put("message", "开奖结果保存成功");
            result.put("data", lotteryResult);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除开奖结果
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            lotteryService.removeById(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据期号获取开奖结果
     */
    @GetMapping("/api/byIssue")
    @ResponseBody
    public Map<String, Object> getByIssue(@RequestParam String issue) {
        Map<String, Object> result = new HashMap<>();
        LotteryResult lr = lotteryService.getByIssue(issue);
        
        if (lr != null) {
            result.put("exists", true);
            result.put("data", lr);
        } else {
            result.put("exists", false);
        }
        
        return result;
    }
}

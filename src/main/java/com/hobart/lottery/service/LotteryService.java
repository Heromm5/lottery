package com.hobart.lottery.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.mapper.LotteryResultMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大乐透数据服务
 */
@Service
public class LotteryService extends ServiceImpl<LotteryResultMapper, LotteryResult> {

    /**
     * 获取最近N期开奖结果 - 缓存 2 分钟
     */
    @Cacheable(value = "recentResults", key = "#limit")
    public List<LotteryResult> getRecentResults(int limit) {
        return baseMapper.selectRecentResults(limit);
    }

    /**
     * 获取最新一期开奖结果 - 缓存 2 分钟
     */
    @Cacheable(value = "latestResult", key = "'latest'")
    public LotteryResult getLatestResult() {
        return baseMapper.selectLatestResult();
    }

    /**
     * 根据期号查询 - 缓存 5 分钟
     */
    @Cacheable(value = "resultByIssue", key = "#issue")
    public LotteryResult getByIssue(String issue) {
        return baseMapper.selectByIssue(issue);
    }

    /**
     * 获取所有开奖结果
     */
    public List<LotteryResult> getAllResults() {
        return list();
    }

    /**
     * 分页查询，按期号倒序
     */
    public Map<String, Object> getPageOrderByIssueDesc(int page, int size) {
        int offset = (page - 1) * size;
        List<LotteryResult> records = baseMapper.selectPageOrderByIssueDesc(offset, size);
        long total = count();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", (total + size - 1) / size);
        return result;
    }

    /**
     * 生成下一期期号
     */
    public String generateNextIssue() {
        LotteryResult latest = getLatestResult();
        if (latest == null) {
            return "26001";
        }
        String currentIssue = latest.getIssue();
        int issueNum = Integer.parseInt(currentIssue);
        return String.valueOf(issueNum + 1);
    }

    /**
     * 保存开奖结果并自动计算统计字段 - 保存后清除缓存
     */
    @CacheEvict(value = {"recentResults", "latestResult"}, allEntries = true)
    public void saveWithCalculation(LotteryResult result) {
        int[] front = {result.getFrontBall1(), result.getFrontBall2(), 
                       result.getFrontBall3(), result.getFrontBall4(), 
                       result.getFrontBall5()};
        int[] back = {result.getBackBall1(), result.getBackBall2()};
        
        // 排序号码
        java.util.Arrays.sort(front);
        java.util.Arrays.sort(back);
        
        // 更新排序后的号码
        result.setFrontBall1(front[0]);
        result.setFrontBall2(front[1]);
        result.setFrontBall3(front[2]);
        result.setFrontBall4(front[3]);
        result.setFrontBall5(front[4]);
        result.setBackBall1(back[0]);
        result.setBackBall2(back[1]);
        
        // 生成号码字符串
        result.setFrontBalls(front[0] + "," + front[1] + "," + front[2] + "," + front[3] + "," + front[4]);
        result.setBackBalls(back[0] + "," + back[1]);
        
        // 计算和值
        result.setFrontSum(front[0] + front[1] + front[2] + front[3] + front[4]);
        result.setBackSum(back[0] + back[1]);
        
        // 计算前区奇数个数
        int oddCountFront = 0;
        for (int num : front) {
            if (num % 2 == 1) oddCountFront++;
        }
        result.setOddCountFront(oddCountFront);
        
        // 计算后区奇数个数
        int oddCountBack = 0;
        for (int num : back) {
            if (num % 2 == 1) oddCountBack++;
        }
        result.setOddCountBack(oddCountBack);
        
        // 计算前区连号数
        int consecutiveFront = 0;
        for (int i = 0; i < front.length - 1; i++) {
            if (front[i + 1] - front[i] == 1) {
                consecutiveFront++;
            }
        }
        result.setConsecutiveCountFront(consecutiveFront);
        
        // 计算后区连号数
        int consecutiveBack = (back[1] - back[0] == 1) ? 1 : 0;
        result.setConsecutiveCountBack(consecutiveBack);
        
        // 计算AC值（号码差值的不同值个数-号码个数+1）
        java.util.Set<Integer> diffs = new java.util.HashSet<>();
        for (int i = 0; i < front.length; i++) {
            for (int j = i + 1; j < front.length; j++) {
                diffs.add(front[j] - front[i]);
            }
        }
        result.setAcValue(diffs.size() - front.length + 1);
        
        // 保存
        save(result);
    }
}

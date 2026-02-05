package com.hobart.lottery.service.analysis;

import com.hobart.lottery.config.LotteryConfig;
import com.hobart.lottery.domain.model.AssociationRule;
import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.LotteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 关联规则分析服务
 * 挖掘号码之间的共现规律
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssociationAnalyzer {
    
    private final LotteryService lotteryService;
    private final LotteryConfig config;
    
    /**
     * 挖掘指定区域的号码关联规则
     * 
     * @param zone 号码区域
     * @return 关联规则列表（按提升度降序）
     */
    public List<AssociationRule> mineAssociations(NumberZone zone) {
        int period = config.getAnalysis().getAssociationPeriod();
        double minSupport = config.getAnalysis().getMinSupport();
        double minConfidence = config.getAnalysis().getMinConfidence();
        
        return mineAssociations(zone, period, minSupport, minConfidence);
    }
    
    /**
     * 挖掘号码关联规则（自定义参数）
     */
    public List<AssociationRule> mineAssociations(NumberZone zone, int recentCount, 
                                                   double minSupport, double minConfidence) {
        List<LotteryResult> results = lotteryService.getRecentResults(recentCount);
        
        // 1. 统计每个号码的出现次数
        Map<Integer, Integer> singleCount = new HashMap<>();
        // 2. 统计每对号码的共现次数
        Map<String, Integer> pairCount = new HashMap<>();
        
        for (LotteryResult result : results) {
            int[] balls = zone.getBalls(result);
            
            // 单号码计数
            for (int ball : balls) {
                singleCount.merge(ball, 1, Integer::sum);
            }
            
            // 号码对计数（组合）
            for (int i = 0; i < balls.length; i++) {
                for (int j = i + 1; j < balls.length; j++) {
                    // 确保 key 的顺序一致
                    int min = Math.min(balls[i], balls[j]);
                    int max = Math.max(balls[i], balls[j]);
                    String key = min + "-" + max;
                    pairCount.merge(key, 1, Integer::sum);
                }
            }
        }
        
        // 3. 计算关联规则指标
        List<AssociationRule> rules = new ArrayList<>();
        int total = results.size();
        
        for (Map.Entry<String, Integer> entry : pairCount.entrySet()) {
            String[] nums = entry.getKey().split("-");
            int num1 = Integer.parseInt(nums[0]);
            int num2 = Integer.parseInt(nums[1]);
            int coCount = entry.getValue();
            
            // 计算支持度
            double support = (double) coCount / total;
            if (support < minSupport) continue;
            
            // num1 -> num2 的关联规则
            int count1 = singleCount.getOrDefault(num1, 0);
            if (count1 > 0) {
                double confidence = (double) coCount / count1;
                if (confidence >= minConfidence) {
                    double expectedConfidence = (double) singleCount.getOrDefault(num2, 0) / total;
                    double lift = expectedConfidence > 0 ? confidence / expectedConfidence : 0;
                    
                    if (lift > 1) { // 只保留正相关
                        Set<Integer> set1 = new HashSet<>();
                        set1.add(num1);
                        Set<Integer> set2 = new HashSet<>();
                        set2.add(num2);
                        rules.add(new AssociationRule(
                            set1, set2,
                            support, confidence, lift, zone.getCode().toUpperCase()
                        ));
                    }
                }
            }
            
            // num2 -> num1 的关联规则（反向）
            int count2 = singleCount.getOrDefault(num2, 0);
            if (count2 > 0) {
                double confidence = (double) coCount / count2;
                if (confidence >= minConfidence) {
                    double expectedConfidence = (double) singleCount.getOrDefault(num1, 0) / total;
                    double lift = expectedConfidence > 0 ? confidence / expectedConfidence : 0;
                    
                    if (lift > 1) {
                        Set<Integer> set3 = new HashSet<>();
                        set3.add(num2);
                        Set<Integer> set4 = new HashSet<>();
                        set4.add(num1);
                        rules.add(new AssociationRule(
                            set3, set4,
                            support, confidence, lift, zone.getCode().toUpperCase()
                        ));
                    }
                }
            }
        }
        
        // 按提升度降序排序
        rules.sort(Comparator.comparing(AssociationRule::getLift).reversed());
        
        log.info("区域 {} 挖掘到 {} 条关联规则", zone.getDisplayName(), rules.size());
        return rules;
    }
    
    /**
     * 获取与指定号码关联度最高的号码
     * 
     * @param number 指定号码
     * @param zone 号码区域
     * @param topN 返回数量
     * @return 关联号码列表
     */
    public List<Integer> getRelatedNumbers(int number, NumberZone zone, int topN) {
        List<AssociationRule> rules = mineAssociations(zone);
        
        return rules.stream()
            .filter(r -> r.getAntecedent().contains(number))
            .sorted(Comparator.comparing(AssociationRule::getLift).reversed())
            .limit(topN)
            .flatMap(r -> r.getConsequent().stream())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 获取关联网络数据（用于前端可视化）
     * 
     * @param zone 号码区域
     * @param topN 返回规则数量
     * @return 包含 nodes 和 links 的 Map
     */
    public Map<String, Object> getAssociationNetwork(NumberZone zone, int topN) {
        List<AssociationRule> rules = mineAssociations(zone);
        
        // 限制规则数量
        List<AssociationRule> topRules = rules.stream()
            .limit(topN)
            .collect(Collectors.toList());
        
        // 收集所有涉及的号码
        Set<Integer> involvedNumbers = new HashSet<>();
        for (AssociationRule rule : topRules) {
            involvedNumbers.addAll(rule.getAntecedent());
            involvedNumbers.addAll(rule.getConsequent());
        }
        
        // 计算每个号码的出现频率（用于节点大小）
        int period = config.getAnalysis().getAssociationPeriod();
        List<LotteryResult> results = lotteryService.getRecentResults(period);
        Map<Integer, Integer> frequency = new HashMap<>();
        
        for (LotteryResult result : results) {
            int[] balls = zone.getBalls(result);
            for (int ball : balls) {
                if (involvedNumbers.contains(ball)) {
                    frequency.merge(ball, 1, Integer::sum);
                }
            }
        }
        
        // 构建节点
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Integer num : involvedNumbers) {
            Map<String, Object> node = new HashMap<>();
            node.put("number", num);
            node.put("name", String.format("%02d", num));
            node.put("frequency", frequency.getOrDefault(num, 0));
            nodes.add(node);
        }
        
        // 构建边
        List<Map<String, Object>> links = new ArrayList<>();
        for (AssociationRule rule : topRules) {
            for (Integer source : rule.getAntecedent()) {
                for (Integer target : rule.getConsequent()) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", String.format("%02d", source));
                    link.put("target", String.format("%02d", target));
                    link.put("lift", rule.getLift());
                    link.put("confidence", rule.getConfidence());
                    link.put("support", rule.getSupport());
                    links.add(link);
                }
            }
        }
        
        Map<String, Object> network = new HashMap<>();
        network.put("nodes", nodes);
        network.put("links", links);
        network.put("rules", topRules.stream()
            .map(AssociationRule::getDescription)
            .collect(Collectors.toList()));
        
        return network;
    }
    
    /**
     * 挖掘连续期关联（上期号码与本期号码的关联）
     * 
     * @param zone 号码区域
     * @return 连续期关联规则
     */
    public List<AssociationRule> mineSequentialAssociations(NumberZone zone) {
        int period = config.getAnalysis().getAssociationPeriod();
        double minSupport = config.getAnalysis().getMinSupport();
        double minConfidence = config.getAnalysis().getMinConfidence();
        
        List<LotteryResult> results = lotteryService.getRecentResults(period);
        
        // 统计上期号码 -> 本期号码的共现
        Map<String, Integer> sequentialCount = new HashMap<>();
        Map<Integer, Integer> prevCount = new HashMap<>();
        
        for (int i = 0; i < results.size() - 1; i++) {
            int[] prevBalls = zone.getBalls(results.get(i + 1)); // 上期（索引大的是更早的）
            int[] currBalls = zone.getBalls(results.get(i));     // 本期
            
            for (int prev : prevBalls) {
                prevCount.merge(prev, 1, Integer::sum);
                for (int curr : currBalls) {
                    String key = prev + "->" + curr;
                    sequentialCount.merge(key, 1, Integer::sum);
                }
            }
        }
        
        // 计算关联规则
        List<AssociationRule> rules = new ArrayList<>();
        int total = results.size() - 1;
        
        for (Map.Entry<String, Integer> entry : sequentialCount.entrySet()) {
            String[] parts = entry.getKey().split("->");
            int prev = Integer.parseInt(parts[0]);
            int curr = Integer.parseInt(parts[1]);
            int coCount = entry.getValue();
            
            double support = (double) coCount / total;
            if (support < minSupport) continue;
            
            int pCount = prevCount.getOrDefault(prev, 0);
            if (pCount == 0) continue;
            
            double confidence = (double) coCount / pCount;
            if (confidence < minConfidence) continue;
            
            // 期望置信度（本期号码的整体出现概率）
            double expectedConfidence = (double) zone.getCount() / zone.getNumberCount();
            double lift = confidence / expectedConfidence;
            
            if (lift > 1) {
                Set<Integer> set5 = new HashSet<>();
                set5.add(prev);
                Set<Integer> set6 = new HashSet<>();
                set6.add(curr);
                rules.add(new AssociationRule(
                    set5, set6,
                    support, confidence, lift, zone.getCode().toUpperCase() + "_SEQ"
                ));
            }
        }
        
        rules.sort(Comparator.comparing(AssociationRule::getLift).reversed());
        
        log.info("区域 {} 挖掘到 {} 条连续期关联规则", zone.getDisplayName(), rules.size());
        return rules;
    }
}

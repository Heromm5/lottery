package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 冷热均衡预测器
 * 前区：热号3个 + 温号1个 + 冷号1个
 * 后区：热号1个 + 冷号1个
 */
@Component
public class BalancedPredictor extends BasePredictor {

    public BalancedPredictor(AnalysisFacade analysisFacade) {
        super(analysisFacade);
    }

    @Override
    public String getMethodName() {
        return "冷热均衡";
    }

    @Override
    public String getMethodCode() {
        return "BALANCED";
    }

    @Override
    public int[][] predict() {
        // 获取热号和冷号
        List<Integer> hotFront = analysisFacade.getHotFrontNumbers(12);
        List<Integer> coldFront = analysisFacade.getColdFrontNumbers(12);
        List<Integer> hotBack = analysisFacade.getHotBackNumbers(4);
        List<Integer> coldBack = analysisFacade.getColdBackNumbers(4);
        
        // 温号 = 全部号码 - 热号 - 冷号
        Set<Integer> hotSet = new HashSet<>(hotFront);
        Set<Integer> coldSet = new HashSet<>(coldFront);
        List<Integer> warmFront = new ArrayList<>();
        for (int i = NumberZone.FRONT.getMin(); i <= NumberZone.FRONT.getMax(); i++) {
            if (!hotSet.contains(i) && !coldSet.contains(i)) {
                warmFront.add(i);
            }
        }
        
        Set<Integer> selected = new TreeSet<>();
        
        // 选3个热号
        Collections.shuffle(hotFront);
        for (int num : hotFront) {
            if (selected.size() >= 3) break;
            selected.add(num);
        }
        
        // 选1个温号
        Collections.shuffle(warmFront);
        for (int num : warmFront) {
            if (selected.size() >= 4) break;
            if (!selected.contains(num)) {
                selected.add(num);
            }
        }
        
        // 选1个冷号
        Collections.shuffle(coldFront);
        for (int num : coldFront) {
            if (selected.size() >= 5) break;
            if (!selected.contains(num)) {
                selected.add(num);
            }
        }
        
        // 补充不够的
        while (selected.size() < 5) {
            int num = random.nextInt(35) + 1;
            selected.add(num);
        }
        
        int[] front = selected.stream().mapToInt(Integer::intValue).toArray();
        
        // 后区：热号1个 + 冷号1个
        Set<Integer> backSelected = new TreeSet<>();
        Collections.shuffle(hotBack);
        if (!hotBack.isEmpty()) {
            backSelected.add(hotBack.get(0));
        }
        Collections.shuffle(coldBack);
        for (int num : coldBack) {
            if (backSelected.size() >= 2) break;
            if (!backSelected.contains(num)) {
                backSelected.add(num);
            }
        }
        while (backSelected.size() < 2) {
            int num = random.nextInt(12) + 1;
            backSelected.add(num);
        }
        
        int[] back = backSelected.stream().mapToInt(Integer::intValue).toArray();
        
        return new int[][]{front, back};
    }
}

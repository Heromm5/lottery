package com.hobart.lottery.predictor;

import com.hobart.lottery.service.analysis.AnalysisFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("BasePredictor - 号码生成基本约束")
class BasePredictorTest {

    private final AnalysisFacade mockFacade = mock(AnalysisFacade.class);

    private final BasePredictor predictor = new BasePredictor(mockFacade) {
        @Override
        public String getMethodName() { return "测试"; }
        @Override
        public String getMethodCode() { return "TEST"; }
        @Override
        public int[][] predict() { return generateRandom(); }
    };

    @Test
    @DisplayName("生成的前区号码应有5个，范围1-35，无重复")
    void frontBallsShouldBeValid() {
        int[][] result = predictor.predict();
        int[] front = result[0];

        assertEquals(5, front.length, "前区应有5个号码");

        Set<Integer> unique = new HashSet<>();
        for (int n : front) {
            assertTrue(n >= 1 && n <= 35, "前区号码应在1-35之间: " + n);
            unique.add(n);
        }
        assertEquals(5, unique.size(), "前区号码不应重复");
    }

    @Test
    @DisplayName("生成的后区号码应有2个，范围1-12，无重复")
    void backBallsShouldBeValid() {
        int[][] result = predictor.predict();
        int[] back = result[1];

        assertEquals(2, back.length, "后区应有2个号码");

        Set<Integer> unique = new HashSet<>();
        for (int n : back) {
            assertTrue(n >= 1 && n <= 12, "后区号码应在1-12之间: " + n);
            unique.add(n);
        }
        assertEquals(2, unique.size(), "后区号码不应重复");
    }

    @Test
    @DisplayName("前区号码应升序排列")
    void frontBallsShouldBeSorted() {
        int[][] result = predictor.predict();
        int[] front = result[0];
        int[] sorted = Arrays.copyOf(front, front.length);
        Arrays.sort(sorted);
        assertArrayEquals(sorted, front, "前区号码应升序排列");
    }

    @Test
    @DisplayName("批量生成应返回指定数量且不重复")
    void predictMultipleShouldReturnDistinctResults() {
        List<int[][]> results = predictor.predictMultiple(5);
        assertEquals(5, results.size());

        Set<String> keys = new HashSet<>();
        for (int[][] r : results) {
            String key = Arrays.toString(r[0]) + "-" + Arrays.toString(r[1]);
            keys.add(key);
        }
        assertEquals(5, keys.size(), "批量生成的结果应不重复");
    }
}

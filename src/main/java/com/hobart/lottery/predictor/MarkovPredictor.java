package com.hobart.lottery.predictor;

import com.hobart.lottery.domain.model.NumberZone;
import com.hobart.lottery.entity.LotteryResult;
import com.hobart.lottery.service.AnalysisService;
import com.hobart.lottery.service.LotteryService;

import java.util.*;
public class MarkovPredictor extends BasePredictor {

    private final LotteryService lotteryService;
    
    // 转移概率矩阵
    private double[][] frontTransitionMatrix;
    private double[][] backTransitionMatrix;
    
    // 稳态分布
    private double[] frontStationaryDist;
    private double[] backStationaryDist;
    
    // 历史数据量
    private static final int HISTORY_PERIODS = 500;

    public MarkovPredictor(AnalysisService analysisService, LotteryService lotteryService) {
        super(analysisService);
        this.lotteryService = lotteryService;
    }

    @Override
    public String getMethodName() {
        return "马尔可夫";
    }

    @Override
    public String getMethodCode() {
        return "MARKOV";
    }

    @Override
    public int[][] predict() {
        List<LotteryResult> results = lotteryService.getRecentResults(HISTORY_PERIODS);
        
        if (results.size() < 10) {
            // 历史数据不足，使用默认预测
            return new int[][]{{1, 2, 3, 4, 5}, {1, 2}};
        }
        
        // 构建转移概率矩阵
        buildTransitionMatrices(results);
        
        // 计算稳态分布
        frontStationaryDist = calculateStationaryDistribution(frontTransitionMatrix, 35);
        backStationaryDist = calculateStationaryDistribution(backTransitionMatrix, 12);
        
        // 基于稳态分布采样
        int[] front = sampleByProbability(frontStationaryDist, 5, 35);
        int[] back = sampleByProbability(backStationaryDist, 2, 12);
        
        return new int[][]{front, back};
    }

    /**
     * 构建转移概率矩阵
     * P[from][to] = 从from转移到to的概率
     */
    private void buildTransitionMatrices(List<LotteryResult> results) {
        frontTransitionMatrix = new double[36][36];  // 1-35
        backTransitionMatrix = new double[13][13];  // 1-12
        
        // 统计转移次数
        for (int i = 0; i < results.size() - 1; i++) {
            int[] currentFront = results.get(i).getFrontBallArray();
            int[] nextFront = results.get(i + 1).getFrontBallArray();
            int[] currentBack = results.get(i).getBackBallArray();
            int[] nextBack = results.get(i + 1).getBackBallArray();
            
            // 前区转移
            for (int from : currentFront) {
                for (int to : nextFront) {
                    frontTransitionMatrix[from][to]++;
                }
            }
            
            // 后区转移
            for (int from : currentBack) {
                for (int to : nextBack) {
                    backTransitionMatrix[from][to]++;
                }
            }
        }
        
        // 归一化为概率
        normalizeMatrix(frontTransitionMatrix, 35);
        normalizeMatrix(backTransitionMatrix, 12);
    }

    /**
     * 归一化矩阵为概率
     */
    private void normalizeMatrix(double[][] matrix, int size) {
        for (int i = 1; i <= size; i++) {
            double rowSum = 0;
            for (int j = 1; j <= size; j++) {
                rowSum += matrix[i][j];
            }
            
            if (rowSum == 0) {
                // 如果该号码从未作为起始出现，使用均匀分布
                for (int j = 1; j <= size; j++) {
                    matrix[i][j] = 1.0 / size;
                }
            } else {
                for (int j = 1; j <= size; j++) {
                    matrix[i][j] /= rowSum;
                }
            }
        }
    }

    /**
     * 计算稳态分布（长期概率）
     * 使用幂迭代法求解：π = π × P
     */
    private double[] calculateStationaryDistribution(double[][] matrix, int size) {
        double[] pi = new double[size + 1];
        
        // 初始化为均匀分布
        for (int i = 1; i <= size; i++) {
            pi[i] = 1.0 / size;
        }
        
        // 幂迭代
        double[] nextPi = new double[size + 1];
        for (int iter = 0; iter < 1000; iter++) {
            // π_new = π × P
            Arrays.fill(nextPi, 0);
            for (int i = 1; i <= size; i++) {
                for (int j = 1; j <= size; j++) {
                    nextPi[j] += pi[i] * matrix[i][j];
                }
            }
            
            // 归一化
            double sum = 0;
            for (int i = 1; i <= size; i++) {
                sum += nextPi[i];
            }
            if (sum > 0) {
                for (int i = 1; i <= size; i++) {
                    pi[i] = nextPi[i] / sum;
                }
            }
            
            // 检查收敛
            double diff = 0;
            for (int i = 1; i <= size; i++) {
                diff += Math.abs(pi[i] - nextPi[i] / sum);
            }
            if (diff < 1e-10) {
                break;
            }
        }
        
        return pi;
    }

    /**
     * 基于概率采样（保证不重复）
     */
    private int[] sampleByProbability(double[] probs, int need, int size) {
        // 按概率降序排序
        List<Integer> candidates = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            candidates.add(i);
        }
        
        candidates.sort((a, b) -> Double.compare(probs[b], probs[a]));
        
        // 选择概率最高的need个
        Set<Integer> selected = new HashSet<>();
        int[] result = new int[need];
        int idx = 0;
        
        for (int num : candidates) {
            if (selected.size() >= need) break;
            if (!selected.contains(num)) {
                selected.add(num);
                result[idx++] = num;
            }
        }
        
        Arrays.sort(result);
        return result;
    }

    /**
     * 获取转移概率矩阵（用于分析）
     */
    public double[][] getFrontTransitionMatrix() {
        return frontTransitionMatrix;
    }

    /**
     * 获取后区转移概率矩阵（用于分析）
     */
    public double[][] getBackTransitionMatrix() {
        return backTransitionMatrix;
    }

    /**
     * 获取两个号码的共现概率
     */
    public double getCooccurrenceProbability(int num1, int num2, NumberZone zone) {
        double[][] matrix = zone == NumberZone.FRONT ? frontTransitionMatrix : backTransitionMatrix;
        
        // P(一起出现) = P(num1出现) × P(num2|num1出现)
        double p1 = zone == NumberZone.FRONT ? frontStationaryDist[num1] : backStationaryDist[num1];
        double p2_given_p1 = matrix[num1][num2];
        
        return p1 * p2_given_p1;
    }
}

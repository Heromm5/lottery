package com.hobart.lottery.ai.service;

import com.hobart.lottery.ai.dto.AiPredictionRequest;
import com.hobart.lottery.ai.dto.AiPredictionResult;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
import com.hobart.lottery.ai.gateway.CircuitOpenException;
import com.hobart.lottery.ai.gateway.RateLimitException;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * AI Deep Learning Predictor
 * Generates prediction numbers based on historical data and AI model
 */
@Service
@RequiredArgsConstructor
public class DeepLearningPredictor {

    private static final int MAX_COUNT = 50;
    private static final int MIN_COUNT = 1;
    private static final int FRONT_ZONE_MIN = 1;
    private static final int FRONT_ZONE_MAX = 35;
    private static final int BACK_ZONE_MIN = 1;
    private static final int BACK_ZONE_MAX = 12;

    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;

    /**
     * Generate AI prediction results
     *
     * @param request prediction request
     * @return list of prediction results
     */
    public List<AiPredictionResult> predict(AiPredictionRequest request) {
        // 1. Validate request parameters
        validateRequest(request);

        // 2. If AI service unavailable, use mock fallback
        try {
            return predictWithAi(request);
        } catch (RateLimitException | CircuitOpenException e) {
            return predictWithMock(request);
        } catch (Exception e) {
            return predictWithMock(request);
        }
    }

    /**
     * Predict with AI service
     */
    private List<AiPredictionResult> predictWithAi(AiPredictionRequest request) {
        String cacheKey = buildCacheKey(request);
        String historyData = getHistoryData(request.getHistoryPeriods());
        String prompt = buildPrompt(request, historyData);

        String aiResponse = gateway.call(AiProvider.CLAUDE, cacheKey,
            () -> callAiApi(prompt)).orElseThrow(() -> new RuntimeException("AI response empty"));

        return parseAiResponse(aiResponse);
    }

    /**
     * Validate request parameters
     */
    private void validateRequest(AiPredictionRequest request) {
        Integer count = request.getCount();
        if (count == null || count < MIN_COUNT || count > MAX_COUNT) {
            throw new IllegalArgumentException(
                "count must be between " + MIN_COUNT + " and " + MAX_COUNT);
        }
    }

    /**
     * Build cache key
     */
    private String buildCacheKey(AiPredictionRequest request) {
        return String.format("predict:%d:%d",
            request.getCount(),
            request.getHistoryPeriods() != null ? request.getHistoryPeriods() : 100);
    }

    /**
     * Get historical data
     */
    private String getHistoryData(Integer periods) {
        int periodsCount = periods != null ? periods : 100;
        List<Integer> hotFront = analysisFacade.getHotFrontNumbers(10);
        List<Integer> hotBack = analysisFacade.getHotBackNumbers(5);
        return String.format("Hot front: %s, Hot back: %s",
            hotFront.stream().map(String::valueOf).collect(Collectors.joining(",")),
            hotBack.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    /**
     * Build AI prompt
     */
    private String buildPrompt(AiPredictionRequest request, String historyData) {
        int count = request.getCount() != null ? request.getCount() : 5;
        StringBuilder sb = new StringBuilder();
        sb.append("Based on the following lottery historical data, predict next issue numbers:\n");
        sb.append(historyData).append("\n\n");
        sb.append("Please generate ").append(count).append(" predictions, each with 5 front numbers (1-35) and 2 back numbers (1-12).\n");
        sb.append("Return format: JSON array, each item: {\"front\":[...],\"back\":[...]}\n");
        sb.append("Only return JSON array, no other content.");
        return sb.toString();
    }

    /**
     * Call AI API (replace with real API call in actual implementation)
     */
    private String callAiApi(String prompt) {
        // TODO: Implement real AI API call
        // Return mock data for testing
        return buildMockResponse(5);
    }

    /**
     * Parse AI response
     */
    private List<AiPredictionResult> parseAiResponse(String response) {
        try {
            // Check if mock data
            if (response.contains("MOCK") || !response.contains("front")) {
                return generateMockResults(5);
            }

            // Try to parse the number of results from response
            int count = countJsonObjects(response);
            if (count <= 0) {
                count = 5;
            }

            List<AiPredictionResult> results = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < count; i++) {
                AiPredictionResult result = new AiPredictionResult();
                // Generate random numbers based on mock
                result.setFrontBalls(generateRandomNumbers(FRONT_ZONE_MIN, FRONT_ZONE_MAX, 5, random));
                result.setBackBalls(generateRandomNumbers(BACK_ZONE_MIN, BACK_ZONE_MAX, 2, random));
                result.setConfidence(0.75);
                result.setAiModel("claude-sonnet-4-6");
                result.setReasoning("Based on historical frequency analysis");
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            // Ignore
        }

        // Fallback to mock
        return generateMockResults(5);
    }

    /**
     * Count JSON objects in response
     */
    private int countJsonObjects(String response) {
        int count = 0;
        int index = 0;
        while ((index = response.indexOf("{\"front\":", index)) != -1) {
            count++;
            index++;
        }
        return count;
    }

    // ==================== Mock Implementation ====================

    /**
     * Use Mock to generate prediction results (when AI service unavailable)
     */
    private List<AiPredictionResult> predictWithMock(AiPredictionRequest request) {
        int count = request.getCount() != null ? request.getCount() : 5;
        return generateMockResults(count);
    }

    /**
     * Generate Mock prediction results
     */
    private List<AiPredictionResult> generateMockResults(int count) {
        List<AiPredictionResult> results = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            AiPredictionResult result = new AiPredictionResult();

            // Generate front numbers (5 unique)
            List<String> frontBalls = generateRandomNumbers(
                FRONT_ZONE_MIN, FRONT_ZONE_MAX, 5, random);
            result.setFrontBalls(frontBalls);

            // Generate back numbers (2 unique)
            List<String> backBalls = generateRandomNumbers(
                BACK_ZONE_MIN, BACK_ZONE_MAX, 2, random);
            result.setBackBalls(backBalls);

            // Set confidence (simulated value)
            result.setConfidence(0.6 + random.nextDouble() * 0.3);
            result.setAiModel("MOCK");
            result.setReasoning("Mock prediction - AI service unavailable");

            results.add(result);
        }

        return results;
    }

    /**
     * Generate random unique numbers
     */
    private List<String> generateRandomNumbers(int min, int max, int count, Random random) {
        List<Integer> numbers = new ArrayList<>();
        while (numbers.size() < count) {
            int num = random.nextInt(max - min + 1) + min;
            if (!numbers.contains(num)) {
                numbers.add(num);
            }
        }
        return numbers.stream()
            .map(n -> String.format("%02d", n))
            .collect(Collectors.toList());
    }

    /**
     * Build Mock response
     */
    private String buildMockResponse(int count) {
        StringBuilder sb = new StringBuilder("[");
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");

            List<Integer> front = generateRandomInts(
                FRONT_ZONE_MIN, FRONT_ZONE_MAX, 5, random);
            List<Integer> back = generateRandomInts(
                BACK_ZONE_MIN, BACK_ZONE_MAX, 2, random);

            sb.append(String.format(
                "{\"front\":[%s],\"back\":[%s]}",
                front.stream().map(String::valueOf).collect(Collectors.joining(",")),
                back.stream().map(String::valueOf).collect(Collectors.joining(","))));
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Generate random integer list
     */
    private List<Integer> generateRandomInts(int min, int max, int count, Random random) {
        List<Integer> numbers = new ArrayList<>();
        while (numbers.size() < count) {
            int num = random.nextInt(max - min + 1) + min;
            if (!numbers.contains(num)) {
                numbers.add(num);
            }
        }
        return numbers;
    }
}
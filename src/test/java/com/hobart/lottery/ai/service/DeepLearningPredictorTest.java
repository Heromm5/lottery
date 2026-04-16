package com.hobart.lottery.ai.service;

import com.hobart.lottery.ai.dto.AiPredictionRequest;
import com.hobart.lottery.ai.dto.AiPredictionResult;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * DeepLearningPredictor Test Class
 */
@ExtendWith(MockitoExtension.class)
class DeepLearningPredictorTest {

    @Mock
    private AIGateway gateway;

    @Mock
    private AnalysisFacade analysisFacade;

    private DeepLearningPredictor predictor;

    @BeforeEach
    void setUp() {
        predictor = new DeepLearningPredictor(gateway, analysisFacade);
    }

    @Test
    void shouldGeneratePredictions() {
        // Prepare mock data
        when(analysisFacade.getHotFrontNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        when(analysisFacade.getHotBackNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5));

        // Simulate AI gateway returning
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of("[{\"front\":[1,2,3,4,5],\"back\":[1,2]}]"));

        // Create request
        AiPredictionRequest request = new AiPredictionRequest();
        request.setCount(5);
        request.setHistoryPeriods(100);

        // Execute prediction
        List<AiPredictionResult> results = predictor.predict(request);

        // Verify results
        assertNotNull(results);
        assertEquals(5, results.size());

        // Verify each result
        for (AiPredictionResult result : results) {
            assertNotNull(result.getFrontBalls());
            assertNotNull(result.getBackBalls());
            assertEquals(5, result.getFrontBalls().size());
            assertEquals(2, result.getBackBalls().size());
        }
    }

    @Test
    void shouldRespectCountLimit() {
        // Test count > 50 should throw exception
        AiPredictionRequest request = new AiPredictionRequest();
        request.setCount(100);

        assertThrows(IllegalArgumentException.class, () -> predictor.predict(request));
    }

    @Test
    void shouldRespectMinCount() {
        // Test count < 1 should throw exception
        AiPredictionRequest request = new AiPredictionRequest();
        request.setCount(0);

        assertThrows(IllegalArgumentException.class, () -> predictor.predict(request));
    }

    @Test
    void shouldUseMockWhenAiUnavailable() {
        // Simulate AI service unavailable
        when(analysisFacade.getHotFrontNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        when(analysisFacade.getHotBackNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5));

        // Simulate AI gateway throwing exception
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        // Create request
        AiPredictionRequest request = new AiPredictionRequest();
        request.setCount(3);
        request.setHistoryPeriods(100);

        // Execute prediction - should fallback to mock
        List<AiPredictionResult> results = predictor.predict(request);

        // Verify results
        assertNotNull(results);
        assertEquals(3, results.size());

        // Verify it's mock data
        for (AiPredictionResult result : results) {
            assertEquals("MOCK", result.getAiModel());
        }
    }

    @Test
    void shouldGenerateCorrectNumberFormat() {
        // Prepare mock data
        when(analysisFacade.getHotFrontNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        when(analysisFacade.getHotBackNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5));

        // Simulate AI service unavailable
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        AiPredictionRequest request = new AiPredictionRequest();
        request.setCount(1);

        List<AiPredictionResult> results = predictor.predict(request);

        assertEquals(1, results.size());
        AiPredictionResult result = results.get(0);

        // Verify number format is two digits (e.g., 01, 02)
        String frontBall = result.getFrontBalls().get(0);
        assertTrue(frontBall.length() == 2, "Front ball should be two-digit format");
    }

    @Test
    void shouldHandleNullHistoryPeriods() {
        // Prepare mock data
        when(analysisFacade.getHotFrontNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        when(analysisFacade.getHotBackNumbers(anyInt()))
            .thenReturn(Arrays.asList(1, 2, 3, 4, 5));

        // Simulate AI service unavailable
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        // Do not set historyPeriods
        AiPredictionRequest request = new AiPredictionRequest();
        request.setCount(2);

        List<AiPredictionResult> results = predictor.predict(request);

        assertNotNull(results);
        assertEquals(2, results.size());
    }
}
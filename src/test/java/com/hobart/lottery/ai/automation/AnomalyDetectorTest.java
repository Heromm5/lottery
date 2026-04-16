package com.hobart.lottery.ai.automation;

import com.hobart.lottery.ai.dto.AnomalyAlert;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
import com.hobart.lottery.mapper.AnomalyAlertMapper;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AnomalyDetector Test Class
 */
@ExtendWith(MockitoExtension.class)
class AnomalyDetectorTest {

    @Mock
    private AIGateway gateway;

    @Mock
    private AnalysisFacade analysisFacade;

    @Mock
    private AnomalyAlertMapper alertMapper;

    private AnomalyDetector anomalyDetector;

    @BeforeEach
    void setUp() {
        anomalyDetector = new AnomalyDetector(gateway, analysisFacade, alertMapper);
    }

    @Test
    void shouldDetectAnomalies() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("=== 号码频率分析 (最近100期) ===\n前区频率 Top10: [01:15, 02:12, ...]");

        // Simulate AI gateway returning anomalies
        String mockAlerts = "["
            + "{\"type\":\"HIGH_FREQUENCY\",\"severity\":\"MEDIUM\","
            + "\"description\":\"检测到号码01出现频率异常高\","
            + "\"data\":{\"number\":1,\"count\":15,\"threshold\":10}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockAlerts));

        // Execute anomaly detection
        List<AnomalyAlert> alerts = anomalyDetector.detectAnomalies(100);

        // Verify results
        assertNotNull(alerts);
    }

    @Test
    void shouldReturnAlertsWhenAiUnavailable() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(50))
            .thenReturn("=== 分析数据 ===");

        // Simulate AI gateway throwing exception
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        // Execute anomaly detection - should fallback to mock
        List<AnomalyAlert> alerts = anomalyDetector.detectAnomalies(50);

        // Verify results - should return default patterns
        assertNotNull(alerts);
    }

    @Test
    void shouldParseAlertSeverities() {
        // Prepare mock data with different severity levels
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("test data");

        String mockAlerts = "["
            + "{\"type\":\"HIGH_FREQUENCY\",\"severity\":\"LOW\","
            + "\"description\":\"低严重程度告警\",\"data\":{}},"
            + "{\"type\":\"MISSING_DUE\",\"severity\":\"HIGH\","
            + "\"description\":\"高严重程度告警\",\"data\":{}},"
            + "{\"type\":\"CRITICAL_ERROR\",\"severity\":\"CRITICAL\","
            + "\"description\":\"严重告警\",\"data\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockAlerts));

        // Execute
        List<AnomalyAlert> alerts = anomalyDetector.detectAnomalies(100);

        // Verify severity levels
        assertTrue(alerts.stream().anyMatch(a -> "LOW".equals(a.getSeverity())));
        assertTrue(alerts.stream().anyMatch(a -> "HIGH".equals(a.getSeverity())));
        assertTrue(alerts.stream().anyMatch(a -> "CRITICAL".equals(a.getSeverity())));
    }

    @Test
    void shouldSetDetectedAt() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("test data");

        String mockAlerts = "["
            + "{\"type\":\"HIGH_FREQUENCY\",\"severity\":\"MEDIUM\","
            + "\"description\":\"test\",\"data\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockAlerts));

        // Execute
        List<AnomalyAlert> alerts = anomalyDetector.detectAnomalies(100);

        // Verify detectedAt is set
        assertTrue(alerts.stream().allMatch(a -> a.getDetectedAt() != null));
    }

    @Test
    void shouldSetAcknowledgedFalse() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("test data");

        String mockAlerts = "["
            + "{\"type\":\"HIGH_FREQUENCY\",\"severity\":\"MEDIUM\","
            + "\"description\":\"test\",\"data\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockAlerts));

        // Execute
        List<AnomalyAlert> alerts = anomalyDetector.detectAnomalies(100);

        // Verify acknowledged is false by default
        assertTrue(alerts.stream().allMatch(a -> Boolean.FALSE.equals(a.getAcknowledged())));
    }
}
package com.hobart.lottery.ai.automation;

import com.hobart.lottery.ai.dto.DiscoveredPattern;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
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
 * RuleDiscovery Test Class
 */
@ExtendWith(MockitoExtension.class)
class RuleDiscoveryTest {

    @Mock
    private AIGateway gateway;

    @Mock
    private AnalysisFacade analysisFacade;

    private RuleDiscovery ruleDiscovery;

    @BeforeEach
    void setUp() {
        ruleDiscovery = new RuleDiscovery(gateway, analysisFacade);
    }

    @Test
    void shouldDiscoverPatterns() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("=== 号码频率分析 ===\n前区频率 Top10: ...");

        // Simulate AI gateway returning patterns
        String mockPatterns = "["
            + "{\"type\":\"frequency\",\"description\":\"前区号码07出现频率较高\",\"confidence\":0.85,\"evidence\":{}},"
            + "{\"type\":\"missing\",\"description\":\"前区号码02遗漏较大\",\"confidence\":0.78,\"evidence\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockPatterns));

        // Execute pattern discovery
        List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(100);

        // Verify results
        assertNotNull(patterns);
        assertFalse(patterns.isEmpty());
    }

    @Test
    void shouldReturnPatternsWhenAiUnavailable() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(50))
            .thenReturn("=== 分析数据 ===");

        // Simulate AI gateway throwing exception
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        // Execute pattern discovery - should fallback to mock
        List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(50);

        // Verify results - should return default patterns
        assertNotNull(patterns);
        assertFalse(patterns.isEmpty());
    }

    @Test
    void shouldParsePatternTypes() {
        // Prepare mock data with different pattern types
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("test data");

        String mockPatterns = "["
            + "{\"type\":\"frequency\",\"description\":\"频率规律\",\"confidence\":0.9,\"evidence\":{}},"
            + "{\"type\":\"missing\",\"description\":\"遗漏规律\",\"confidence\":0.8,\"evidence\":{}},"
            + "{\"type\":\"trend\",\"description\":\"趋势规律\",\"confidence\":0.75,\"evidence\":{}},"
            + "{\"type\":\"association\",\"description\":\"关联规律\",\"confidence\":0.7,\"evidence\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockPatterns));

        // Execute
        List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(100);

        // Verify pattern types
        assertEquals(4, patterns.size());
        assertTrue(patterns.stream().anyMatch(p -> "frequency".equals(p.getPatternType())));
        assertTrue(patterns.stream().anyMatch(p -> "missing".equals(p.getPatternType())));
        assertTrue(patterns.stream().anyMatch(p -> "trend".equals(p.getPatternType())));
        assertTrue(patterns.stream().anyMatch(p -> "association".equals(p.getPatternType())));
    }

    @Test
    void shouldSetConfidenceInRange() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("test data");

        String mockPatterns = "["
            + "{\"type\":\"frequency\",\"description\":\"test\",\"confidence\":0.95,\"evidence\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockPatterns));

        // Execute
        List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(100);

        // Verify confidence is in valid range
        for (DiscoveredPattern pattern : patterns) {
            assertNotNull(pattern.getConfidence());
            assertTrue(pattern.getConfidence() >= 0.0 && pattern.getConfidence() <= 1.0,
                "Confidence should be between 0 and 1");
        }
    }

    @Test
    void shouldSetDiscoveredAt() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(100))
            .thenReturn("test data");

        String mockPatterns = "["
            + "{\"type\":\"frequency\",\"description\":\"test\",\"confidence\":0.8,\"evidence\":{}}"
            + "]";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockPatterns));

        // Execute
        List<DiscoveredPattern> patterns = ruleDiscovery.discoverPatterns(100);

        // Verify discoveredAt is set
        assertTrue(patterns.stream().allMatch(p -> p.getDiscoveredAt() != null));
    }
}
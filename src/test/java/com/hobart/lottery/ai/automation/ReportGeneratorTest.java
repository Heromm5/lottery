package com.hobart.lottery.ai.automation;

import com.hobart.lottery.ai.dto.AiAnalysisReport;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
import com.hobart.lottery.mapper.AiReportMapper;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ReportGenerator Test Class
 */
@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

    @Mock
    private AIGateway gateway;

    @Mock
    private AnalysisFacade analysisFacade;

    @Mock
    private AiReportMapper reportMapper;

    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator(gateway, analysisFacade, reportMapper);
    }

    @Test
    void shouldGenerateDailyReport() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(30))
            .thenReturn("=== 号码频率分析 ===\n前区频率 Top10: ...");

        String mockReport = "{"
            + "\"reportType\":\"daily\","
            + "\"summary\":\"日报分析完成\","
            + "\"content\":{\"hotNumbers\":{\"front\":[7,18],\"back\":[9]}},"
            + "\"insights\":{\"confidence\":0.8}"
            + "}";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockReport));

        // Execute report generation
        AiAnalysisReport report = reportGenerator.generateReport("daily", null, null);

        // Verify results
        assertNotNull(report);
        assertEquals("daily", report.getReportType());
        assertNotNull(report.getSummary());
        assertNotNull(report.getContent());
        assertNotNull(report.getInsights());
        assertNotNull(report.getGeneratedAt());

        // Verify database save
        verify(reportMapper, times(1)).insert(any());
    }

    @Test
    void shouldGenerateWeeklyReport() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(30))
            .thenReturn("=== 周报分析数据 ===");

        String mockReport = "{"
            + "\"reportType\":\"weekly\","
            + "\"summary\":\"周报分析完成\","
            + "\"content\":{\"trends\":\"上升趋势\"},"
            + "\"insights\":{\"confidence\":0.85}"
            + "}";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockReport));

        // Execute
        AiAnalysisReport report = reportGenerator.generateReport("weekly", null, null);

        // Verify
        assertNotNull(report);
        assertEquals("weekly", report.getReportType());
        verify(reportMapper, times(1)).insert(any());
    }

    @Test
    void shouldGenerateIssueReport() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(30))
            .thenReturn("=== 期次分析数据 ===");

        String mockReport = "{"
            + "\"reportType\":\"issue\","
            + "\"summary\":\"期次分析完成\","
            + "\"content\":{\"issueRange\":\"2025001-2025010\"},"
            + "\"insights\":{\"confidence\":0.9}"
            + "}";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockReport));

        // Execute with issue range
        AiAnalysisReport report = reportGenerator.generateReport("issue", 2025001, 2025010);

        // Verify
        assertNotNull(report);
        assertEquals("issue", report.getReportType());
    }

    @Test
    void shouldFallbackToMockWhenAiUnavailable() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(30))
            .thenReturn("=== 分析数据 ===");

        // Simulate AI gateway throwing exception
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenThrow(new RuntimeException("AI service unavailable"));

        // Execute - should fallback to mock
        AiAnalysisReport report = reportGenerator.generateReport("daily", null, null);

        // Verify - should still return a report with default content
        assertNotNull(report);
        assertEquals("daily", report.getReportType());
        assertNotNull(report.getGeneratedAt());
    }

    @Test
    void shouldSaveReportToDatabase() {
        // Prepare mock data
        when(analysisFacade.getComprehensiveAnalysis(30))
            .thenReturn("=== 数据 ===");

        String mockReport = "{"
            + "\"reportType\":\"daily\","
            + "\"summary\":\"测试报告\","
            + "\"content\":{\"test\":\"data\"},"
            + "\"insights\":{\"confidence\":0.5}"
            + "}";
        when(gateway.call(any(AiProvider.class), anyString(), (Supplier<String>) any()))
            .thenReturn(Optional.of(mockReport));

        // Execute
        reportGenerator.generateReport("daily", null, null);

        // Verify mapper was called
        verify(reportMapper, times(1)).insert(any());
    }
}
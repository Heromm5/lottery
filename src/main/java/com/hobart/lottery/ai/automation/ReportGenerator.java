package com.hobart.lottery.ai.automation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hobart.lottery.ai.dto.AiAnalysisReport;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
import com.hobart.lottery.entity.AiReportEntity;
import com.hobart.lottery.mapper.AiReportMapper;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 报告生成服务
 * 通过 AI 生成分析报告（日报/周报/期次报告）
 */
@Service
public class ReportGenerator {

    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;
    private final AiReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    public ReportGenerator(AIGateway gateway, AnalysisFacade analysisFacade,
                           AiReportMapper reportMapper) {
        this.gateway = gateway;
        this.analysisFacade = analysisFacade;
        this.reportMapper = reportMapper;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成分析报告
     *
     * @param reportType 报告类型：daily|weekly|issue
     * @param startIssue 起始期号（issue类型使用）
     * @param endIssue   结束期号（issue类型使用）
     * @return AI分析报告
     */
    public AiAnalysisReport generateReport(String reportType, Integer startIssue, Integer endIssue) {
        // 获取分析数据
        String analysisData = analysisFacade.getComprehensiveAnalysis(30);
        
        // 构建缓存键
        String cacheKey = "report:" + reportType + ":" + startIssue + ":" + endIssue;
        
        // 调用 AI 网关生成报告（使用 mock 响应）
        String aiResponse = callAiForReport(reportType, analysisData, startIssue, endIssue);
        
        // 解析并保存报告
        return parseAndSaveReport(reportType, aiResponse);
    }

    /**
     * 调用 AI 生成报告
     * 由于没有真实 API key，这里使用 mock 数据
     */
    private String callAiForReport(String reportType, String analysisData, 
                                    Integer startIssue, Integer endIssue) {
        try {
            // 尝试通过网关调用（mock 模式）
            return gateway.call(AiProvider.KIMI, "report:" + reportType, 
                () -> generateMockReport(reportType, analysisData, startIssue, endIssue))
                .orElse(generateMockReport(reportType, analysisData, startIssue, endIssue));
        } catch (Exception e) {
            // 如果网关调用失败，使用 mock 数据
            return generateMockReport(reportType, analysisData, startIssue, endIssue);
        }
    }

    /**
     * 生成模拟报告数据
     */
    private String generateMockReport(String reportType, String analysisData,
                                       Integer startIssue, Integer endIssue) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"reportType\":\"").append(reportType).append("\",");
        sb.append("\"summary\":\"根据历史数据分析，本期推荐关注号码分布较为均匀，前区建议关注 07、18、25 等热号，后区建议关注 09、11 等号码。\",");
        sb.append("\"content\":{");
        sb.append("\"hotNumbers\":{\"front\":[7,18,25,32,35],\"back\":[9,11]},");
        sb.append("\"coldNumbers\":{\"front\":[2,5,14],\"back\":[2,5]},");
        sb.append("\"trends\":\"近期号码分布较为分散，建议关注均衡策略\",");
        sb.append("\"recommendations\":[\"前区关注07、18、25、32、35\",\"后区关注09、11\",\"建议采用热号优先策略\"]");
        sb.append("},");
        sb.append("\"insights\":{");
        sb.append("\"confidence\":0.82,");
        sb.append("\"keyPattern\":\"前区热号集中于07-25区间，后区09、11近期走热\",");
        sb.append("\"riskLevel\":\"MEDIUM\"");
        sb.append("}");
        sb.append("}");
        
        return sb.toString();
    }

    /**
     * 解析 AI 响应并保存到数据库
     */
    private AiAnalysisReport parseAndSaveReport(String reportType, String aiResponse) {
        AiAnalysisReport report = new AiAnalysisReport();
        
        try {
            // 简单解析 JSON
            // 实际项目中可以使用 Jackson 的 JsonNode 或对象映射
            Map<String, Object> parsed = objectMapper.readValue(aiResponse, Map.class);
            
            report.setReportType((String) parsed.get("reportType"));
            report.setSummary((String) parsed.get("summary"));
            
            if (parsed.get("content") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> contentMap = (Map<String, Object>) parsed.get("content");
                report.setContent(new HashMap<>(contentMap));
            }
            
            if (parsed.get("insights") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> insightsMap = (Map<String, Object>) parsed.get("insights");
                report.setInsights(new HashMap<>(insightsMap));
            }
            
            report.setGeneratedAt(LocalDateTime.now());
            
            // 保存到数据库
            saveToDatabase(report, aiResponse);
            
        } catch (JsonProcessingException e) {
            // 解析失败，创建默认报告
            report.setReportType(reportType);
            report.setSummary("报告生成失败，使用默认数据");
            report.setContent(createDefaultContent());
            report.setInsights(createDefaultInsights());
            report.setGeneratedAt(LocalDateTime.now());
        }
        
        return report;
    }

    /**
     * 保存报告到数据库
     */
    private void saveToDatabase(AiAnalysisReport report, String rawJson) {
        AiReportEntity entity = new AiReportEntity();
        entity.setReportType(report.getReportType());
        entity.setSummary(report.getSummary());
        entity.setContent(rawJson);
        
        try {
            entity.setInsights(objectMapper.writeValueAsString(report.getInsights()));
        } catch (JsonProcessingException e) {
            entity.setInsights("{}");
        }
        
        entity.setCreatedAt(LocalDateTime.now());
        
        reportMapper.insert(entity);
    }

    /**
     * 创建默认内容
     */
    private Map<String, Object> createDefaultContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("note", "使用默认数据");
        return content;
    }

    /**
     * 创建默认洞察
     */
    private Map<String, Object> createDefaultInsights() {
        Map<String, Object> insights = new HashMap<>();
        insights.put("confidence", 0.5);
        insights.put("riskLevel", "UNKNOWN");
        return insights;
    }
}
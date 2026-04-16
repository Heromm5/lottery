package com.hobart.lottery.ai.automation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hobart.lottery.ai.dto.AnomalyAlert;
import com.hobart.lottery.ai.gateway.AIGateway;
import com.hobart.lottery.ai.gateway.AiProvider;
import com.hobart.lottery.entity.AnomalyAlertEntity;
import com.hobart.lottery.mapper.AnomalyAlertMapper;
import com.hobart.lottery.service.analysis.AnalysisFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 异常检测器
 * 使用 AI 分析历史数据，检测异常模式并生成告警
 */
@Slf4j
@Service
public class AnomalyDetector {

    private final AIGateway gateway;
    private final AnalysisFacade analysisFacade;
    private final AnomalyAlertMapper alertMapper;
    private final ObjectMapper objectMapper;

    public AnomalyDetector(AIGateway gateway, AnalysisFacade analysisFacade,
                        AnomalyAlertMapper alertMapper) {
        this.gateway = gateway;
        this.analysisFacade = analysisFacade;
        this.alertMapper = alertMapper;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 检测异常
     *
     * @param periods 历史期数
     * @return 告警列表
     */
    public List<AnomalyAlert> detectAnomalies(int periods) {
        // 获取分析数据
        String currentData = analysisFacade.getComprehensiveAnalysis(periods);
        String cacheKey = "anomaly:" + periods;

        // 调用 AI 分析（使用 mock 响应）
        String aiResponse = callAiForAnomalies(currentData);

        // 解析告警
        List<AnomalyAlert> alerts = parseAlerts(aiResponse);

        // 保存告警
        List<AnomalyAlert> savedAlerts = new ArrayList<>();
        for (AnomalyAlert alert : alerts) {
            AnomalyAlert saved = saveAlert(alert);
            savedAlerts.add(saved);
        }

        return savedAlerts;
    }

    /**
     * 获取告警列表
     *
     * @param severity     严重程度筛选（可选）
     * @param acknowledged 确认状态筛选（可选）
     * @return 告警列表
     */
    public List<AnomalyAlert> getAlerts(String severity, Boolean acknowledged) {
        QueryWrapper<AnomalyAlertEntity> wrapper = new QueryWrapper<>();

        if (severity != null && !severity.isEmpty()) {
            wrapper.eq("severity", severity);
        }
        if (acknowledged != null) {
            wrapper.eq("acknowledged", acknowledged);
        }

        wrapper.orderByDesc("detected_at");

        List<AnomalyAlertEntity> entities = alertMapper.selectList(wrapper);
        return convertToDto(entities);
    }

    /**
     * 确认告警
     *
     * @param id 告警ID
     * @return 是否成功
     */
    public boolean acknowledgeAlert(Long id) {
        AnomalyAlertEntity entity = alertMapper.selectById(id);
        if (entity == null) {
            return false;
        }
        entity.setAcknowledged(true);
        entity.setAcknowledgedAt(LocalDateTime.now());
        return alertMapper.updateById(entity) > 0;
    }

    /**
     * 调用 AI 分析异常
     * 这里使用 mock 实现，后续接入真实 AI API
     */
    private String callAiForAnomalies(String data) {
        // 尝试使用 AIGateway（如果可用）
        try {
            Optional<String> result = gateway.call(AiProvider.GPT4O, "anomaly:" + data.hashCode(),
                () -> generateMockAnomalyResponse(data));
            return result.orElseGet(() -> generateMockAnomalyResponse(data));
        } catch (Exception e) {
            log.warn("AI gateway 调用失败，使用 mock 响应: {}", e.getMessage());
            return generateMockAnomalyResponse(data);
        }
    }

    /**
     * 生成 mock 异常响应
     */
    private String generateMockAnomalyResponse(String data) {
        // 简单的 mock 实现：分析数据中可能的异常模式
        StringBuilder response = new StringBuilder();
        response.append("[");

        // 检测数据中是否有异常高的频率
        if (data.contains("前区频率 Top10") && data.contains("01:")) {
            response.append("{\"type\":\"HIGH_FREQUENCY\",\"severity\":\"MEDIUM\",");
            response.append("\"description\":\"检测到号码01出现频率异常高\",");
            response.append("\"data\":{\"number\":1,\"count\":15,\"threshold\":10}}");
        }

        // 检测是否有遗漏到期的号码
        if (data.contains("遗漏到期") && data.contains("20:")) {
            if (response.length() > 1) {
                response.append(",");
            }
            response.append("{\"type\":\"MISSING_DUE\",\"severity\":\"HIGH\",");
            response.append("\"description\":\"号码20遗漏超过30期，已到回补期\",");
            response.append("\"data\":{\"number\":20,\"missing\":32,\"threshold\":30}}");
        }

        response.append("]");
        return response.toString();
    }

    /**
     * 解析 AI 响应为告警列表
     */
    private List<AnomalyAlert> parseAlerts(String aiResponse) {
        List<AnomalyAlert> alerts = new ArrayList<>();

        try {
            // 解析 JSON 数组
            List<Map<String, Object>> items = objectMapper.readValue(aiResponse,
                new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> item : items) {
                AnomalyAlert alert = new AnomalyAlert();
                alert.setType((String) item.get("type"));
                alert.setSeverity((String) item.get("severity"));
                alert.setDescription((String) item.get("description"));

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) item.get("data");
                if (data != null) {
                    alert.setData(data);
                }

                alert.setDetectedAt(LocalDateTime.now());
                alert.setAcknowledged(false);

                alerts.add(alert);
            }
        } catch (JsonProcessingException e) {
            log.error("解析 AI 响应失败: {}", e.getMessage());
        }

        return alerts;
    }

    /**
     * 保存告警到数据库
     */
    private AnomalyAlert saveAlert(AnomalyAlert alert) {
        AnomalyAlertEntity entity = new AnomalyAlertEntity();
        entity.setAlertType(alert.getType());
        entity.setSeverity(alert.getSeverity());
        entity.setDescription(alert.getDescription());

        try {
            entity.setDetectedData(objectMapper.writeValueAsString(alert.getData()));
        } catch (JsonProcessingException e) {
            entity.setDetectedData("{}");
        }

        entity.setAcknowledged(false);
        entity.setDetectedAt(LocalDateTime.now());

        alertMapper.insert(entity);

        alert.setId(entity.getId());
        return alert;
    }

    /**
     * 转换实体为 DTO
     */
    private List<AnomalyAlert> convertToDto(List<AnomalyAlertEntity> entities) {
        List<AnomalyAlert> alerts = new ArrayList<>();

        for (AnomalyAlertEntity entity : entities) {
            AnomalyAlert alert = new AnomalyAlert();
            alert.setId(entity.getId());
            alert.setType(entity.getAlertType());
            alert.setSeverity(entity.getSeverity());
            alert.setDescription(entity.getDescription());
            alert.setAcknowledged(entity.getAcknowledged());
            alert.setDetectedAt(entity.getDetectedAt());

            try {
                Map<String, Object> data = objectMapper.readValue(entity.getDetectedData(),
                    new TypeReference<Map<String, Object>>() {});
                alert.setData(data);
            } catch (JsonProcessingException e) {
                alert.setData(new HashMap<>());
            }

            alerts.add(alert);
        }

        return alerts;
    }
}
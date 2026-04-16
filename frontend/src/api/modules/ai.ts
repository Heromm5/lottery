import { request } from '../axios'

export interface AiPredictRequest {
  count: number
  historyPeriods?: number
  method?: string
}

export interface AiPredictResponse {
  predictions: Array<{
    frontBalls: number[]
    backBalls: number[]
    confidence: number
    method: string
  }>
  model: string
  timestamp: string
}

export interface AiAnalyzeRequest {
  periods?: number
  analysisType?: string
}

export interface AiAnalyzeResponse {
  patterns: Array<{
    type: string
    description: string
    confidence: number
    evidence: string[]
  }>
  timestamp: string
}

export interface AiReportRequest {
  reportType: string
  startIssue?: number
  endIssue?: number
}

export interface AiReportResponse {
  reportId: string
  content: string
  summary: string
  generatedAt: string
}

export interface AiAnomalyAlert {
  id: string
  type: string
  severity: 'low' | 'medium' | 'high' | 'critical'
  message: string
  detectedAt: string
  acknowledged: boolean
  acknowledgedAt?: string
}

export const aiApi = {
  predict: (data: AiPredictRequest) =>
    request.post<AiPredictResponse>('/ai/predict', data),

  analyzePatterns: (data: AiAnalyzeRequest) =>
    request.post<AiAnalyzeResponse>('/ai/analyze-patterns', data),

  generateReport: (data: AiReportRequest) =>
    request.post<AiReportResponse>('/ai/report', data),

  getAnomalyAlerts: (params?: { severity?: string; acknowledged?: boolean }) =>
    request.get<AiAnomalyAlert[]>('/ai/anomaly-alerts', { params })
}

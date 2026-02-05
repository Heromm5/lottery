import { request } from '../axios'
import type { PredictionRecord, PredictionResult, Result, PageResult } from '@/types'

// 预测相关 API
export const predictionApi = {
  // 获取所有预测方法
  getMethods: () =>
    request.get<Result<Array<{ code: string; name: string }>>>('/prediction/methods'),

  // 生成预测并保存
  generate: (count = 5, method?: string, targetIssue?: string) =>
    request.post<Result<PredictionResult[]>>('/prediction/generate', null, {
      params: { count, method, targetIssue }
    }),

  // 生成最优预测（每种方法选最优一注）
  generateBest: (candidateCount = 10, targetIssue?: string) =>
    request.post<Result<PredictionResult[]>>('/prediction/generate/best', null, {
      params: { candidateCount, targetIssue }
    }),

  // 获取某期预测记录
  getByIssue: (issue: string) =>
    request.get<Result<PredictionResult[]>>(`/prediction/issue/${issue}`),

  // 获取最近的预测记录
  getRecent: (limit = 20) =>
    request.get<Result<PredictionResult[]>>('/prediction/recent', {
      params: { limit }
    }),

  // 分页查询预测记录
  getList: (page = 1, size = 20) =>
    request.get<Result<PageResult<PredictionRecord>>>('/prediction/list', {
      params: { page, size }
    }),

  // 获取预测详情
  getDetail: (id: number) =>
    request.get<Result<PredictionRecord>>(`/prediction/${id}`),

  // 为预测结果评分（返回带分数的结果，按分数降序）
  score: (predictions: PredictionResult[]) =>
    request.post<Result<PredictionResult[]>>('/prediction/score', predictions)
}

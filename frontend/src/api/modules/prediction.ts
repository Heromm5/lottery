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

  // 分页查询预测记录（status: all | verified | unverified），接口返回已解包为 PageResult
  getList: (page = 1, size = 50, status = 'all') =>
    request.get<PageResult<PredictionRecord>>('/prediction/list', {
      params: { page, size, status }
    }),

  // 获取预测详情
  getDetail: (id: number) =>
    request.get<Result<PredictionRecord>>(`/prediction/${id}`),

  // 为预测结果评分（返回带分数的结果，按分数降序）
  score: (predictions: PredictionResult[]) =>
    request.post<Result<PredictionResult[]>>('/prediction/score', predictions),

  // 将指定记录标记为当次最终预测（生成时选中的 Top N 注）
  markFinal: (recordIds: number[]) =>
    request.post<Result<void>>('/prediction/mark-final', recordIds),

  // 获取下一预测期号（最新预测期号+1；无预测时为最新开奖期号+1）
  getNextIssue: () =>
    request.get<Result<string>>('/prediction/next-issue')
}

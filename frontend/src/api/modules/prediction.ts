import { request } from '../axios'
import type { GeneratePinnedRequest, PredictionRecord, PredictionResult, PageResult } from '@/types'

export const predictionApi = {
  getMethods: () =>
    request.get<Array<{ code: string; name: string }>>('/prediction/methods'),

  generate: (count = 5, method?: string, targetIssue?: string) =>
    request.post<PredictionResult[]>('/prediction/generate', null, {
      params: { count, method, targetIssue }
    }),

  generatePinned: (body: GeneratePinnedRequest) =>
    request.post<PredictionResult[]>('/prediction/generate-pinned', body),

  generateBest: (candidateCount = 10, targetIssue?: string) =>
    request.post<PredictionResult[]>('/prediction/generate/best', null, {
      params: { candidateCount, targetIssue }
    }),

  getByIssue: (issue: string) =>
    request.get<PredictionResult[]>(`/prediction/issue/${issue}`),

  getRecent: (limit = 20) =>
    request.get<PredictionResult[]>('/prediction/recent', {
      params: { limit }
    }),

  getList: (page = 1, size = 50, status = 'all') =>
    request.get<PageResult<PredictionRecord>>('/prediction/list', {
      params: { page, size, status }
    }),

  getDetail: (id: number) =>
    request.get<PredictionRecord>(`/prediction/${id}`),

  score: (predictions: PredictionResult[]) =>
    request.post<PredictionResult[]>('/prediction/score', predictions),

  markFinal: (recordIds: number[]) =>
    request.post<void>('/prediction/mark-final', recordIds),

  getNextIssue: () =>
    request.get<string>('/prediction/next-issue')
}

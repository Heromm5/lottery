import { request } from '../axios'
import type { LotteryResult, PageResult } from '@/types'

export const lotteryApi = {
  getLatest: () => request.get<LotteryResult>('/lottery/latest'),

  getRecent: (size = 10) =>
    request.get<LotteryResult[]>('/lottery/recent', { params: { size } }),

  getList: (page = 1, size = 20) =>
    request.get<PageResult<LotteryResult>>('/lottery/list', {
      params: { page, size }
    }),

  search: (keyword: string, page = 1, size = 20) =>
    request.get<PageResult<LotteryResult>>('/lottery/search', {
      params: { keyword, page, size }
    }),

  add: (data: Partial<LotteryResult>) =>
    request.post<number>('/lottery/add', data),

  delete: (id: number) =>
    request.delete<void>(`/lottery/delete/${id}`),

  getStats: () =>
    request.get<{
      totalCount: number
      nextIssue: string
      methodCount: number
      statsCount: number
    }>('/lottery/stats')
}

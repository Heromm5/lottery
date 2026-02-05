import { request } from '../axios'
import type { LotteryResult, PageResult, Result } from '@/types'

// 彩票相关 API
export const lotteryApi = {
  // 获取最新开奖
  getLatest: () => request.get<Result<LotteryResult>>('/lottery/latest'),

  // 获取近期开奖
  getRecent: (size = 10) =>
    request.get<Result<LotteryResult[]>>('/lottery/recent', { params: { size } }),

  // 分页查询
  getList: (page = 1, size = 20) =>
    request.get<Result<PageResult<LotteryResult>>>('/lottery/list', {
      params: { page, size }
    }),

  // 搜索
  search: (keyword: string, page = 1, size = 20) =>
    request.get<Result<PageResult<LotteryResult>>>('/lottery/search', {
      params: { keyword, page, size }
    }),

  // 添加开奖结果
  add: (data: Partial<LotteryResult>) =>
    request.post<Result<number>>('/lottery/add', data),

  // 删除
  delete: (id: number) =>
    request.delete<Result<void>>(`/lottery/delete/${id}`),

  // 获取统计数据
  getStats: () =>
    request.get<Result<{
      totalCount: number
      nextIssue: string
      methodCount: number
      statsCount: number
    }>>('/lottery/stats')
}

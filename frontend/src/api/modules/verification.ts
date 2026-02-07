import { request } from '../axios'
import type { AccuracyStats, BacktestResult, Result, PageResult } from '@/types'

// 验证历史记录类型
export interface VerificationHistoryRecord {
  id: number
  targetIssue: string
  predictMethod: string
  methodName: string
  frontBallsStr: string
  backBallsStr: string
  actualFrontBallsStr: string
  actualBackBallsStr: string
  frontHitCount: number
  backHitCount: number
  prizeLevel: string
  createdAt: string
  verifiedAt: string
}

// 验证相关 API
export const verificationApi = {
  // 获取准确率统计
  getAccuracyStats: () =>
    request.get<Result<AccuracyStats[]>>('/verification/stats'),

  // 获取准确率排行榜
  getAccuracyRanking: (sortBy = 'composite', ascending = false) =>
    request.get<Result<AccuracyStats[]>>('/verification/stats/ranking', {
      params: { sortBy, ascending }
    }),

  // 获取验证历史
  getHistory: (page = 1, size = 20) =>
    request.get<Result<PageResult<VerificationHistoryRecord>>>('/verification/history', {
      params: { page, size }
    }),

  // 获取未验证的期号列表
  getUnverifiedIssues: () =>
    request.get<Result<string[]>>('/verification/unverified/issues'),

  // 检查某期是否有开奖结果
  checkDrawResult: (issue: string) =>
    request.get<Result<boolean>>(`/verification/check/${issue}`),

  // 触发验证
  triggerVerify: (issue: string) =>
    request.post<Result<any>>(`/verification/verify/${issue}`),

  // 批量历史回测（增加超时时间，因为回测需要较长时间）
  runBacktest: (method?: string, issueCount = 50, predictionsPerIssue = 5) =>
    request.post<Result<BacktestResult[]>>('/verification/backtest', null, {
      params: { method, issueCount, predictionsPerIssue },
      timeout: 120000 // 120秒超时
    })
}

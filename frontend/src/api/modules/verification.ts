import { request } from '../axios'
import type { AccuracyStats, BacktestResult, PageResult } from '@/types'

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
  isFinal?: number
}

export const verificationApi = {
  getAccuracyStats: () =>
    request.get<AccuracyStats[]>('/verification/stats'),

  getAccuracyRanking: (sortBy = 'composite', ascending = false) =>
    request.get<AccuracyStats[]>('/verification/stats/ranking', {
      params: { sortBy, ascending }
    }),

  getHistory: (page = 1, size = 20) =>
    request.get<PageResult<VerificationHistoryRecord>>('/verification/history', {
      params: { page, size }
    }),

  getUnverifiedIssues: () =>
    request.get<string[]>('/verification/unverified/issues'),

  checkDrawResult: (issue: string) =>
    request.get<boolean>(`/verification/check/${issue}`),

  triggerVerify: (issue: string) =>
    request.post<any>(`/verification/verify/${issue}`),

  runBacktest: (method?: string, issueCount = 50, predictionsPerIssue = 5) =>
    request.post<BacktestResult[]>('/verification/backtest', null, {
      params: { method, issueCount, predictionsPerIssue },
      timeout: 120000
    })
}

import { request } from '../axios'
import type { AccuracyStats, Result, PageResult } from '@/types'

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
    request.post<Result<any>>(`/verification/verify/${issue}`)
}

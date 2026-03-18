import { request } from '../axios'
import type {
  FrequencyDTO,
  MissingDTO,
  TrendData,
  AccuracyStats,
  Result,
  PageResult
} from '@/types'

// 尾数频率 DTO 类型
export interface DigitFrequencyDTO {
  digit: number
  count: number
  frequency: number
  missing: number
}

// 分析相关 API
export const analysisApi = {
  // 前区频率统计
  getFrontFrequency: (recentCount?: number) =>
    request.get<Result<FrequencyDTO[]>>('/analysis/frequency/front', {
      params: recentCount ? { recentCount } : {}
    }),

  // 后区频率统计
  getBackFrequency: (recentCount?: number) =>
    request.get<Result<FrequencyDTO[]>>('/analysis/frequency/back', {
      params: recentCount ? { recentCount } : {}
    }),

  // 前区遗漏分析
  getFrontMissing: () =>
    request.get<Result<MissingDTO[]>>('/analysis/missing/front'),

  // 后区遗漏分析
  getBackMissing: () =>
    request.get<Result<MissingDTO[]>>('/analysis/missing/back'),

  // 走势分析
  getTrend: (size = 30) =>
    request.get<Result<TrendData[]>>('/analysis/trend', { params: { size } }),

  // 奇偶比统计
  getOddEvenStats: () =>
    request.get<Result<Record<string, number>>>('/analysis/stats/odd-even'),

  // 和值分布统计
  getSumStats: () =>
    request.get<Result<Record<string, number>>>('/analysis/stats/sum'),

  // 连号统计
  getConsecutiveStats: () =>
    request.get<Result<Record<number, number>>>('/analysis/stats/consecutive'),

  // 热号推荐
  getHotNumbers: (count = 10) =>
    request.get<Result<{ front: number[]; back: number[] }>>('/analysis/hot', {
      params: { count }
    }),

  // 冷号推荐
  getColdNumbers: (count = 10) =>
    request.get<Result<{ front: number[]; back: number[] }>>('/analysis/cold', {
      params: { count }
    }),

  // 遗漏到期号码
  getMissingDue: (count = 10) =>
    request.get<Result<{ front: number[]; back: number[] }>>('/analysis/missing-due', {
      params: { count }
    }),

  // 关联规则列表
  getAssociationRules: (zone: 'front' | 'back' = 'front') =>
    request.get<Result<AssociationRule[]>>('/analysis/association', {
      params: { zone }
    }),

  // 关联网络数据
  getAssociationNetwork: (zone: 'front' | 'back' = 'front', topN = 50) =>
    request.get<Result<any>>('/analysis/association/network', {
      params: { zone, topN }
    }),

  // 尾数频率统计
  getDigitFrequency: (zone: 'front' | 'back' = 'front') =>
    request.get<Result<DigitFrequencyDTO[]>>('/analysis/digit/frequency', {
      params: { zone }
    }),

  // 尾数和值统计
  getDigitSumStats: () =>
    request.get<Result<Record<string, number>>>('/analysis/digit/sum'),

  // 区间分布统计
  getZoneDistribution: () =>
    request.get<Result<Record<string, Record<string, number>>>>('/analysis/zone/distribution'),

  // 统计信息（兼容性方法）
  getStats: () =>
    request.get<Result<{
      oddEvenStats: Record<string, number>
      sumRangeStats: Record<string, number>
    }>>('/analysis/stats/odd-even')
}

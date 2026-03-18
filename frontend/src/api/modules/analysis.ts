import { request } from '../axios'
import type { FrequencyDTO, MissingDTO, TrendData, AssociationRule } from '@/types'

export interface DigitFrequencyDTO {
  digit: number
  count: number
  frequency: number
  missing: number
}

export const analysisApi = {
  getFrontFrequency: (recentCount?: number) =>
    request.get<FrequencyDTO[]>('/analysis/frequency/front', {
      params: recentCount ? { recentCount } : {}
    }),

  getBackFrequency: (recentCount?: number) =>
    request.get<FrequencyDTO[]>('/analysis/frequency/back', {
      params: recentCount ? { recentCount } : {}
    }),

  getFrontMissing: () =>
    request.get<MissingDTO[]>('/analysis/missing/front'),

  getBackMissing: () =>
    request.get<MissingDTO[]>('/analysis/missing/back'),

  getTrend: (size = 30) =>
    request.get<TrendData[]>('/analysis/trend', { params: { size } }),

  getOddEvenStats: () =>
    request.get<Record<string, number>>('/analysis/stats/odd-even'),

  getSumStats: () =>
    request.get<Record<string, number>>('/analysis/stats/sum'),

  getConsecutiveStats: () =>
    request.get<Record<number, number>>('/analysis/stats/consecutive'),

  getHotNumbers: (count = 10) =>
    request.get<{ front: number[]; back: number[] }>('/analysis/hot', {
      params: { count }
    }),

  getColdNumbers: (count = 10) =>
    request.get<{ front: number[]; back: number[] }>('/analysis/cold', {
      params: { count }
    }),

  getMissingDue: (count = 10) =>
    request.get<{ front: number[]; back: number[] }>('/analysis/missing-due', {
      params: { count }
    }),

  getAssociationRules: (zone: 'front' | 'back' = 'front') =>
    request.get<AssociationRule[]>('/analysis/association', {
      params: { zone }
    }),

  getAssociationNetwork: (zone: 'front' | 'back' = 'front', topN = 50) =>
    request.get<any>('/analysis/association/network', {
      params: { zone, topN }
    }),

  getDigitFrequency: (zone: 'front' | 'back' = 'front') =>
    request.get<DigitFrequencyDTO[]>('/analysis/digit/frequency', {
      params: { zone }
    }),

  getDigitSumStats: () =>
    request.get<Record<string, number>>('/analysis/digit/sum'),

  getZoneDistribution: () =>
    request.get<Record<string, Record<string, number>>>('/analysis/zone/distribution'),

  getStats: () =>
    request.get<{
      oddEvenStats: Record<string, number>
      sumRangeStats: Record<string, number>
    }>('/analysis/stats/odd-even')
}

import { request } from '../axios'
import type { Result, PageResult } from '@/types'

export interface MethodWeight {
  id: number
  methodName: string
  weight: number
  description?: string
}

// 学习相关 API
export const learningApi = {
  // 获取所有方法权重
  getWeights: () =>
    request.get<Result<MethodWeight[]>>('/learning/weights'),

  // 更新权重
  updateWeight: (id: number, weight: number) =>
    request.put<Result<void>>(`/learning/weights/${id}`, { weight }),

  // 批量更新权重
  batchUpdateWeights: (weights: { id: number; weight: number }[]) =>
    request.put<Result<void>>('/learning/weights/batch', weights),

  // 重新训练模型
  retrain: () =>
    request.post<Result<void>>('/learning/retrain')
}

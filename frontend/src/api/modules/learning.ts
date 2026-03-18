import { request } from '../axios'

export interface MethodWeight {
  id: number
  methodName: string
  weight: number
  description?: string
}

export const learningApi = {
  getWeights: () =>
    request.get<MethodWeight[]>('/learning/weights'),

  updateWeight: (id: number, weight: number) =>
    request.put<void>(`/learning/weights/${id}`, { weight }),

  batchUpdateWeights: (weights: { id: number; weight: number }[]) =>
    request.put<void>('/learning/weights/batch', weights),

  retrain: () =>
    request.post<void>('/learning/retrain')
}

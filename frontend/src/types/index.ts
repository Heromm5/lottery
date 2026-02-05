// 统一响应结构
export interface Result<T> {
  code: number
  msg: string
  data: T
}

// 分页结构
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 彩票相关类型
export interface LotteryResult {
  id: number
  issue: string
  drawDate: string
  frontBall1: number
  frontBall2: number
  frontBall3: number
  frontBall4: number
  frontBall5: number
  backBall1: number
  backBall2: number
  frontSum: number
  oddCountFront: number
  consecutiveCountFront: number
}

// 预测相关类型
export interface PredictionRecord {
  id: number
  targetIssue: string
  methodName: string
  frontBalls: number[]
  backBalls: number[]
  isVerified: number
  frontHitCount: number
  backHitCount: number
  prizeLevel: string
  createdAt: string
  verifiedAt?: string
}

export interface PredictionResult {
  id?: number
  targetIssue?: string
  predictMethod?: string
  methodName: string
  frontBalls: number[]
  backBalls: number[]
  frontBallsStr?: string
  backBallsStr?: string
  confidence?: number
  verified?: boolean
  frontHitCount?: number
  backHitCount?: number
  prizeLevel?: string
  score?: number
}

// 分析相关类型
export interface FrequencyDTO {
  number: number
  zone: 'front' | 'back'
  count: number
  percentage: number
}

export interface MissingDTO {
  number: number
  currentMissing: number
  avgMissing: number
  maxMissing: number
  type?: string
}

export interface TrendData {
  issue: string
  numbers: number[]
  [key: string]: any
}

export interface AssociationRule {
  antecedent: number[]
  consequent: number[]
  support: number
  confidence: number
  lift: number
}

export interface AccuracyStats {
  methodName: string
  totalPredictions: number
  frontAvgHit: number
  backAvgHit: number
  totalPrizeCount: number
  prizeRate?: number
}

// 统计相关类型
export interface StatCard {
  label: string
  value: number | string
  icon?: string
  trend?: 'up' | 'down' | 'stable'
  trendValue?: string
}

// 搜索筛选类型
export interface SearchFilters {
  keyword?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

// 预测方法配置（与后端 PredictionMethod 枚举保持一致）
export interface PredictionMethodConfig {
  code: string
  displayName: string
  description: string
}

// 所有预测方法配置列表
export const PREDICTION_METHODS: PredictionMethodConfig[] = [
  { code: 'HOT', displayName: '热号优先', description: '基于近期出现频率较高的号码进行预测' },
  { code: 'MISSING', displayName: '遗漏回补', description: '根据号码的历史遗漏值，预测回补概率高的号码' },
  { code: 'BALANCED', displayName: '冷热均衡', description: '综合考虑热号和冷号，追求预测结果的均衡性' },
  { code: 'ML', displayName: '机器学习', description: '使用机器学习算法分析历史数据，挖掘潜在规律' },
  { code: 'ADAPTIVE', displayName: '自适应预测', description: '根据当前数据特征自动选择最佳预测策略' },
  { code: 'BAYESIAN', displayName: '贝叶斯预测', description: '基于贝叶斯概率论进行号码预测' },
  { code: 'MARKOV', displayName: '马尔可夫预测', description: '使用马尔可夫链模型预测号码转移概率' },
  { code: 'MONTECARLO', displayName: '蒙特卡洛预测', description: '基于蒙特卡洛模拟进行随机抽样预测' },
  { code: 'GRADIENT_BOOST', displayName: '梯度提升预测', description: '使用梯度提升决策树进行预测' },
  { code: 'ENSEMBLE', displayName: '集成预测', description: '综合多种预测方法的结果进行集成投票' }
]

// 根据代码获取预测方法显示名称
export function getMethodDisplayName(code: string): string {
  const method = PREDICTION_METHODS.find(m => m.code === code)
  return method ? method.displayName : code
}

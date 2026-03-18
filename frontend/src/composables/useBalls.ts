/**
 * 球号解析与格式化工具
 */
export function parseBalls(balls: string | number[] | null | undefined): number[] {
  if (!balls) return []
  if (Array.isArray(balls)) return balls
  const str = String(balls)
  if (!str || str === '-') return []
  return str.split(',').map(Number).filter(n => !isNaN(n))
}

export function formatBalls(balls: number[]): string {
  return balls.join(',')
}

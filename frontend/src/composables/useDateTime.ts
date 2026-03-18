/**
 * 日期时间格式化工具
 */
export function formatDateTime(isoStr: string | null | undefined): string {
  if (!isoStr) return '--'
  const d = new Date(isoStr)
  if (isNaN(d.getTime())) return String(isoStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${s}`
}

export function formatDate(isoStr: string | null | undefined): string {
  if (!isoStr) return '--'
  const d = new Date(isoStr)
  if (isNaN(d.getTime())) return String(isoStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

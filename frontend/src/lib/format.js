export function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export function readingMinutes(text) {
  const source = text || ''
  const cjk = (source.match(/[\u4e00-\u9fff]/g) || []).length
  const latin = (source.replace(/[\u4e00-\u9fff]/g, ' ').match(/[A-Za-z0-9]+/g) || []).length
  const words = cjk + latin
  return Math.max(1, Math.ceil(words / 420))
}
export function formatPrice(amount, currency = 'USD') {
  if (amount === null || amount === undefined || amount === '') return ''
  const num = Number(amount)
  if (Number.isNaN(num)) return String(amount)
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency
  }).format(num)
}

export function formatRelative(iso, locale = 'en') {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  const now = Date.now()
  const diff = Math.round((date.getTime() - now) / 1000)
  const abs = Math.abs(diff)
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' })
  if (abs < 60) return rtf.format(diff, 'second')
  if (abs < 3600) return rtf.format(Math.round(diff / 60), 'minute')
  if (abs < 86400) return rtf.format(Math.round(diff / 3600), 'hour')
  if (abs < 604800) return rtf.format(Math.round(diff / 86400), 'day')
  return new Intl.DateTimeFormat(locale, { year: 'numeric', month: 'short', day: 'numeric' }).format(date)
}

export function formatDate(iso, locale = 'en') {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat(locale, { year: 'numeric', month: 'short', day: 'numeric' }).format(date)
}

export function initials(name) {
  if (!name) return '·'
  return String(name)
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('')
}

export function avatarBackground(name) {
  const hues = ['#ff7e5f', '#6c5ce7', '#10b981', '#ef4444', '#f59e0b', '#0ea5e9', '#8b5cf6']
  if (!name) return hues[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = (hash * 31 + name.charCodeAt(i)) >>> 0
  return hues[hash % hues.length]
}

export function formatMoney(value) {
  const amount = Number(value || 0)
  return `৳${new Intl.NumberFormat('en-BD', {
    maximumFractionDigits: 2,
  }).format(amount)}`
}

export function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en-BD', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date(`${value}T00:00:00`))
}

export function differenceInNights(checkIn, checkOut) {
  if (!checkIn || !checkOut) return 0
  const start = new Date(`${checkIn}T00:00:00`)
  const end = new Date(`${checkOut}T00:00:00`)
  return Math.max(0, Math.round((end - start) / 86_400_000))
}

export function todayString() {
  const now = new Date()
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 10)
}

export function nextDateString(date) {
  if (!date) return todayString()
  const value = new Date(`${date}T00:00:00`)
  value.setDate(value.getDate() + 1)
  return value.toISOString().slice(0, 10)
}

export function buildSearchParams(filters) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== '') params.set(key, value)
  })
  return params.toString()
}

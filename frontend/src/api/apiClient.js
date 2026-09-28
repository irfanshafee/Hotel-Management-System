const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080')
  .replace(/\/$/, '')

const TOKEN_KEY = 'hotelBookingToken'

export class ApiError extends Error {
  constructor(message, status, data = null, response = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.data = data
    this.response = response
  }
}

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function storeToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeStoredToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export async function apiRequest(path, options = {}) {
  const token = getStoredToken()
  const headers = new Headers(options.headers)

  if (options.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  } catch {
    throw new ApiError('Unable to connect to the server. Please try again.', 0)
  }

  const text = await response.text()
  let payload = null
  if (text) {
    try {
      payload = JSON.parse(text)
    } catch {
      payload = null
    }
  }

  if (!response.ok) {
    if (response.status === 401) {
      removeStoredToken()
      window.dispatchEvent(new Event('auth:unauthorized'))
    }
    throw new ApiError(
      payload?.responseMessage || 'The request could not be completed.',
      response.status,
      payload?.data ?? null,
      payload,
    )
  }

  return payload?.data
}

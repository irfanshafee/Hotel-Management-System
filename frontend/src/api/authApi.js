import { apiRequest } from './apiClient'

export const login = (credentials) =>
  apiRequest('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  })

export const register = (details) =>
  apiRequest('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(details),
  })

export const getCurrentUser = () => apiRequest('/api/user/me')

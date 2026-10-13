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

export const verifySignup = (details) =>
  apiRequest('/api/auth/verify-signup', {
    method: 'POST',
    body: JSON.stringify(details),
  })

export const resendSignupOtp = (email) =>
  apiRequest('/api/auth/resend-signup-otp', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })

export const getCurrentUser = () => apiRequest('/api/user/me')
export const forgotPassword = (email) => apiRequest('/api/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) })
export const resetPassword = (token, newPassword) => apiRequest('/api/auth/reset-password', { method: 'POST', body: JSON.stringify({ token, newPassword }) })

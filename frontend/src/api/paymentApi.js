import { apiRequest } from './apiClient'

export const initiatePayment = (bookingId) =>
  apiRequest(`/api/payments/initiate/${bookingId}`, { method: 'POST' })

export const submitPayment = (paymentId, transactionId) =>
  apiRequest(`/api/payments/${paymentId}/submit`, {
    method: 'POST',
    body: JSON.stringify({ transactionId }),
  })

export const getPaymentForBooking = (bookingId) =>
  apiRequest(`/api/payments/booking/${bookingId}`)

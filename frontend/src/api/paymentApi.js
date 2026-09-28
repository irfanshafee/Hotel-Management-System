import { apiRequest } from './apiClient'

export const initiatePayment = (bookingReference) =>
  apiRequest('/api/payments/initiate', {
    method: 'POST',
    body: JSON.stringify({ bookingReference }),
  })

export const submitPayment = (paymentReference, transactionId) =>
  apiRequest('/api/payments/submit', {
    method: 'POST',
    body: JSON.stringify({ paymentReference, transactionId }),
  })

export const getPaymentForBooking = (bookingReference) =>
  apiRequest('/api/payments/booking', {
    method: 'POST',
    body: JSON.stringify({ bookingReference }),
  })

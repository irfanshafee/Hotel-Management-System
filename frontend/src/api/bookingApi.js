import { apiRequest } from './apiClient'

export const createBooking = (booking) =>
  apiRequest('/api/bookings', {
    method: 'POST',
    body: JSON.stringify(booking),
  })

export const getMyBookings = () => apiRequest('/api/bookings/my')

export const getBooking = (bookingId) => apiRequest(`/api/bookings/${bookingId}`)

export const cancelBooking = (bookingId) =>
  apiRequest(`/api/bookings/${bookingId}/cancel`, { method: 'PATCH' })

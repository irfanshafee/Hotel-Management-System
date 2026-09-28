import { apiRequest } from './apiClient'

export const createBooking = (booking) =>
  apiRequest('/api/bookings', {
    method: 'POST',
    body: JSON.stringify(booking),
  })

export const getMyBookings = () => apiRequest('/api/bookings/my')

export const getBooking = (bookingReference) =>
  apiRequest('/api/bookings/details', {
    method: 'POST',
    body: JSON.stringify({ bookingReference }),
  })

export const cancelBooking = (bookingReference) =>
  apiRequest('/api/bookings/cancel', {
    method: 'PATCH',
    body: JSON.stringify({ bookingReference }),
  })

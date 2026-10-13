import { transportApiRequest } from './apiClient'

export const searchTrips = ({ origin, destination, date }) => {
  const params = new URLSearchParams({ origin, destination, date })
  return transportApiRequest(`/api/trips?${params}`)
}

export const getTripSeats = (tripId) =>
  transportApiRequest(`/api/trips/${tripId}/seats`)

export const createTransportBooking = (tripId, seatIds) =>
  transportApiRequest('/api/bookings', {
    method: 'POST',
    body: JSON.stringify({ tripId, seatIds }),
  })

export const getMyTransportBookings = () =>
  transportApiRequest('/api/bookings/my')

export const initiateTransportPayment = (bookingId) =>
  transportApiRequest(`/api/payments/initiate/${bookingId}`, { method: 'POST' })

export const getTransportPayment = (bookingId) =>
  transportApiRequest(`/api/payments/booking/${bookingId}`)

export const submitTransportPayment = (paymentId, transactionId) =>
  transportApiRequest(`/api/payments/${paymentId}/submit`, {
    method: 'POST',
    body: JSON.stringify({ transactionId }),
  })

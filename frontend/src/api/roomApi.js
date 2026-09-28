import { apiRequest } from './apiClient'

function appendRoomFilters(params, filters, keys) {
  keys.forEach((key) => {
    if (filters[key] !== undefined && filters[key] !== '') {
      params.set(key, filters[key])
    }
  })
}

export const getRoomsByHotel = (hotelId, filters) => {
  const params = new URLSearchParams()
  appendRoomFilters(params, filters, ['capacity', 'category', 'minPrice', 'maxPrice'])
  const query = params.toString()
  return apiRequest(`/api/hotels/${hotelId}/rooms${query ? `?${query}` : ''}`)
}

export const getAvailableRooms = (hotelId, filters) => {
  const params = new URLSearchParams()
  const supportedFilters = [
    'checkIn',
    'checkOut',
    'capacity',
    'category',
    'minPrice',
    'maxPrice',
  ]

  appendRoomFilters(params, filters, supportedFilters)

  return apiRequest(`/api/hotels/${hotelId}/rooms/available?${params.toString()}`)
}

import { apiRequest } from './apiClient'

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

  supportedFilters.forEach((key) => {
    if (filters[key] !== undefined && filters[key] !== '') {
      params.set(key, filters[key])
    }
  })

  return apiRequest(`/api/hotels/${hotelId}/rooms/available?${params.toString()}`)
}

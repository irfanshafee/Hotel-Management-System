import { apiRequest } from './apiClient'

export const getHotels = () => apiRequest('/api/hotels')

export const getHotel = (hotelId) => apiRequest(`/api/hotels/${hotelId}`)

export const searchHotels = ({ city, name } = {}) => {
  const params = new URLSearchParams()
  if (city) params.set('city', city)
  if (name) params.set('name', name)
  return apiRequest(`/api/hotels/search?${params.toString()}`)
}

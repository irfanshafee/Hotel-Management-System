import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { createBooking } from '../api/bookingApi'
import { getHotel } from '../api/hotelApi'
import { getAvailableRooms } from '../api/roomApi'
import RoomCard from '../components/RoomCard'
import { useAuth } from '../context/AuthContext'
import { differenceInNights, formatDate, todayString } from '../utils/formatters'

function validDates(checkIn, checkOut) {
  return checkIn && checkOut && checkIn >= todayString() && checkIn < checkOut
}

export default function HotelDetailsPage() {
  const { hotelId } = useParams()
  const [searchParams] = useSearchParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const [hotel, setHotel] = useState(null)
  const [rooms, setRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [bookingRoomId, setBookingRoomId] = useState(null)

  const filters = useMemo(() => ({
    checkIn: searchParams.get('checkIn') || '',
    checkOut: searchParams.get('checkOut') || '',
    capacity: searchParams.get('capacity') || '',
    category: searchParams.get('category') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
  }), [searchParams])

  const datesAreValid = validDates(filters.checkIn, filters.checkOut)
  const nights = differenceInNights(filters.checkIn, filters.checkOut)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')

    const requests = [getHotel(hotelId)]
    if (datesAreValid) requests.push(getAvailableRooms(hotelId, filters))

    Promise.all(requests)
      .then(([hotelData, roomData = []]) => {
        if (!active) return
        setHotel(hotelData)
        setRooms(roomData)
      })
      .catch((requestError) => {
        if (active) setError(requestError.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [hotelId, datesAreValid, filters])

  const handleBook = async (room) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: location.pathname + location.search } })
      return
    }

    setBookingRoomId(room.id)
    setError('')
    try {
      const booking = await createBooking({
        roomId: room.id,
        checkIn: filters.checkIn,
        checkOut: filters.checkOut,
      })
      navigate(`/checkout/${booking.bookingId}`)
    } catch (bookingError) {
      setError(bookingError.message)
    } finally {
      setBookingRoomId(null)
    }
  }

  if (loading) return <div className="page-state">Loading available rooms…</div>

  return (
    <div className="page-width section-space">
      <Link className="back-link" to={`/?city=${encodeURIComponent(hotel?.city || '')}`}>← Back to hotels</Link>

      {hotel && (
        <section className="hotel-detail-hero">
          <div>
            <span className="eyebrow eyebrow--light">{hotel.city}</span>
            <h1>{hotel.name}</h1>
            <p>{hotel.address || `${hotel.city}, Bangladesh`}</p>
          </div>
          <div className="stay-summary">
            <span>{formatDate(filters.checkIn)} → {formatDate(filters.checkOut)}</span>
            <strong>{nights || '—'} {nights === 1 ? 'night' : 'nights'}</strong>
          </div>
        </section>
      )}

      {hotel?.description && <p className="hotel-intro">{hotel.description}</p>}
      {error && <div className="form-alert">{error}</div>}

      {!datesAreValid ? (
        <div className="empty-card">
          <h2>Choose valid stay dates first</h2>
          <p>Check-in and check-out are required, and check-out must be after check-in.</p>
          <Link className="button button--primary" to="/">Update search</Link>
        </div>
      ) : (
        <section className="rooms-section">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Live availability</span>
              <h2>Available rooms</h2>
            </div>
            <span className="result-count">{rooms.length} rooms</span>
          </div>

          {rooms.length === 0 ? (
            <div className="page-state">No available rooms match your dates and filters.</div>
          ) : (
            <div className="room-list">
              {rooms.map((room) => (
                <RoomCard
                  key={room.id}
                  room={room}
                  checkIn={filters.checkIn}
                  checkOut={filters.checkOut}
                  booking={bookingRoomId === room.id}
                  onBook={handleBook}
                />
              ))}
            </div>
          )}
        </section>
      )}
    </div>
  )
}

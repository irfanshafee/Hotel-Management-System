import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { createBooking } from '../api/bookingApi'
import { getHotel } from '../api/hotelApi'
import { getAvailableRooms, getRoomsByHotel } from '../api/roomApi'
import RoomCard from '../components/RoomCard'
import { useAuth } from '../context/AuthContext'
import {
  differenceInNights,
  formatDate,
  nextDateString,
  todayString,
} from '../utils/formatters'

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
  const [datePromptRoom, setDatePromptRoom] = useState(null)
  const [bookingDates, setBookingDates] = useState({ checkIn: '', checkOut: '' })
  const [dateError, setDateError] = useState('')

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

    const roomRequest = datesAreValid
      ? getAvailableRooms(hotelId, filters)
      : getRoomsByHotel(hotelId, filters)
    const requests = [getHotel(hotelId), roomRequest]

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

  const bookRoom = async (room, dates) => {
    if (!isAuthenticated) {
      const returnParams = new URLSearchParams(searchParams)
      returnParams.set('checkIn', dates.checkIn)
      returnParams.set('checkOut', dates.checkOut)
      navigate('/login', {
        state: { from: `${location.pathname}?${returnParams.toString()}` },
      })
      return
    }

    setBookingRoomId(room.id)
    setError('')
    try {
      const booking = await createBooking({
        roomId: room.id,
        checkIn: dates.checkIn,
        checkOut: dates.checkOut,
      })
      navigate(`/checkout/${booking.bookingId}`)
    } catch (bookingError) {
      setError(bookingError.message)
    } finally {
      setBookingRoomId(null)
    }
  }

  const handleBook = (room) => {
    if (datesAreValid) {
      bookRoom(room, { checkIn: filters.checkIn, checkOut: filters.checkOut })
      return
    }
    setDatePromptRoom(room)
    setBookingDates({
      checkIn: filters.checkIn || '',
      checkOut: filters.checkOut || '',
    })
    setDateError('')
  }

  const handleDateBooking = (event) => {
    event.preventDefault()
    if (!bookingDates.checkIn || !bookingDates.checkOut) {
      setDateError('Choose both check-in and check-out dates.')
      return
    }
    if (!validDates(bookingDates.checkIn, bookingDates.checkOut)) {
      setDateError('Dates cannot be in the past, and check-out must be after check-in.')
      return
    }
    const selectedRoom = datePromptRoom
    setDatePromptRoom(null)
    bookRoom(selectedRoom, bookingDates)
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
            {datesAreValid ? (
              <>
                <span>{formatDate(filters.checkIn)} → {formatDate(filters.checkOut)}</span>
                <strong>{nights} {nights === 1 ? 'night' : 'nights'}</strong>
              </>
            ) : (
              <>
                <span>Flexible dates</span>
                <strong>Browse all rooms</strong>
              </>
            )}
          </div>
        </section>
      )}

      {hotel?.description && <p className="hotel-intro">{hotel.description}</p>}
      {error && <div className="form-alert">{error}</div>}

      {!datesAreValid && (filters.checkIn || filters.checkOut) && (
        <div className="browse-notice">
          Your date selection is incomplete or invalid. Rooms are shown for browsing only;
          you can choose valid dates when booking.
        </div>
      )}

      <section className="rooms-section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">{datesAreValid ? 'Live availability' : 'Room collection'}</span>
            <h2>{datesAreValid ? 'Available rooms' : 'Browse rooms'}</h2>
          </div>
          <span className="result-count">{rooms.length} rooms</span>
        </div>

        {rooms.length === 0 ? (
          <div className="page-state">
            {datesAreValid
              ? 'No available rooms match your dates and filters.'
              : 'No rooms match the selected filters.'}
          </div>
        ) : (
          <div className="room-list">
            {rooms.map((room) => (
              <RoomCard
                key={room.id}
                room={room}
                checkIn={datesAreValid ? filters.checkIn : ''}
                checkOut={datesAreValid ? filters.checkOut : ''}
                booking={bookingRoomId === room.id}
                onBook={handleBook}
              />
            ))}
          </div>
        )}
      </section>

      {datePromptRoom && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setDatePromptRoom(null)}>
          <section
            aria-labelledby="booking-dates-title"
            aria-modal="true"
            className="date-dialog"
            role="dialog"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <button className="date-dialog__close" type="button" aria-label="Close" onClick={() => setDatePromptRoom(null)}>×</button>
            <span className="eyebrow">Room {datePromptRoom.roomNumber}</span>
            <h2 id="booking-dates-title">When would you like to stay?</h2>
            <p>Choose your dates to check and create the booking.</p>
            {dateError && <div className="form-alert">{dateError}</div>}
            <form className="form-stack" onSubmit={handleDateBooking}>
              <div className="date-dialog__fields">
                <label>
                  <span>Check-in</span>
                  <input
                    type="date"
                    min={todayString()}
                    value={bookingDates.checkIn}
                    onChange={(event) => {
                      setBookingDates((current) => ({ ...current, checkIn: event.target.value }))
                      setDateError('')
                    }}
                  />
                </label>
                <label>
                  <span>Check-out</span>
                  <input
                    type="date"
                    min={nextDateString(bookingDates.checkIn)}
                    value={bookingDates.checkOut}
                    onChange={(event) => {
                      setBookingDates((current) => ({ ...current, checkOut: event.target.value }))
                      setDateError('')
                    }}
                  />
                </label>
              </div>
              <button className="button button--primary button--block" type="submit">
                Continue to Booking
              </button>
            </form>
          </section>
        </div>
      )}
    </div>
  )
}

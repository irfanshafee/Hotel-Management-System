import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { cancelBooking, getMyBookings } from '../api/bookingApi'
import StatusBadge from '../components/StatusBadge'
import { formatDate, formatMoney } from '../utils/formatters'

export default function MyBookingsPage() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [cancelling, setCancelling] = useState(null)

  useEffect(() => {
    getMyBookings()
      .then(setBookings)
      .catch((requestError) => setError(requestError.message))
      .finally(() => setLoading(false))
  }, [])

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Cancel this booking? This action cannot be undone.')) return
    setCancelling(bookingId)
    setError('')
    try {
      const updated = await cancelBooking(bookingId)
      setBookings((current) => current.map((booking) => (
        booking.bookingId === bookingId ? updated : booking
      )))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setCancelling(null)
    }
  }

  return (
    <div className="page-width section-space bookings-page">
      <div className="section-heading section-heading--large">
        <div>
          <span className="eyebrow">Your trips</span>
          <h1>My Bookings</h1>
          <p>Review upcoming and past booking activity.</p>
        </div>
        <Link className="button button--secondary" to="/">Find another stay</Link>
      </div>

      {error && <div className="form-alert">{error}</div>}
      {loading && <div className="page-state">Loading your bookings…</div>}
      {!loading && bookings.length === 0 && (
        <div className="empty-card">
          <h2>No bookings yet</h2>
          <p>Your booked rooms will appear here.</p>
          <Link className="button button--primary" to="/">Explore hotels</Link>
        </div>
      )}

      {!loading && bookings.length > 0 && (
        <div className="booking-list">
          {bookings.map((booking) => {
            const cancellable = ['PENDING', 'CONFIRMED'].includes(booking.status)
            return (
              <article className="booking-card" key={booking.bookingId}>
                <div className="booking-card__identity">
                  <span className="booking-number">Booking #{booking.bookingId}</span>
                  <StatusBadge status={booking.status} />
                  <h2>{booking.hotelName}</h2>
                  <p>{booking.city} · Room {booking.roomNumber}</p>
                </div>
                <div className="booking-card__facts">
                  <div><span>Check-in</span><strong>{formatDate(booking.checkIn)}</strong></div>
                  <div><span>Check-out</span><strong>{formatDate(booking.checkOut)}</strong></div>
                  <div><span>Room</span><strong>{booking.category}</strong></div>
                  <div><span>Nightly price</span><strong>{formatMoney(booking.price)}</strong></div>
                </div>
                <div className="booking-card__actions">
                  {booking.status === 'PENDING' && (
                    <Link className="button button--primary" to={`/checkout/${booking.bookingId}`}>
                      Continue to Payment
                    </Link>
                  )}
                  {cancellable && (
                    <button
                      className="button button--danger-ghost"
                      disabled={cancelling === booking.bookingId}
                      onClick={() => handleCancel(booking.bookingId)}
                      type="button"
                    >
                      {cancelling === booking.bookingId ? 'Cancelling…' : 'Cancel Booking'}
                    </button>
                  )}
                </div>
              </article>
            )
          })}
        </div>
      )}
    </div>
  )
}

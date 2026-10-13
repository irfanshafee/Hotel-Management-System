import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyTransportBookings } from '../api/transportApi'
import StatusBadge from '../components/StatusBadge'
import { formatMoney } from '../utils/formatters'

export default function TransportBookingsPage() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => { getMyTransportBookings().then(setBookings).catch((e) => setError(e.message)).finally(() => setLoading(false)) }, [])
  return <div className="transport-theme transport-page"><div className="page-width section-space">
    <div className="section-heading section-heading--large"><div><span className="eyebrow">Your journeys</span><h1>Transport Bookings</h1><p>Review tickets and finish pending payments.</p></div><Link className="button button--secondary" to="/transport">Find a trip</Link></div>
    {error && <div className="form-alert">{error}</div>}{loading && <div className="page-state">Loading transport bookings…</div>}
    {!loading && !bookings.length && <div className="empty-card"><h2>No transport bookings yet</h2><Link className="button button--primary" to="/transport">Explore transport</Link></div>}
    <div className="booking-list">{bookings.map((booking) => <article className="booking-card transport-booking-card" key={booking.bookingReference}>
      <div className="booking-card__identity"><small className="booking-number">{booking.bookingReference}</small><StatusBadge status={booking.status}/><h2>{booking.origin} → {booking.destination}</h2><p>{booking.busNumber}</p></div>
      <div className="booking-card__facts"><div><span>Departure</span><strong>{new Date(booking.departureTime).toLocaleString()}</strong></div><div><span>Seats</span><strong>{booking.seats.map((s) => s.seatNumber).join(', ')}</strong></div><div><span>Total</span><strong>{formatMoney(booking.totalAmount)}</strong></div></div>
      <div className="booking-card__actions">{booking.status === 'PENDING' && <Link className="button button--primary" to={`/transport/checkout/${booking.bookingReference}`}>Complete payment</Link>}<Link className="button button--secondary" to="/transport">Book another</Link></div>
    </article>)}</div>
  </div></div>
}

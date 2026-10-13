import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createTransportBooking, getTripSeats, searchTrips } from '../api/transportApi'
import { useAuth } from '../context/AuthContext'
import { formatMoney, nextDateString, todayString } from '../utils/formatters'

const cities = [
  { label: 'Dhaka', value: 'Dhaka' },
  { label: 'Chittagong', value: 'Chattogram' },
  { label: 'Sylhet', value: 'Sylhet' },
  { label: 'Rajshahi', value: 'Rajshahi' },
]

export default function TransportPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated } = useAuth()
  const [search, setSearch] = useState({ origin: 'Dhaka', destination: 'Chattogram', date: nextDateString(todayString()) })
  const [trips, setTrips] = useState([])
  const [selectedTrip, setSelectedTrip] = useState(null)
  const [seats, setSeats] = useState([])
  const [selectedSeats, setSelectedSeats] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const findTrips = async (event) => {
    event.preventDefault(); setLoading(true); setError(''); setSelectedTrip(null)
    try { setTrips(await searchTrips(search)) } catch (err) { setError(err.message) } finally { setLoading(false) }
  }

  const chooseTrip = async (trip) => {
    setError(''); setSelectedTrip(trip); setSelectedSeats([])
    try { const result = await getTripSeats(trip.tripId); setSeats(result.seats) }
    catch (err) { setError(err.message) }
  }

  const toggleSeat = (seatId) => setSelectedSeats((current) =>
    current.includes(seatId) ? current.filter((id) => id !== seatId) : [...current, seatId])

  const book = async () => {
    if (!isAuthenticated) { navigate('/login', { state: { from: location.pathname } }); return }
    if (!selectedSeats.length) { setError('Choose at least one available seat.'); return }
    setLoading(true); setError('')
    try {
      const booking = await createTransportBooking(selectedTrip.tripId, selectedSeats)
      navigate(`/transport/checkout/${booking.bookingReference}`)
    } catch (err) { setError(err.message) } finally { setLoading(false) }
  }

  return <div className="transport-theme">
    <section className="transport-hero"><div className="page-width">
      <span className="eyebrow eyebrow--light">Travel across Bangladesh</span>
      <h1>Your next journey starts here.</h1>
      <p>Search scheduled buses, choose your seats, and pay securely.</p>
      <form className="transport-search" onSubmit={findTrips}>
        <label><span>From</span><select value={search.origin} onChange={(e) => setSearch({...search, origin:e.target.value})} required>{cities.map((city) => <option key={city.value} value={city.value} disabled={city.value === search.destination}>{city.label}</option>)}</select></label>
        <label><span>To</span><select value={search.destination} onChange={(e) => setSearch({...search, destination:e.target.value})} required>{cities.map((city) => <option key={city.value} value={city.value} disabled={city.value === search.origin}>{city.label}</option>)}</select></label>
        <label><span>Date</span><input type="date" min={todayString()} value={search.date} onChange={(e) => setSearch({...search, date:e.target.value})} required /></label>
        <button className="button button--primary" disabled={loading}>{loading ? 'Searching…' : 'Search buses'}</button>
      </form>
    </div></section>
    <section className="page-width section-space">
      <div className="section-heading section-heading--large"><div><span className="eyebrow">Available journeys</span><h1>Choose your trip</h1><p>Clear schedules, fares, and live seat availability.</p></div><button className="button button--secondary" onClick={() => navigate('/transport/bookings')}>My transport bookings</button></div>
      {error && <div className="form-alert">{error}</div>}
      {!loading && !trips.length && <div className="empty-card"><h2>Where are you heading?</h2><p>Use the search above to find scheduled transport.</p></div>}
      <div className="transport-trip-list">{trips.map((trip) => <article className="transport-trip-card" key={trip.tripId}>
        <div className="transport-route"><span>{trip.origin}</span><i>→</i><span>{trip.destination}</span></div>
        <div><small>Departure</small><strong>{new Date(trip.departureTime).toLocaleString()}</strong></div>
        <div><small>Coach</small><strong>{trip.vehicleName}</strong><span>{trip.busNumber}</span></div>
        <div className="transport-fare"><strong>{formatMoney(trip.fare)}</strong><span>per seat</span></div>
        <button className="button button--primary" onClick={() => chooseTrip(trip)}>Select seats</button>
      </article>)}</div>
    </section>
    {selectedTrip && <div className="modal-backdrop" onMouseDown={() => setSelectedTrip(null)}><section className="date-dialog transport-seat-dialog" onMouseDown={(e) => e.stopPropagation()}>
      <button className="date-dialog__close" onClick={() => setSelectedTrip(null)}>×</button><span className="eyebrow">{selectedTrip.origin} → {selectedTrip.destination}</span><h2>Choose your seats</h2>
      <div className="seat-legend"><span><i className="seat-dot available"/>Available</span><span><i className="seat-dot selected"/>Selected</span><span><i className="seat-dot occupied"/>Occupied</span></div>
      <div className="bus-shell"><div className="driver-wheel">◉ Driver</div><div className="seat-grid">{seats.map((seat) => <button key={seat.seatId} disabled={!seat.available} className={`seat ${selectedSeats.includes(seat.seatId) ? 'is-selected' : ''}`} onClick={() => toggleSeat(seat.seatId)}>{seat.seatNumber}</button>)}</div></div>
      <div className="seat-total"><span>{selectedSeats.length} seat(s)</span><strong>{formatMoney(Number(selectedTrip.fare) * selectedSeats.length)}</strong></div>
      <button className="button button--primary button--block" onClick={book} disabled={loading || !selectedSeats.length}>Continue to payment</button>
    </section></div>}
  </div>
}

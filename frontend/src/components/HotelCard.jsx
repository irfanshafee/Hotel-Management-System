import { Link } from 'react-router-dom'

export default function HotelCard({ hotel, search }) {
  return (
    <article className="hotel-card">
      <div className="hotel-card__visual" aria-hidden="true">
        <span className="hotel-card__visual-mark">SF</span>
        <div className="hotel-card__skyline">
          <i /><i /><i /><i />
        </div>
      </div>
      <div className="hotel-card__body">
        <div className="hotel-card__topline">
          <span className="location-pill">{hotel.city}</span>
          <span className="muted">Hotel stay</span>
        </div>
        <h3>{hotel.name}</h3>
        <p className="hotel-card__address">{hotel.address || `${hotel.city}, Bangladesh`}</p>
        <p className="hotel-card__description">
          {hotel.description || 'A comfortable place to stay while you explore the city.'}
        </p>
        <div className="hotel-card__actions">
          <span className="muted">Check live room availability</span>
          <Link className="button button--secondary" to={`/hotels/${hotel.id}?${search}`}>
            View Rooms
          </Link>
        </div>
      </div>
    </article>
  )
}

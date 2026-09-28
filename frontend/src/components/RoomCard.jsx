import { differenceInNights, formatMoney } from '../utils/formatters'

export default function RoomCard({ room, checkIn, checkOut, booking, onBook }) {
  const nights = differenceInNights(checkIn, checkOut)
  const total = Number(room.price) * nights

  return (
    <article className="room-card">
      <div className="room-card__icon" aria-hidden="true">{room.roomNumber}</div>
      <div className="room-card__details">
        <span className="eyebrow">Room {room.roomNumber}</span>
        <h3>{room.category.charAt(0) + room.category.slice(1).toLowerCase()} Room</h3>
        <div className="room-facts">
          <span>Up to {room.capacity} guests</span>
          <span>{nights} {nights === 1 ? 'night' : 'nights'}</span>
        </div>
      </div>
      <div className="room-card__price">
        <strong>{formatMoney(room.price)}</strong>
        <span>per night</span>
        <b>Total: {formatMoney(total)}</b>
        <button className="button button--primary" disabled={booking} onClick={() => onBook(room)} type="button">
          {booking ? 'Booking…' : 'Book Room'}
        </button>
      </div>
    </article>
  )
}

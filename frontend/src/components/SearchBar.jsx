import { nextDateString, todayString } from '../utils/formatters'

export default function SearchBar({ filters, cities, errors, onChange, onSubmit, busy }) {
  return (
    <form className="search-bar" onSubmit={onSubmit} noValidate>
      <label className="search-field">
        <span>City</span>
        <select value={filters.city} onChange={(event) => onChange('city', event.target.value)}>
          <option value="">Where are you going?</option>
          {cities.map((city) => <option key={city} value={city}>{city}</option>)}
        </select>
        {errors.city && <small className="field-error">{errors.city}</small>}
      </label>

      <label className="search-field">
        <span>Check-in</span>
        <input
          type="date"
          min={todayString()}
          value={filters.checkIn}
          onChange={(event) => onChange('checkIn', event.target.value)}
        />
        {errors.checkIn && <small className="field-error">{errors.checkIn}</small>}
      </label>

      <label className="search-field">
        <span>Check-out</span>
        <input
          type="date"
          min={nextDateString(filters.checkIn)}
          value={filters.checkOut}
          onChange={(event) => onChange('checkOut', event.target.value)}
        />
        {errors.checkOut && <small className="field-error">{errors.checkOut}</small>}
      </label>

      <label className="search-field search-field--compact">
        <span>Guests</span>
        <select
          value={filters.capacity}
          onChange={(event) => onChange('capacity', event.target.value)}
        >
          <option value="">Any</option>
          {[2, 3, 4].map((capacity) => (
            <option key={capacity} value={capacity}>{capacity} guests</option>
          ))}
        </select>
      </label>

      <button className="button button--primary search-bar__button" disabled={busy} type="submit">
        {busy ? 'Searching…' : 'Search'}
      </button>
    </form>
  )
}

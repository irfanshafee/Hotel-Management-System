export default function FilterSidebar({ filters, cities, onChange, onApply, busy }) {
  return (
    <aside className="filter-card">
      <div className="filter-card__heading">
        <div>
          <span className="eyebrow">Refine results</span>
          <h2>Filters</h2>
        </div>
      </div>

      <label className="filter-group">
        <span className="filter-group__label">City</span>
        <select value={filters.city} onChange={(event) => onChange('city', event.target.value)}>
          <option value="">Select a city</option>
          {cities.map((city) => <option key={city} value={city}>{city}</option>)}
        </select>
      </label>

      <fieldset className="filter-group">
        <legend>Price range per night</legend>
        <div className="price-inputs">
          <label>
            <span>Minimum</span>
            <input
              type="number"
              min="0"
              placeholder="৳0"
              value={filters.minPrice}
              onChange={(event) => onChange('minPrice', event.target.value)}
            />
          </label>
          <label>
            <span>Maximum</span>
            <input
              type="number"
              min="0"
              placeholder="Any"
              value={filters.maxPrice}
              onChange={(event) => onChange('maxPrice', event.target.value)}
            />
          </label>
        </div>
      </fieldset>

      <fieldset className="filter-group">
        <legend>Room category</legend>
        <label className="choice-row">
          <input
            type="radio"
            name="category"
            checked={filters.category === ''}
            onChange={() => onChange('category', '')}
          /> Any category
        </label>
        {['NORMAL', 'DELUXE'].map((category) => (
          <label className="choice-row" key={category}>
            <input
              type="radio"
              name="category"
              checked={filters.category === category}
              onChange={() => onChange('category', category)}
            /> {category.charAt(0) + category.slice(1).toLowerCase()}
          </label>
        ))}
      </fieldset>

      <fieldset className="filter-group">
        <legend>Capacity</legend>
        <div className="capacity-options">
          {['', '2', '3', '4'].map((capacity) => (
            <button
              className={filters.capacity === capacity ? 'selected' : ''}
              key={capacity || 'any'}
              type="button"
              onClick={() => onChange('capacity', capacity)}
            >
              {capacity || 'Any'}
            </button>
          ))}
        </div>
      </fieldset>

      <button className="button button--secondary button--block" disabled={busy} onClick={onApply} type="button">
        Apply Filters
      </button>
    </aside>
  )
}

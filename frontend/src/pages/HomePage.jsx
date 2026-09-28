import { useEffect, useMemo, useState } from 'react'
import FilterSidebar from '../components/FilterSidebar'
import HotelCard from '../components/HotelCard'
import SearchBar from '../components/SearchBar'
import { getHotels, searchHotels } from '../api/hotelApi'
import { buildSearchParams, todayString } from '../utils/formatters'

const initialFilters = {
  city: '',
  checkIn: '',
  checkOut: '',
  capacity: '',
  category: '',
  minPrice: '',
  maxPrice: '',
}

function validate(filters) {
  const errors = {}
  if (filters.checkIn && !filters.checkOut) {
    errors.checkOut = 'Select a check-out date or leave both dates empty.'
  }
  if (!filters.checkIn && filters.checkOut) {
    errors.checkIn = 'Select a check-in date or leave both dates empty.'
  }
  if (filters.checkIn && filters.checkIn < todayString()) {
    errors.checkIn = 'Check-in cannot be in the past.'
  }
  if (filters.checkOut && filters.checkOut < todayString()) {
    errors.checkOut = 'Check-out cannot be in the past.'
  }
  if (filters.checkIn && filters.checkOut && filters.checkIn >= filters.checkOut) {
    errors.checkOut = 'Check-out must be after check-in.'
  }
  if (
    filters.minPrice && filters.maxPrice
    && Number(filters.minPrice) > Number(filters.maxPrice)
  ) {
    errors.form = 'Minimum price cannot be greater than maximum price.'
  }
  return errors
}

export default function HomePage() {
  const [allHotels, setAllHotels] = useState([])
  const [hotels, setHotels] = useState([])
  const [filters, setFilters] = useState(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState(initialFilters)
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(true)
  const [searching, setSearching] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [hasSearched, setHasSearched] = useState(false)

  useEffect(() => {
    getHotels()
      .then((data) => {
        setAllHotels(data)
        setHotels(data)
      })
      .catch((error) => setLoadError(error.message))
      .finally(() => setLoading(false))
  }, [])

  const cities = useMemo(
    () => [...new Set(allHotels.map((hotel) => hotel.city).filter(Boolean))].sort(),
    [allHotels],
  )

  const updateFilter = (name, value) => {
    setFilters((current) => ({ ...current, [name]: value }))
    setErrors((current) => ({ ...current, [name]: '', form: '' }))
  }

  const performSearch = async (event) => {
    event?.preventDefault()
    const validationErrors = validate(filters)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length) return

    setSearching(true)
    setLoadError('')
    try {
      const results = filters.city
        ? await searchHotels({ city: filters.city })
        : allHotels
      setHotels(results)
      setAppliedFilters({ ...filters })
      setHasSearched(true)
    } catch (error) {
      setLoadError(error.message)
    } finally {
      setSearching(false)
    }
  }

  const query = buildSearchParams(appliedFilters)

  return (
    <>
      <section className="hero">
        <div className="page-width">
          <div className="hero__copy">
            <span className="eyebrow eyebrow--light">Find your next stay</span>
            <h1>Comfortable rooms, clear prices, easy booking.</h1>
            <p>Browse hotels freely, then add dates whenever you are ready to check availability or book.</p>
          </div>
          <SearchBar
            filters={filters}
            cities={cities}
            errors={errors}
            onChange={updateFilter}
            onSubmit={performSearch}
            busy={searching}
          />
          {errors.form && <p className="form-alert form-alert--hero">{errors.form}</p>}
        </div>
      </section>

      <section className="page-width search-layout section-space">
        <FilterSidebar
          filters={filters}
          cities={cities}
          onChange={updateFilter}
          onApply={performSearch}
          busy={searching}
        />

        <div className="results-panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">Places to stay</span>
              <h2>
                {hasSearched && appliedFilters.city
                  ? `Hotels in ${appliedFilters.city}`
                  : 'Explore our hotels'}
              </h2>
            </div>
            {!loading && !loadError && <span className="result-count">{hotels.length} results</span>}
          </div>

          {loading && <div className="page-state">Loading hotels…</div>}
          {loadError && <div className="page-state page-state--error">{loadError}</div>}
          {!loading && !loadError && hotels.length === 0 && (
            <div className="page-state">No hotels found for that city.</div>
          )}
          {!loading && !loadError && hotels.length > 0 && (
            <div className="hotel-list">
              {hotels.map((hotel) => (
                <HotelCard key={hotel.id} hotel={hotel} search={query} />
              ))}
            </div>
          )}
        </div>
      </section>
    </>
  )
}

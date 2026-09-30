import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/apiClient'
import { getBooking } from '../api/bookingApi'
import {
  getPaymentForBooking,
  initiatePayment,
  submitPayment,
} from '../api/paymentApi'
import StatusBadge from '../components/StatusBadge'
import { differenceInNights, formatDate, formatMoney } from '../utils/formatters'

export default function CheckoutPage() {
  const { bookingId } = useParams()
  const [booking, setBooking] = useState(null)
  const [payment, setPayment] = useState(null)
  const [transactionId, setTransactionId] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [resultMessage, setResultMessage] = useState('')

  useEffect(() => {
    let active = true

    async function loadCheckout() {
      setLoading(true)
      setError('')
      try {
        const bookingData = await getBooking(bookingId)
        if (!active) return
        setBooking(bookingData)

        let paymentData
        try {
          paymentData = await getPaymentForBooking(bookingId)
        } catch (requestError) {
          if (!(requestError instanceof ApiError) || requestError.status !== 404) throw requestError
          if (bookingData.status !== 'PENDING') throw requestError
          paymentData = await initiatePayment(bookingId)
        }
        if (active) setPayment(paymentData)
      } catch (requestError) {
        if (active) setError(requestError.message)
      } finally {
        if (active) setLoading(false)
      }
    }

    loadCheckout()
    return () => {
      active = false
    }
  }, [bookingId])

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setResultMessage('')
    try {
      const updatedPayment = await submitPayment(payment.paymentId, transactionId)
      setPayment(updatedPayment)
      setBooking((current) => ({ ...current, status: updatedPayment.bookingStatus }))
      setResultMessage('Payment successful. Booking confirmed.')
    } catch (requestError) {
      if (requestError.status === 400 && requestError.data) {
        setPayment(requestError.data)
        setBooking((current) => ({ ...current, status: requestError.data.bookingStatus }))
        setResultMessage(requestError.message)
      } else {
        setError(requestError.message)
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="page-state">Preparing your checkout…</div>

  if (error && !booking) {
    return (
      <div className="page-width section-space">
        <div className="page-state page-state--error">{error}</div>
      </div>
    )
  }

  const nights = differenceInNights(booking?.checkIn, booking?.checkOut)
  const paymentSuccessful = payment?.paymentStatus === 'PAID'
  const paymentFailed = payment?.paymentStatus === 'FAILED'
  const isFinal = paymentSuccessful || paymentFailed

  return (
    <div className="checkout-page page-width section-space">
      <div className="section-heading section-heading--large">
        <div>
          <span className="eyebrow">Secure checkout</span>
          <h1>{isFinal ? 'Payment result' : 'Complete your booking'}</h1>
          <p>Booking ID: {booking.bookingId}</p>
        </div>
        <StatusBadge status={payment?.bookingStatus || booking.status} />
      </div>

      {error && <div className="form-alert">{error}</div>}

      <div className="checkout-grid">
        <section className="checkout-card stay-card">
          <span className="eyebrow">Stay details</span>
          <h2>{booking.hotelName}</h2>
          <p>{booking.city} · Room {booking.roomNumber}</p>
          <div className="checkout-details">
            <div><span>Check-in</span><strong>{formatDate(booking.checkIn)}</strong></div>
            <div><span>Check-out</span><strong>{formatDate(booking.checkOut)}</strong></div>
            <div><span>Duration</span><strong>{nights} nights</strong></div>
            <div><span>Price per night</span><strong>{formatMoney(booking.price)}</strong></div>
          </div>
          <div className="total-line">
            <span>Total payment</span>
            <strong>{formatMoney(payment?.amount || Number(booking.price) * nights)}</strong>
          </div>
        </section>

        <section className={`checkout-card payment-card ${isFinal ? 'payment-card--result' : ''}`}>
          {paymentSuccessful && (
            <div className="payment-result payment-result--success">
              <span className="result-icon">✓</span>
              <h2>Payment Successful</h2>
              <p>Booking Confirmed</p>
            </div>
          )}
          {paymentFailed && (
            <div className="payment-result payment-result--failed">
              <span className="result-icon">×</span>
              <h2>Payment Failed</h2>
              <p>Booking Cancelled</p>
            </div>
          )}

          {!isFinal && payment && (
            <>
              <span className="eyebrow">Dummy payment</span>
              <h2>Enter transaction ID</h2>
              <p className="payment-guidance">
                Required format: uppercase <strong>S</strong>, one space, then exactly eight letters or digits.
                Example: <code>S 6789ABcd</code>
              </p>
              <form className="form-stack" onSubmit={handleSubmit}>
                <label>
                  <span>Transaction ID</span>
                  <input
                    autoComplete="off"
                    placeholder="S 6789ABcd"
                    value={transactionId}
                    onChange={(event) => setTransactionId(event.target.value)}
                  />
                </label>
                <button className="button button--primary button--block" disabled={submitting} type="submit">
                  {submitting ? 'Processing…' : `Pay ${formatMoney(payment.amount)}`}
                </button>
              </form>
            </>
          )}

          {isFinal && (
            <div className="payment-receipt">
              {resultMessage && <p>{resultMessage}</p>}
              <dl>
                <div><dt>Payment ID</dt><dd>{payment.paymentId}</dd></div>
                <div><dt>Booking ID</dt><dd>{payment.bookingId}</dd></div>
                <div><dt>Transaction ID</dt><dd>{payment.transactionId || '—'}</dd></div>
                <div><dt>Amount</dt><dd>{formatMoney(payment.amount)}</dd></div>
                <div><dt>Payment status</dt><dd><StatusBadge status={payment.paymentStatus} /></dd></div>
                <div><dt>Booking status</dt><dd><StatusBadge status={payment.bookingStatus} /></dd></div>
              </dl>
              <div className="result-actions">
                <Link className="button button--primary" to="/bookings">View My Bookings</Link>
                <Link className="button button--secondary" to="/">Back to Hotels</Link>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

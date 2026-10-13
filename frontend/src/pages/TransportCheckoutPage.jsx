import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/apiClient'
import { getMyTransportBookings, getTransportPayment, initiateTransportPayment, submitTransportPayment } from '../api/transportApi'
import StatusBadge from '../components/StatusBadge'
import { formatMoney } from '../utils/formatters'

export default function TransportCheckoutPage() {
  const { bookingId } = useParams()
  const [booking, setBooking] = useState(null), [payment, setPayment] = useState(null)
  const [transactionId, setTransactionId] = useState(''), [loading, setLoading] = useState(true), [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(''), [message, setMessage] = useState('')
  useEffect(() => { let active = true; (async () => { try {
    const list = await getMyTransportBookings(); const found = list.find((item) => item.bookingReference === bookingId)
    if (!found) throw new Error('Transport booking not found.'); if (!active) return; setBooking(found)
    let pay; try { pay = await getTransportPayment(bookingId) } catch (e) { if (!(e instanceof ApiError) || e.status !== 404 || found.status !== 'PENDING') throw e; pay = await initiateTransportPayment(bookingId) }
    if (active) setPayment(pay)
  } catch (e) { if (active) setError(e.message) } finally { if (active) setLoading(false) } })(); return () => { active = false } }, [bookingId])
  const submit = async (event) => { event.preventDefault(); setSubmitting(true); setError(''); setMessage(''); try {
    const result = await submitTransportPayment(payment.paymentId, transactionId); setPayment(result); setBooking((b) => ({...b,status:result.bookingStatus})); setMessage('Payment successful. Your transport booking is confirmed.')
  } catch (e) { if (e.status === 400 && e.data) { setPayment(e.data); setBooking((b) => ({...b,status:e.data.bookingStatus})); setMessage(e.message) } else setError(e.message) } finally { setSubmitting(false) } }
  if (loading) return <div className="transport-theme transport-page"><div className="page-state">Preparing transport checkout…</div></div>
  if (error && !booking) return <div className="transport-theme transport-page"><div className="page-width section-space"><div className="page-state page-state--error">{error}</div></div></div>
  const final = payment?.paymentStatus === 'PAID' || payment?.paymentStatus === 'FAILED'
  return <div className="transport-theme transport-page"><div className="checkout-page page-width section-space">
    <div className="section-heading section-heading--large"><div><span className="eyebrow">Transport checkout</span><h1>{final ? 'Payment result' : 'Complete your ticket'}</h1><p>Booking ID: {bookingId}</p></div><StatusBadge status={payment?.bookingStatus || booking?.status}/></div>
    {error && <div className="form-alert">{error}</div>}<div className="checkout-grid"><section className="checkout-card stay-card"><span className="eyebrow">Journey details</span><h2>{booking.origin} → {booking.destination}</h2><p>{booking.busNumber}</p><div className="checkout-details"><div><span>Departure</span><strong>{new Date(booking.departureTime).toLocaleString()}</strong></div><div><span>Seats</span><strong>{booking.seats.map((s) => s.seatNumber).join(', ')}</strong></div></div><div className="total-line"><span>Total payment</span><strong>{formatMoney(booking.totalAmount)}</strong></div></section>
    <section className="checkout-card payment-card">{!final && payment && <><span className="eyebrow">Dummy payment</span><h2>Enter transaction ID</h2><p className="payment-guidance">Use uppercase <strong>S</strong>, one space, then eight letters or digits. Example: <code>S 6789ABcd</code></p><form className="form-stack" onSubmit={submit}><label><span>Transaction ID</span><input value={transactionId} onChange={(e) => setTransactionId(e.target.value)} placeholder="S 6789ABcd" required /></label><button className="button button--primary button--block" disabled={submitting}>{submitting ? 'Processing…' : `Pay ${formatMoney(payment.amount)}`}</button></form></>}
    {final && <div className={`payment-result payment-result--${payment.paymentStatus === 'PAID' ? 'success' : 'failed'}`}><span className="result-icon">{payment.paymentStatus === 'PAID' ? '✓' : '×'}</span><h2>Payment {payment.paymentStatus === 'PAID' ? 'Successful' : 'Failed'}</h2><p>{message || `Booking ${payment.bookingStatus.toLowerCase()}.`}</p><div className="payment-receipt"><dl><div><dt>Payment ID</dt><dd>{payment.paymentId}</dd></div><div><dt>Transaction</dt><dd>{payment.transactionId}</dd></div><div><dt>Amount</dt><dd>{formatMoney(payment.amount)}</dd></div></dl><div className="result-actions"><Link className="button button--primary" to="/transport/bookings">My tickets</Link><Link className="button button--secondary" to="/transport">Find trips</Link></div></div></div>}</section></div>
  </div></div>
}

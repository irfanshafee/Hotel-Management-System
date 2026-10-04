import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const destination = location.state?.from || '/'

  if (isAuthenticated) return <Navigate to={destination} replace />

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(form)
      navigate(destination, { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-card">
        <span className="eyebrow">Welcome back</span>
        <h1>Log in to your account</h1>
        <p>Manage bookings and continue to secure checkout.</p>
        {location.state?.notice && <div className="form-notice">{location.state.notice}</div>}
        {error && <div className="form-alert">{error}</div>}
        <form onSubmit={handleSubmit} className="form-stack">
          <label>
            <span>Email address</span>
            <input
              autoComplete="email"
              type="email"
              required
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
            />
          </label>
          <label>
            <span>Password</span>
            <input
              autoComplete="current-password"
              type="password"
              required
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
            />
          </label>
          <Link className="forgot-link" to="/forgot-password">Forgot password?</Link>
          <button className="button button--primary button--block" disabled={submitting} type="submit">
            {submitting ? 'Logging in…' : 'Log In'}
          </button>
        </form>
        <p className="auth-card__footer">New here? <Link to="/signup">Create an account</Link></p>
      </section>
    </div>
  )
}

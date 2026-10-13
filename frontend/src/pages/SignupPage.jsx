import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register, resendSignupOtp, verifySignup } from '../api/authApi'

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', age: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [otpOpen, setOtpOpen] = useState(false)
  const [otp, setOtp] = useState('')
  const [otpError, setOtpError] = useState('')
  const [otpNotice, setOtpNotice] = useState('')
  const [verifying, setVerifying] = useState(false)
  const [resending, setResending] = useState(false)

  const update = (name, value) => setForm((current) => ({ ...current, [name]: value }))

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await register({
        ...form,
        age: form.age ? Number(form.age) : null,
      })
      setOtp('')
      setOtpError('')
      setOtpNotice(`We sent a 6-digit verification code to ${form.email}.`)
      setOtpOpen(true)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleOtpChange = (value) => setOtp(value.replace(/\D/g, '').slice(0, 6))

  const handleVerifyOtp = async (event) => {
    event.preventDefault()
    setOtpError('')
    setOtpNotice('')
    setVerifying(true)
    try {
      await verifySignup({ email: form.email, otp })
      navigate('/login', {
        replace: true,
        state: { notice: 'Email verified. Your account has been created—please log in.' },
      })
    } catch (requestError) {
      setOtpError(requestError.message)
    } finally {
      setVerifying(false)
    }
  }

  const handleResendOtp = async () => {
    setOtpError('')
    setOtpNotice('')
    setResending(true)
    try {
      await resendSignupOtp(form.email)
      setOtpNotice('A new verification code has been sent. The earlier code no longer works.')
      setOtp('')
    } catch (requestError) {
      setOtpError(requestError.message)
    } finally {
      setResending(false)
    }
  }

  return (
    <div className="auth-page">
      <section className="auth-card">
        <span className="eyebrow">Join StayFinder</span>
        <h1>Create your account</h1>
        <p>Sign up to book rooms and keep your stays in one place.</p>
        {error && <div className="form-alert">{error}</div>}
        <form onSubmit={handleSubmit} className="form-stack">
          <label>
            <span>Full name</span>
            <input required maxLength="100" value={form.name} onChange={(event) => update('name', event.target.value)} />
          </label>
          <label>
            <span>Age <small>(optional)</small></span>
            <input type="number" min="1" value={form.age} onChange={(event) => update('age', event.target.value)} />
          </label>
          <label>
            <span>Email address</span>
            <input autoComplete="email" type="email" required value={form.email} onChange={(event) => update('email', event.target.value)} />
          </label>
          <label>
            <span>Password</span>
            <input autoComplete="new-password" type="password" minLength="8" maxLength="72" required value={form.password} onChange={(event) => update('password', event.target.value)} />
            <small>Use between 8 and 72 characters.</small>
          </label>
          <button className="button button--primary button--block" disabled={submitting} type="submit">
            {submitting ? 'Creating account…' : 'Create Account'}
          </button>
        </form>
        <p className="auth-card__footer">Already have an account? <Link to="/login">Log in</Link></p>
      </section>

      {otpOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="date-dialog otp-dialog" role="dialog" aria-modal="true" aria-labelledby="signup-otp-title">
            <span className="eyebrow">Email verification</span>
            <h2 id="signup-otp-title">Enter your verification code</h2>
            <p>Enter the six-digit code sent to <strong>{form.email}</strong>. Your account will be created only after the code is verified.</p>
            {otpNotice && <div className="form-notice">{otpNotice}</div>}
            {otpError && <div className="form-alert">{otpError}</div>}
            <form onSubmit={handleVerifyOtp} className="form-stack">
              <label>
                <span>Verification code</span>
                <input
                  className="otp-dialog__input"
                  autoComplete="one-time-code"
                  inputMode="numeric"
                  maxLength="6"
                  pattern="[0-9]{6}"
                  placeholder="123456"
                  required
                  value={otp}
                  onChange={(event) => handleOtpChange(event.target.value)}
                />
              </label>
              <button className="button button--primary button--block" disabled={verifying || otp.length !== 6} type="submit">
                {verifying ? 'Verifying…' : 'Verify and create account'}
              </button>
            </form>
            <button className="button button--ghost button--block otp-dialog__resend" disabled={resending || verifying} type="button" onClick={handleResendOtp}>
              {resending ? 'Sending code…' : 'Resend code'}
            </button>
          </section>
        </div>
      )}
    </div>
  )
}

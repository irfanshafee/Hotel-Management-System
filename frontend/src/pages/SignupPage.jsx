import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/authApi'

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', age: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

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
      navigate('/login', {
        replace: true,
        state: { notice: 'Account created. Please log in.' },
      })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
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
    </div>
  )
}

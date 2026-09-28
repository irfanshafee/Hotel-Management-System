import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="page-width section-space">
      <div className="empty-card">
        <span className="eyebrow">404</span>
        <h1>Page not found</h1>
        <p>The page you requested does not exist.</p>
        <Link className="button button--primary" to="/">Back to hotels</Link>
      </div>
    </div>
  )
}

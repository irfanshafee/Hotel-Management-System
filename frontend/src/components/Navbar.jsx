import { useEffect, useRef, useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const menuRef = useRef(null)

  useEffect(() => {
    const closeMenu = (event) => {
      if (!menuRef.current?.contains(event.target)) setOpen(false)
    }
    document.addEventListener('mousedown', closeMenu)
    return () => document.removeEventListener('mousedown', closeMenu)
  }, [])

  return (
    <header className="navbar">
      <div className="page-width navbar__inner">
        <Link className="brand" to="/" aria-label="StayFinder home">
          <span className="brand__mark" aria-hidden="true">S</span>
          <span>StayFinder</span>
        </Link>

        <nav className="navbar__links" aria-label="Primary navigation">
          <NavLink to="/" end>Hotels</NavLink>
          {isAuthenticated && <NavLink to="/bookings">My Bookings</NavLink>}
        </nav>

        <div className="navbar__auth">
          {!isAuthenticated ? (
            <>
              <Link className="button button--ghost button--small" to="/signup">Sign Up</Link>
              <Link className="button button--primary button--small" to="/login">Log In</Link>
            </>
          ) : (
            <div className="user-menu" ref={menuRef}>
              <button
                className="user-menu__trigger"
                type="button"
                aria-expanded={open}
                onClick={() => setOpen((value) => !value)}
              >
                <span className="avatar">{user.name?.charAt(0).toUpperCase()}</span>
                <span className="user-menu__greeting">Hello, {user.name}</span>
                <span aria-hidden="true">⌄</span>
              </button>
              {open && (
                <div className="user-menu__dropdown">
                  <Link to="/bookings" onClick={() => setOpen(false)}>My Bookings</Link>
                  <button type="button" onClick={logout}>Logout</button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  )
}

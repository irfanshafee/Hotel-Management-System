import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import CheckoutPage from './pages/CheckoutPage'
import HomePage from './pages/HomePage'
import HotelDetailsPage from './pages/HotelDetailsPage'
import LoginPage from './pages/LoginPage'
import MyBookingsPage from './pages/MyBookingsPage'
import NotFoundPage from './pages/NotFoundPage'
import SignupPage from './pages/SignupPage'

export default function App() {
  return (
    <div className="app-shell">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/hotels/:hotelId" element={<HotelDetailsPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/bookings" element={<MyBookingsPage />} />
            <Route path="/checkout/:bookingId" element={<CheckoutPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </div>
  )
}

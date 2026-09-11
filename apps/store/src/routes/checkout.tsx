import { ProtectedRoute } from '../components/auth/ProtectedRoute'
import { Checkout } from '../pages/Checkout'

export default function CheckoutRoute() {
  return (
    <ProtectedRoute>
      <Checkout />
    </ProtectedRoute>
  )
}

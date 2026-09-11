import { ProtectedRoute } from '../components/auth/ProtectedRoute'
import { PaymentPage } from '../pages/PaymentPage'

export default function PaymentRoute() {
  return (
    <ProtectedRoute>
      <PaymentPage />
    </ProtectedRoute>
  )
}

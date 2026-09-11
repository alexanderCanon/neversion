import { ProtectedRoute } from '../components/auth/ProtectedRoute'
import { CustomerPanel } from '../pages/CustomerPanel'

export default function CustomerPanelRoute() {
  return (
    <ProtectedRoute allowedRoles={['client']}>
      <CustomerPanel />
    </ProtectedRoute>
  )
}

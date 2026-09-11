import { GuestRoute } from '../components/auth/GuestRoute'
import { Login } from '../pages/Login'

export default function LoginRoute() {
  return (
    <GuestRoute>
      <Login />
    </GuestRoute>
  )
}

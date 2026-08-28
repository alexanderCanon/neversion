import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { Loader2 } from 'lucide-react'
import { OnboardingModal } from './OnboardingModal'
import type { UserRole } from '../../types/auth'

interface ProtectedRouteProps {
  children: React.ReactNode
  allowedRoles?: UserRole[]
}

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { user, isRestoring, needsOnboarding } = useAuth()
  const location = useLocation()

  if (isRestoring) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
        <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
        <span className="text-sm font-medium">Verificando sesión...</span>
      </div>
    )
  }

  if (needsOnboarding) {
    return <OnboardingModal />
  }

  if (!user) {
    return <Navigate to={`/login?returnUrl=${encodeURIComponent(location.pathname + location.search)}`} replace />
  }

  // Role-based access control: redirect unauthorized roles to home
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}

import { Navigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { Loader2 } from 'lucide-react'
import { OnboardingModal } from './OnboardingModal'

export function GuestRoute({ children }: { children: React.ReactNode }) {
  const { user, isRestoring, needsOnboarding } = useAuth()
  const [searchParams] = useSearchParams()
  const returnUrl = searchParams.get('returnUrl') || '/customer-panel'

  if (isRestoring) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
        <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
        <span className="text-sm font-medium">Cargando...</span>
      </div>
    )
  }

  if (needsOnboarding) {
    return <OnboardingModal />
  }

  if (user) {
    return <Navigate to={returnUrl} replace />
  }

  return <>{children}</>
}

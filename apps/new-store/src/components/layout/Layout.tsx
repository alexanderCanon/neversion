import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'
import { Footer } from './Footer'
import { CartSidebar } from './CartSidebar'
import { FloatingWhatsApp } from './FloatingWhatsApp'
import { ToastContainer } from '../ui/ToastContainer'
import { useAuth } from '../../hooks/useAuth'
import { OnboardingModal } from '../auth/OnboardingModal'

export function Layout() {
  const { needsOnboarding } = useAuth()

  return (
    <div className="min-h-screen bg-[#0d0e18] text-[#f0f0f5] flex flex-col selection:bg-[#F5A623] selection:text-[#0d0e18]">
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
      <CartSidebar />
      <ToastContainer />
      <FloatingWhatsApp />
      {needsOnboarding && <OnboardingModal />}
    </div>
  )
}


import { createBrowserRouter, Navigate } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { Home } from './pages/Home'
import { Login } from './pages/Login'
import { Checkout } from './pages/Checkout'
import { PaymentPage } from './pages/PaymentPage'
import { CustomerPanel } from './pages/CustomerPanel'
import { HowToBuy } from './pages/HowToBuy'
import { PaymentMethods } from './pages/PaymentMethods'
import { Wholesalers } from './pages/Wholesalers'
import { Contact } from './pages/Contact'
import { Support } from './pages/Support'
import { GameDetail } from './pages/GameDetail'
import { Platforms } from './pages/Platforms'
import { PlatformDetail } from './pages/PlatformDetail'
import { Games } from './pages/Games'
import { Combo } from './pages/Combo'
import { Offers } from './pages/Offers'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import { GuestRoute } from './components/auth/GuestRoute'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'platforms',
        element: <Platforms />,
      },
      {
        path: 'platforms/:platformId',
        element: <PlatformDetail />,
      },
      {
        path: 'games',
        element: <Games />,
      },
      {
        path: 'games/:slug',
        element: <GameDetail />,
      },
      {
        path: 'combo',
        element: <Combo />,
      },
      {
        path: 'offers',
        element: <Offers />,
      },
      {
        path: 'login',
        element: (
          <GuestRoute>
            <Login />
          </GuestRoute>
        ),
      },
      {
        path: 'checkout',
        element: (
          <ProtectedRoute>
            <Checkout />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payment',
        element: (
          <ProtectedRoute>
            <PaymentPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payment-page',
        element: (
          <ProtectedRoute>
            <PaymentPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'customer-panel',
        element: (
          <ProtectedRoute allowedRoles={['client']}>
            <CustomerPanel />
          </ProtectedRoute>
        ),
      },
      {
        path: 'how-to-buy',
        element: <HowToBuy />,
      },
      {
        path: 'payment-methods',
        element: <PaymentMethods />,
      },
      {
        path: 'wholesalers',
        element: <Wholesalers />,
      },
      {
        path: 'contact',
        element: <Contact />,
      },
      {
        path: 'support',
        element: <Support />,
      },
      {
        path: '*',
        element: <Navigate to="/" replace />,
      },
    ],
  },
])

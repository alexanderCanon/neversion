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
        path: 'games/:slug',
        element: <GameDetail />,
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

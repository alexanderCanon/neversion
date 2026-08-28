import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router-dom'
import { queryClient } from './lib/queryClient'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import { ToastProvider } from './context/ToastContext'
import { router } from './router'

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <CartProvider>
          <ToastProvider>
            <RouterProvider router={router} />
          </ToastProvider>
        </CartProvider>
      </AuthProvider>
    </QueryClientProvider>
  )
}


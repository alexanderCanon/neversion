import {
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
} from 'react-router'
import { useState } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import { ToastProvider } from './context/ToastContext'
import stylesheet from './index.css?url'

export const links = () => [
  { rel: 'stylesheet', href: stylesheet },
  { rel: 'icon', type: 'image/svg+xml', href: '/assets/favicon.svg' },
]

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>Neversion - Streaming & Recargas</title>
        <Meta />
        <Links />
        <script src="/assets/runtime-config.js" />
      </head>
      <body className="bg-[#0d0e18] text-[#f0f0f5]">
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  )
}

export default function App() {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 1000 * 60 * 5,
            retry: 1,
            refetchOnWindowFocus: false,
          },
        },
      }),
  )

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <CartProvider>
          <ToastProvider>
            <Outlet />
          </ToastProvider>
        </CartProvider>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export function ErrorBoundary({ error }: { error?: unknown }) {
  let message = 'Oops! Ocurrió un error inesperado.'
  if (error instanceof Error) {
    message = error.message
  }
  return (
    <div className="min-h-screen bg-[#0d0e18] text-white flex flex-col items-center justify-center p-4">
      <h1 className="text-2xl font-bold text-[#F5A623] mb-2">Algo salió mal</h1>
      <p className="text-[#a0a2b5] mb-4 text-center max-w-md">{message}</p>
      <a
        href="/"
        className="px-5 py-2.5 bg-[#F5A623] hover:bg-[#d98f18] text-[#0d0e18] font-bold rounded-xl transition-all"
      >
        Volver al Inicio
      </a>
    </div>
  )
}

import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useCart } from '../../hooks/useCart'
import neverisionLogo from '@/imports/neversion-logo-24062026.jpeg'
import {
  ShoppingCart,
  User,
  LogOut,
  Menu,
  X,
  Sparkles,
  LayoutDashboard,
} from 'lucide-react'

export function Navbar() {
  const { user, logout } = useAuth()
  const { itemCount, openCart } = useCart()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [userMenuOpen, setUserMenuOpen] = useState(false)
  const location = useLocation()

  const navLinks = [
    { label: 'Inicio', href: '/' },
    { label: 'Plataformas', href: '/platforms' },
    { label: 'Combos', href: '/combo' },
    { label: 'Juegos', href: '/games' },
    { label: 'Ofertas', href: '/offers' },
    { label: 'Mayoristas', href: '/wholesalers' },
  ]

  const handleNavClick = (href: string) => {
    setMobileOpen(false)
    if (href.startsWith('/#')) {
      const id = href.replace('/#', '')
      if (location.pathname === '/') {
        document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
      }
    }
  }

  return (
    <header className="sticky top-0 z-40 bg-[#0d0e18]/90 backdrop-blur-md border-b border-[#252838]">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3 shrink-0">
          <img
            src={neverisionLogo}
            alt="Neversion Logo"
            className="w-9 h-9 rounded-xl object-cover ring-1 ring-[#F5A623]/30"
          />
          <div className="flex flex-col">
            <span className="font-[Barlow_Condensed] text-2xl font-bold tracking-wider text-white leading-none">
              NEVERSION
            </span>
            <span className="text-[9px] uppercase tracking-widest text-[#F5A623] font-semibold">
              Store Guatemala
            </span>
          </div>
        </Link>

        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center gap-6">
          {navLinks.map(l => (
            <Link
              key={l.label}
              to={l.href}
              onClick={() => handleNavClick(l.href)}
              className="text-xs font-medium uppercase tracking-wider text-[#b0b3c6] hover:text-[#F5A623] transition-colors"
            >
              {l.label}
            </Link>
          ))}
        </nav>

        {/* Action buttons */}
        <div className="flex items-center gap-3">
          {/* Cart button */}
          <button
            onClick={openCart}
            className="relative p-2.5 rounded-xl bg-[#131623] border border-[#252838] hover:border-[#F5A623]/40 text-white transition-all cursor-pointer"
            title="Ver carrito"
          >
            <ShoppingCart className="w-4 h-4 text-[#F5A623]" />
            {itemCount > 0 && (
              <span className="absolute -top-1.5 -right-1.5 w-5 h-5 rounded-full bg-[#F5A623] text-[#0d0e18] text-[10px] font-extrabold flex items-center justify-center animate-scale">
                {itemCount}
              </span>
            )}
          </button>

          {/* User Profile / Login */}
          {user ? (
            <div className="relative">
              <button
                onClick={() => setUserMenuOpen(v => !v)}
                className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#131623] border border-[#252838] hover:border-[#F5A623]/40 text-white text-xs font-medium cursor-pointer transition-all"
              >
                <div className="w-6 h-6 rounded-lg bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623] font-bold">
                  {user.name ? user.name[0].toUpperCase() : 'U'}
                </div>
                <span className="hidden sm:inline max-w-[100px] truncate">
                  {user.name || user.email.split('@')[0]}
                </span>
              </button>

              {userMenuOpen && (
                <div
                  className="absolute right-0 mt-2 w-48 bg-[#131623] border border-[#252838] rounded-xl p-1.5 shadow-2xl z-50 animate-fade-up text-xs"
                  onClick={() => setUserMenuOpen(false)}
                >
                  <div className="px-3 py-2 border-b border-[#252838] mb-1">
                    <p className="font-semibold text-white truncate">{user.name || 'Mi Cuenta'}</p>
                    <p className="text-[10px] text-[#7a7d90] truncate">{user.email}</p>
                  </div>
                  <Link
                    to="/customer-panel"
                    className="flex items-center gap-2 px-3 py-2 rounded-lg text-[#b0b3c6] hover:text-white hover:bg-[#1a1d2e] transition-colors"
                  >
                    <LayoutDashboard className="w-3.5 h-3.5 text-[#F5A623]" />
                    <span>Mi Panel de Accesos</span>
                  </Link>
                  <button
                    onClick={() => logout()}
                    className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-red-400 hover:bg-red-500/10 transition-colors cursor-pointer text-left"
                  >
                    <LogOut className="w-3.5 h-3.5" />
                    <span>Cerrar Sesión</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link
              to="/login"
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-[#F5A623] text-[#0d0e18] hover:bg-[#e09516] font-semibold text-xs transition-all"
            >
              <User className="w-3.5 h-3.5" />
              <span>Entrar</span>
            </Link>
          )}

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMobileOpen(v => !v)}
            className="md:hidden p-2 rounded-xl bg-[#131623] border border-[#252838] text-white"
          >
            {mobileOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileOpen && (
        <div className="md:hidden bg-[#0d0e18] border-b border-[#252838] px-4 py-4 space-y-3 animate-fade-up">
          {navLinks.map(l => (
            <Link
              key={l.label}
              to={l.href}
              onClick={() => handleNavClick(l.href)}
              className="block text-sm font-medium text-[#b0b3c6] hover:text-[#F5A623] py-1"
            >
              {l.label}
            </Link>
          ))}
          {user && (
            <Link
              to="/customer-panel"
              onClick={() => setMobileOpen(false)}
              className="block text-sm font-semibold text-[#F5A623] py-1 border-t border-[#252838] pt-3"
            >
              Ir a Mi Panel
            </Link>
          )}
        </div>
      )}
    </header>
  )
}

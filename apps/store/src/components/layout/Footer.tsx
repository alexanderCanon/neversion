import { Link } from 'react-router-dom'
import neverisionLogo from '@/imports/neversion-logo-24062026.jpeg'
import { STORE_WHATSAPP_URL } from '../../config/constants'
import { ShieldCheck, Zap, Headphones, MessageCircle } from 'lucide-react'

export function Footer() {
  return (
    <footer className="bg-[#0a0b12] border-t border-[#252838] pt-14 pb-10 px-4 mt-20 text-[#7a7d90]">
      <div className="max-w-7xl mx-auto">
        {/* Value props */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 pb-12 mb-12 border-b border-[#252838]">
          <div className="flex items-center gap-3 p-4 rounded-xl bg-[#131623]/50 border border-[#252838]/60">
            <div className="w-10 h-10 rounded-xl bg-[#F5A623]/10 flex items-center justify-center text-[#F5A623]">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-white font-semibold text-sm">Activación Inmediata</h4>
              <p className="text-xs">Entrega rápida tras verificar tu pago</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-4 rounded-xl bg-[#131623]/50 border border-[#252838]/60">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 flex items-center justify-center text-emerald-400">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-white font-semibold text-sm">Garantía Total</h4>
              <p className="text-xs">Soporte y reemplazo durante toda tu suscripción</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-4 rounded-xl bg-[#131623]/50 border border-[#252838]/60">
            <div className="w-10 h-10 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-400">
              <Headphones className="w-5 h-5" />
            </div>
            <div>
              <h4 className="text-white font-semibold text-sm">Atención Personalizada</h4>
              <p className="text-xs">Soporte humano por WhatsApp 24/7</p>
            </div>
          </div>
        </div>

        {/* Links grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-10 text-xs">
          <div className="md:col-span-2 space-y-3">
            <div className="flex items-center gap-2">
              <img src={neverisionLogo} alt="Neversion" className="w-7 h-7 rounded-lg object-cover" />
              <span className="font-[Barlow_Condensed] text-xl font-bold text-white tracking-wide">
                NEVERSION
              </span>
            </div>
            <p className="text-[#7a7d90] max-w-sm leading-relaxed">
              La plataforma líder en Guatemala para adquisición de cuentas streaming, recargas gamer y servicios digitales con activación garantizada y los mejores precios del mercado.
            </p>
          </div>

          <div>
            <div className="text-xs font-semibold text-[#F5A623] uppercase tracking-widest mb-3">
              Servicios & Guías
            </div>
            <ul className="space-y-2">
              <li><Link to="/#plataformas" className="hover:text-white transition-colors">Streaming & Música</Link></li>
              <li><Link to="/#combos" className="hover:text-white transition-colors">Combos Personalizados</Link></li>
              <li><Link to="/#juegos" className="hover:text-white transition-colors">Juegos & Gift Cards</Link></li>
              <li><Link to="/#recargas" className="hover:text-white transition-colors">Recargas Móviles</Link></li>
              <li><Link to="/how-to-buy" className="hover:text-white transition-colors">¿Cómo Comprar?</Link></li>
              <li><Link to="/payment-methods" className="hover:text-white transition-colors">Métodos de Pago</Link></li>
            </ul>
          </div>

          <div>
            <div className="text-xs font-semibold text-[#F5A623] uppercase tracking-widest mb-3">
              Atención & Cuenta
            </div>
            <ul className="space-y-2">
              <li><Link to="/customer-panel" className="hover:text-white transition-colors">Mi Panel de Accesos</Link></li>
              <li><Link to="/wholesalers" className="hover:text-white transition-colors">Precios para Mayoristas</Link></li>
              <li><Link to="/support" className="hover:text-white transition-colors">Centro de Ayuda / Soporte</Link></li>
              <li><Link to="/contact" className="hover:text-white transition-colors">Contacto Directo</Link></li>
              <li>
                <a
                  href={STORE_WHATSAPP_URL}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-1.5 text-emerald-400 hover:text-emerald-300 font-medium"
                >
                  <MessageCircle className="w-3.5 h-3.5" />
                  <span>WhatsApp Soporte</span>
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Copyright */}
        <div className="border-t border-[#252838] pt-6 flex items-center justify-between flex-wrap gap-4 text-[11px]">
          <p>© 2026 Neversion. Guatemala. Todos los derechos reservados.</p>
          <p className="text-[#b0b3c6]">Precios en Quetzales (GTQ) • Activación inmediata</p>
        </div>
      </div>
    </footer>
  )
}

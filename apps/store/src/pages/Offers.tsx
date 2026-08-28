import { Link } from 'react-router-dom'
import { Tag, Sparkles, ArrowRight, Bell } from 'lucide-react'
import { AmberBtn } from '../components/ui/AmberBtn'

export function Offers() {
  return (
    <div className="min-h-screen pb-16">
      {/* Hero */}
      <div className="relative bg-[#131623] border-b border-[#252838] py-16 mb-12 overflow-hidden">
        <div className="absolute top-1/4 left-1/3 w-80 h-80 bg-[#F5A623]/8 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-1/4 right-1/4 w-60 h-60 bg-[#7B2FBE]/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative max-w-4xl mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#F5A623]/10 text-[#F5A623] text-[11px] font-bold uppercase tracking-wider mb-4">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Promociones Especiales</span>
          </div>
          <h1 className="font-[Barlow_Condensed] text-5xl sm:text-6xl font-extrabold text-white mb-4 tracking-tight">
            OFERTAS Y <span className="text-[#F5A623]">DESCUENTOS</span>
          </h1>
          <p className="text-[#7a7d90] text-lg max-w-xl mx-auto mb-8">
            Precios especiales de temporada y paquetes exclusivos para Guatemala.
          </p>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4">
        <div className="bg-[#131623] border border-[#252838] rounded-3xl p-10 text-center shadow-xl space-y-6">
          <div className="w-20 h-20 rounded-3xl bg-[#F5A623]/10 border border-[#F5A623]/20 flex items-center justify-center mx-auto text-[#F5A623]">
            <Tag className="w-10 h-10" />
          </div>

          <div className="space-y-3">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#F5A623]/10 text-[#F5A623] text-xs font-bold uppercase tracking-wider">
              <Bell className="w-3.5 h-3.5" />
              Función en desarrollo
            </span>
            <h2 className="text-2xl sm:text-3xl font-bold text-white">
              Estamos preparando ofertas exclusivas
            </h2>
            <p className="text-sm text-[#b0b3c6] max-w-md mx-auto leading-relaxed">
              Muy pronto podrás acceder a promociones flash, cupones y descuentos especiales en suscripciones y recargas gamer.
            </p>
          </div>

          <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to="/platforms" className="w-full sm:w-auto">
              <AmberBtn className="w-full sm:w-auto px-6 py-2.5">
                <span>Ver Catálogo Activo</span>
                <ArrowRight className="w-4 h-4" />
              </AmberBtn>
            </Link>
            <Link
              to="/combo"
              className="w-full sm:w-auto px-6 py-2.5 rounded-xl border border-[#252838] bg-[#1a1d2e] hover:border-[#F5A623]/40 text-white text-xs font-bold transition-all text-center"
            >
              Ver Combos de Ahorro
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

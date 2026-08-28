import { SectionHeader } from '../components/ui/SectionHeader'
import { getWhatsAppLink } from '../config/constants'
import { Briefcase, Check, MessageCircle } from 'lucide-react'

export function Wholesalers() {
  const benefits = [
    'Precios especiales por volumen a partir de 10 perfiles',
    'Entrega prioritaria y asignación automática de credenciales',
    'Soporte técnico dedicado por WhatsApp los 7 días de la semana',
    'Panel con reporte de accesos y alertas de vencimiento unificadas',
  ]

  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-up space-y-10">
      <SectionHeader
        label="Negocios & Distribuidores"
        title="Ventas para Mayoristas"
        sub="¿Tienes un negocio de reventa de streaming o deseas adquirir cuentas al por mayor? Tenemos planes preferenciales."
      />

      <div className="bg-[#131623] border border-[#252838] rounded-2xl p-8 shadow-xl space-y-6">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623]">
            <Briefcase className="w-6 h-6" />
          </div>
          <div>
            <h3 className="font-bold text-white text-xl">Programa de Distribuidores Neversion</h3>
            <p className="text-xs text-[#7a7d90]">Accede a tarifas exclusivas para revendedores en Guatemala.</p>
          </div>
        </div>

        <ul className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {benefits.map(b => (
            <li key={b} className="flex items-center gap-2 text-xs text-[#b0b3c6]">
              <Check className="w-4 h-4 text-[#F5A623] shrink-0" />
              <span>{b}</span>
            </li>
          ))}
        </ul>

        <div className="pt-4 border-t border-[#252838] flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-xs text-[#7a7d90]">
            Escríbenos directamente por WhatsApp para activar tu perfil de mayorista.
          </p>
          <a
            href={getWhatsAppLink('Hola Neversion, estoy interesado en los planes y precios para mayoristas.')}
            target="_blank"
            rel="noreferrer"
            className="px-6 py-3 rounded-xl bg-[#F5A623] text-[#0d0e18] hover:bg-[#e09516] font-bold text-sm inline-flex items-center gap-2"
          >
            <MessageCircle className="w-4 h-4" />
            <span>Contactar Asesor Mayorista</span>
          </a>
        </div>
      </div>
    </div>
  )
}

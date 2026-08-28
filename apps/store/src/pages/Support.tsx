import { SectionHeader } from '../components/ui/SectionHeader'
import { HelpCircle, RefreshCw, Key, ShieldCheck } from 'lucide-react'

export function Support() {
  const faqs = [
    {
      q: '¿Cuánto tiempo tarda la activación de una cuenta?',
      a: 'La activación promedio se realiza en 5 a 15 minutos tras validar tu comprobante de pago.',
    },
    {
      q: '¿Qué pasa si mi cuenta deja de funcionar?',
      a: 'Todas nuestras cuentas tienen garantía completa durante los 30 días de tu período. Si ocurre algún inconveniente, te reponemos el acceso inmediatamente.',
    },
    {
      q: '¿Puedo renovar la misma cuenta el siguiente mes?',
      a: 'Sí, desde tu Panel de Cliente puedes pulsar el botón "Renovar" antes de la fecha de vencimiento para conservar tu perfil.',
    },
    {
      q: '¿Cómo funcionan las recargas de Free Fire y COD Mobile?',
      a: 'Solo debes proporcionar tu ID numérico de jugador durante el pedido. Las gemas/diamantes se acreditan directamente en tu juego.',
    },
  ]

  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-up">
      <SectionHeader
        label="Preguntas Frecuentes"
        title="Centro de Ayuda y Garantías"
        sub="Encuentra respuestas rápidas a las consultas más comunes de nuestros clientes."
      />

      <div className="space-y-4">
        {faqs.map((faq, i) => (
          <div key={i} className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-2">
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <HelpCircle className="w-4 h-4 text-[#F5A623]" />
              <span>{faq.q}</span>
            </h3>
            <p className="text-xs text-[#b0b3c6] pl-6 leading-relaxed">{faq.a}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

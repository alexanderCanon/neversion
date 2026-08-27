import { SectionHeader } from '../components/ui/SectionHeader'
import { ShoppingCart, CreditCard, Send, CheckCircle2 } from 'lucide-react'

export function HowToBuy() {
  const steps = [
    {
      step: '01',
      title: 'Elige tus plataformas o recargas',
      desc: 'Selecciona los servicios que deseas desde nuestro catálogo o arma tu combo con descuento automático.',
      icon: ShoppingCart,
    },
    {
      step: '02',
      title: 'Genera tu reserva y transfiere',
      desc: 'Al confirmar tu orden, se reservan tus cupos por 15 minutos. Realiza la transferencia o depósito bancario en Quetzales.',
      icon: CreditCard,
    },
    {
      step: '03',
      title: 'Sube tu comprobante o envíalo por WhatsApp',
      desc: 'Adjunta la captura de pantalla de tu pago directamente en la web o notifícanos por nuestro canal de WhatsApp.',
      icon: Send,
    },
    {
      step: '04',
      title: 'Recibe tus credenciales y disfruta',
      desc: 'Tus accesos aparecerán de inmediato en tu Panel de Cliente y te serán notificados para que disfrutes del servicio.',
      icon: CheckCircle2,
    },
  ]

  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-up">
      <SectionHeader
        label="Guía Paso a Paso"
        title="¿Cómo Comprar en Neversion?"
        sub="Comprar tus suscripciones digitales y recargas es rápido, seguro y 100% garantizado."
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {steps.map(s => {
          const Icon = s.icon
          return (
            <div key={s.step} className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl relative overflow-hidden">
              <span className="absolute top-4 right-5 font-[Barlow_Condensed] text-4xl font-extrabold text-[#252838]">
                {s.step}
              </span>
              <div className="w-12 h-12 rounded-xl bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623] mb-4">
                <Icon className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-white text-lg mb-2">{s.title}</h3>
              <p className="text-xs text-[#7a7d90] leading-relaxed">{s.desc}</p>
            </div>
          )
        })}
      </div>
    </div>
  )
}

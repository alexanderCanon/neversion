import { SectionHeader } from '../components/ui/SectionHeader'
import { STORE_PHONE, STORE_EMAIL, STORE_WHATSAPP_URL } from '../config/constants'
import { MessageCircle, Mail, MapPin, Clock } from 'lucide-react'

export function Contact() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-up">
      <SectionHeader
        label="Atención al Cliente"
        title="Canales de Contacto"
        sub="¿Tienes alguna duda con tu compra o necesitas soporte? Estamos para ayudarte."
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
              <MessageCircle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-white text-base">WhatsApp Oficial</h3>
              <p className="text-xs text-[#7a7d90]">Respuesta rápida en minutos</p>
            </div>
          </div>
          <p className="text-xs text-[#b0b3c6]">
            Atendemos pedidos, activaciones, renovaciones y consultas generales.
          </p>
          <a
            href={STORE_WHATSAPP_URL}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-xs"
          >
            <span>Chatear por WhatsApp ({STORE_PHONE})</span>
          </a>
        </div>

        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center">
              <Mail className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-white text-base">Correo Electrónico</h3>
              <p className="text-xs text-[#7a7d90]">{STORE_EMAIL}</p>
            </div>
          </div>
          <p className="text-xs text-[#b0b3c6]">
            Para solicitudes corporativas, acuerdos comerciales y garantías formales.
          </p>
          <div className="flex items-center gap-2 text-xs text-[#7a7d90]">
            <Clock className="w-4 h-4 text-[#F5A623]" />
            <span>Horario de atención: Lunes a Domingo de 8:00 AM a 10:00 PM</span>
          </div>
        </div>
      </div>
    </div>
  )
}

import { MessageCircle } from 'lucide-react'
import { STORE_WHATSAPP_URL } from '../../config/constants'

export function FloatingWhatsApp() {
  return (
    <a
      href={STORE_WHATSAPP_URL}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Contactar por WhatsApp"
      className="fixed bottom-5 right-5 z-50 w-14 h-14 rounded-full bg-[#25d366] hover:bg-[#20ba5a] flex items-center justify-center shadow-lg shadow-[#25d366]/30 transition-all duration-300 hover:scale-110 group"
    >
      <MessageCircle className="w-7 h-7 text-white fill-white" />
      {/* Tooltip */}
      <span className="absolute right-full mr-3 px-3 py-1.5 rounded-lg bg-[#131623] border border-[#252838] text-white text-xs font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none shadow-xl">
        ¿Necesitas ayuda?
      </span>
    </a>
  )
}

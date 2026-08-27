export const STORE_PHONE = '+502 58550420'
export const STORE_WHATSAPP_RAW = '50258550420'
export const STORE_EMAIL = 'soporte@neversion.com'
export const STORE_WHATSAPP_URL = 'https://wa.me/50258550420'

export function getWhatsAppLink(message?: string): string {
  if (!message) return STORE_WHATSAPP_URL
  return `https://wa.me/${STORE_WHATSAPP_RAW}?text=${encodeURIComponent(message)}`
}

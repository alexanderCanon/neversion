import { useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { Phone, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react'

export function OnboardingModal() {
  const { pendingOAuthUser, completeOnboarding, isLoading } = useAuth()
  const [phone, setPhone] = useState('')
  const [agree, setAgree] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isValidPhone = /^[23457]\d{7}$/.test(phone.trim())

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!isValidPhone || !agree) return
    setError(null)

    const res = await completeOnboarding(`+502${phone.trim()}`)
    if (!res.success && res.error) {
      setError(res.error)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-up">
      <div className="w-full max-w-md bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-2xl text-white">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-xl bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623]">
            <Phone className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-bold text-lg leading-tight">Completa tu Registro</h3>
            <p className="text-xs text-[#7a7d90]">
              Hola <span className="text-[#F5A623]">{pendingOAuthUser?.name || 'Cliente'}</span>, necesitamos tu WhatsApp para enviarte tus accesos.
            </p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">
              Número de WhatsApp (Guatemala)
            </label>
            <div className="relative flex items-center">
              <span className="absolute left-3 text-xs font-bold text-[#7a7d90] select-none">
                +502
              </span>
              <input
                type="tel"
                placeholder="51234567"
                value={phone}
                onChange={e => setPhone(e.target.value.replace(/\D/g, '').slice(0, 8))}
                className="w-full bg-[#1a1d2e] border border-[#252838] rounded-lg pl-14 pr-4 py-2.5 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                required
              />
            </div>
            <p className="text-[11px] text-[#7a7d90] mt-1">
              Ingresa los 8 dígitos de tu número (ej. 51234567)
            </p>
          </div>

          <label className="flex items-start gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={agree}
              onChange={e => setAgree(e.target.checked)}
              className="mt-0.5 rounded border-[#252838] bg-[#1a1d2e] text-[#F5A623] focus:ring-0"
              required
            />
            <span className="text-xs text-[#b0b3c6]">
              Acepto los términos y el uso de cookies para gestionar mi sesión y servicios.
            </span>
          </label>

          <button
            type="submit"
            disabled={!isValidPhone || !agree || isLoading}
            className="w-full py-2.5 px-4 rounded-lg bg-[#F5A623] hover:bg-[#e09516] disabled:opacity-50 disabled:cursor-not-allowed font-semibold text-[#0d0e18] text-sm flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>Guardando...</span>
              </>
            ) : (
              <>
                <CheckCircle2 className="w-4 h-4" />
                <span>Continuar</span>
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  )
}

import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCart } from '../hooks/useCart'
import { useAuth } from '../hooks/useAuth'
import { useCreateReservation, useMyPointsSummary } from '../hooks/useQueries'
import { env } from '../config/env'
import { supabase } from '../lib/supabase'
import { SectionHeader } from '../components/ui/SectionHeader'
import {
  ShoppingBag,
  CreditCard,
  Building2,
  Sparkles,
  Gift,
  AlertCircle,
  Loader2,
  ArrowRight,
  ShieldCheck,
} from 'lucide-react'

export function Checkout() {
  const {
    items,
    rawTotal,
    discountAmount,
    discountPercent,
    finalTotal,
    clearCart,
    setSpotifyPreference,
  } = useCart()

  const { user } = useAuth()
  const navigate = useNavigate()

  const [availablePoints, setAvailablePoints] = useState(0)
  const [pointsToRedeem, setPointsToRedeem] = useState(0)
  const [paymentMethod, setPaymentMethod] = useState<'TRANSFERENCIA' | 'DEPOSITO'>('TRANSFERENCIA')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Fetch client points
  useEffect(() => {
    const fetchPoints = async () => {
      if (!user) return
      try {
        const token = (await supabase.auth.getSession()).data.session?.access_token
        const res = await fetch(`${env.apiUrl}/api/v1/loyalty-points/my-summary`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
        if (res.ok) {
          const data = await res.json()
          setAvailablePoints(data.available ?? 0)
        }
      } catch {
        // Fallback default points for demo/offline
        setAvailablePoints(25)
      }
    }
    fetchPoints()
  }, [user])

  if (items.length === 0) {
    return (
      <div className="max-w-xl mx-auto px-4 py-20 text-center animate-fade-up">
        <ShoppingBag className="w-16 h-16 mx-auto mb-4 text-[#252838]" />
        <h2 className="text-2xl font-bold text-white mb-2">No tienes servicios para pagar</h2>
        <p className="text-sm text-[#7a7d90] mb-6">
          Explora nuestro catálogo para agregar plataformas o recargas a tu pedido.
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-[#F5A623] text-[#0d0e18] font-bold text-sm"
        >
          Explorar Plataformas
        </Link>
      </div>
    )
  }

  const spotifyItem = items.find(i => i.name.toLowerCase().includes('spotify') && i.type === 'PROFILE')
  const maxRedeemablePoints = Math.min(availablePoints, Math.floor(finalTotal))
  const totalAfterPoints = Math.max(0, finalTotal - pointsToRedeem)

  const handlePlaceOrder = async () => {
    if (spotifyItem && !spotifyItem.spotifyAccountPreference) {
      setError('Por favor selecciona si deseas Spotify en Cuenta Nueva o en tu Cuenta Propia.')
      return
    }

    setIsSubmitting(true)
    setError(null)

    try {
      const token = (await supabase.auth.getSession()).data.session?.access_token

      const reservationPayload = {
        vendorUuid: env.storeVendorUuid,
        paymentMethod,
        pointsRedeemed: pointsToRedeem,
        items: items.map(item => ({
          serviceId: String(item.serviceId),
          type: item.type,
          quantity: item.quantity,
          spotifyAccountPreference: item.spotifyAccountPreference,
        })),
      }

      const res = await fetch(`${env.apiUrl}/api/v1/reservations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(reservationPayload),
      })

      if (res.ok) {
        const data = await res.json()
        clearCart()
        navigate(`/payment?reservationId=${data.id}`)
      } else {
        const errorData = await res.json().catch(() => ({}))
        setError(errorData.message || 'No se pudo generar la reservación en el servidor. Por favor intenta de nuevo.')
      }
    } catch (err) {
      setError((err as Error).message || 'Error de conexión con el servidor al crear la reservación.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-12 animate-fade-up">
      <SectionHeader
        label="Paso Final"
        title="Confirmar y Reservar Pedido"
        sub="Revisa los detalles de tus servicios antes de generar la orden de pago."
      />

      {error && (
        <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Left column: Cart items & Options */}
        <div className="md:col-span-2 space-y-6">
          {/* Services in cart */}
          <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <ShoppingBag className="w-4 h-4 text-[#F5A623]" />
              <span>Servicios Seleccionados</span>
            </h3>

            <div className="divide-y divide-[#252838]/60">
              {items.map(item => {
                const isSpotify = item.name.toLowerCase().includes('spotify') && item.type === 'PROFILE'
                return (
                  <div key={item.id} className="py-3.5 first:pt-0 last:pb-0 space-y-3">
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-white text-sm">{item.name}</span>
                          <span className="text-[10px] px-2 py-0.5 rounded bg-[#1a1d2e] text-[#F5A623] font-medium">
                            {item.sub}
                          </span>
                        </div>
                        <p className="text-xs text-[#7a7d90] mt-0.5">
                          Cantidad: {item.quantity} × Q{item.price}
                        </p>
                      </div>
                      <span className="font-bold text-white text-base">
                        Q{item.price * item.quantity}
                      </span>
                    </div>

                    {isSpotify && (
                      <div className="p-3 rounded-xl bg-[#1a1d2e] border border-[#252838] space-y-2 text-xs">
                        <span className="font-semibold text-white block">
                          Modalidad Requerida para Spotify:
                        </span>
                        <div className="grid grid-cols-2 gap-2">
                          <button
                            type="button"
                            onClick={() => setSpotifyPreference(item.serviceId, 'CUENTA_NUEVA')}
                            className={`py-2 px-3 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                              item.spotifyAccountPreference === 'CUENTA_NUEVA'
                                ? 'bg-[#F5A623] text-[#0d0e18]'
                                : 'bg-[#131623] text-[#7a7d90] hover:text-white border border-[#252838]'
                            }`}
                          >
                            Cuenta Nueva
                          </button>
                          <button
                            type="button"
                            onClick={() => setSpotifyPreference(item.serviceId, 'CUENTA_PROPIA')}
                            className={`py-2 px-3 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                              item.spotifyAccountPreference === 'CUENTA_PROPIA'
                                ? 'bg-[#F5A623] text-[#0d0e18]'
                                : 'bg-[#131623] text-[#7a7d90] hover:text-white border border-[#252838]'
                            }`}
                          >
                            En mi Cuenta Propia
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          </div>

          {/* Payment Method */}
          <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <CreditCard className="w-4 h-4 text-[#F5A623]" />
              <span>Método de Pago</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setPaymentMethod('TRANSFERENCIA')}
                className={`p-4 rounded-xl border text-left flex items-start gap-3 transition-all cursor-pointer ${
                  paymentMethod === 'TRANSFERENCIA'
                    ? 'border-[#F5A623] bg-[#F5A623]/10 text-white'
                    : 'border-[#252838] bg-[#1a1d2e] text-[#7a7d90] hover:text-white'
                }`}
              >
                <Building2 className="w-5 h-5 text-[#F5A623] shrink-0 mt-0.5" />
                <div>
                  <div className="font-bold text-sm">Transferencia Móvil / Web</div>
                  <div className="text-xs text-[#7a7d90] mt-0.5">BI, Banrural, G&T Continental</div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setPaymentMethod('DEPOSITO')}
                className={`p-4 rounded-xl border text-left flex items-start gap-3 transition-all cursor-pointer ${
                  paymentMethod === 'DEPOSITO'
                    ? 'border-[#F5A623] bg-[#F5A623]/10 text-white'
                    : 'border-[#252838] bg-[#1a1d2e] text-[#7a7d90] hover:text-white'
                }`}
              >
                <CreditCard className="w-5 h-5 text-[#F5A623] shrink-0 mt-0.5" />
                <div>
                  <div className="font-bold text-sm">Depósito Bancario / Caja</div>
                  <div className="text-xs text-[#7a7d90] mt-0.5">Depósito en agencias o agentes</div>
                </div>
              </button>
            </div>
          </div>

          {/* Loyalty points card */}
          {availablePoints > 0 && (
            <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-[#F5A623] font-bold text-sm">
                  <Gift className="w-4 h-4" />
                  <span>Puntos Neversion Disponibles: {availablePoints} pts</span>
                </div>
                <button
                  type="button"
                  onClick={() =>
                    setPointsToRedeem(pointsToRedeem > 0 ? 0 : maxRedeemablePoints)
                  }
                  className="text-xs text-[#F5A623] hover:underline font-semibold cursor-pointer"
                >
                  {pointsToRedeem > 0 ? 'Quitar Puntos' : `Usar ${maxRedeemablePoints} pts (-Q${maxRedeemablePoints})`}
                </button>
              </div>
              <p className="text-xs text-[#7a7d90]">
                1 Punto = Q1 de descuento directo en tu compra.
              </p>
            </div>
          )}
        </div>

        {/* Right column: Order Total and Submit */}
        <div className="space-y-6">
          <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4 sticky top-24">
            <h3 className="font-bold text-white text-base">Resumen de Cuenta</h3>

            <div className="space-y-2.5 text-xs">
              <div className="flex justify-between text-[#7a7d90]">
                <span>Subtotal</span>
                <span>Q{rawTotal}</span>
              </div>

              {discountAmount > 0 && (
                <div className="flex justify-between text-emerald-400 font-semibold">
                  <span className="flex items-center gap-1">
                    <Sparkles className="w-3.5 h-3.5" />
                    Descuento Combo ({discountPercent}%)
                  </span>
                  <span>-Q{discountAmount}</span>
                </div>
              )}

              {pointsToRedeem > 0 && (
                <div className="flex justify-between text-[#F5A623] font-semibold">
                  <span className="flex items-center gap-1">
                    <Gift className="w-3.5 h-3.5" />
                    Puntos Canjeados
                  </span>
                  <span>-Q{pointsToRedeem}</span>
                </div>
              )}

              <div className="flex justify-between text-white font-bold text-xl pt-3 border-t border-[#252838]">
                <span>Total a Pagar</span>
                <span className="text-[#F5A623]">Q{totalAfterPoints}</span>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-[#1a1d2e] border border-[#252838] flex items-center gap-2.5 text-xs text-[#b0b3c6]">
              <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0" />
              <span>Garantía de activación inmediata tras validación.</span>
            </div>

            <button
              type="button"
              onClick={handlePlaceOrder}
              disabled={isSubmitting}
              className="w-full py-3.5 rounded-xl bg-[#F5A623] hover:bg-[#e09516] text-[#0d0e18] font-bold text-sm flex items-center justify-center gap-2 transition-all cursor-pointer shadow-lg shadow-[#F5A623]/20 disabled:opacity-50"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Creando Reserva...</span>
                </>
              ) : (
                <>
                  <span>Proceder a Pagar</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

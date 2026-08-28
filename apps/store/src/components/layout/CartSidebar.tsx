import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../../hooks/useCart'
import { useAuth } from '../../hooks/useAuth'
import { getWhatsAppLink } from '../../config/constants'
import { X, Trash2, Plus, Minus, ShoppingBag, ArrowRight, Sparkles, MessageCircle } from 'lucide-react'

export function CartSidebar() {
  const {
    items,
    isCartOpen,
    closeCart,
    removeFromCart,
    updateQuantity,
    setSpotifyPreference,
    rawTotal,
    discountPercent,
    discountAmount,
    finalTotal,
    itemCount,
  } = useCart()

  const { user } = useAuth()
  const navigate = useNavigate()

  if (!isCartOpen) return null

  const handleCheckoutClick = () => {
    closeCart()
    if (!user) {
      navigate('/login?returnUrl=/checkout')
    } else {
      navigate('/checkout')
    }
  }

  const generateWhatsAppMessage = () => {
    const lines = [
      '👋 ¡Hola Neversion! Me gustaría realizar un pedido:',
      '',
      ...items.map(i => {
        let extra = ''
        if (i.spotifyAccountPreference) {
          extra = ` (${i.spotifyAccountPreference === 'CUENTA_NUEVA' ? 'Cuenta Nueva' : 'Cuenta Propia'})`
        }
        return `• ${i.name} [${i.sub}]${extra} x${i.quantity} — Q${i.price * i.quantity}`
      }),
      '',
      discountAmount > 0 ? `🔥 Descuento Combo: -Q${discountAmount}` : '',
      `💰 *Total: Q${finalTotal}*`,
      '',
      '¿Me podrían indicar los pasos para pagar por transferencia o depósito?',
    ].filter(Boolean)

    return encodeURIComponent(lines.join('\n'))
  }

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div
        onClick={closeCart}
        className="absolute inset-0 bg-black/70 backdrop-blur-xs transition-opacity duration-300"
      />

      <aside className="absolute inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-[#131623] border-l border-[#252838] flex flex-col shadow-2xl text-white">
          {/* Header */}
          <div className="p-5 border-b border-[#252838] flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623]">
                <ShoppingBag className="w-4 h-4" />
              </div>
              <div>
                <h3 className="font-bold text-base">Tu Carrito</h3>
                <p className="text-[11px] text-[#7a7d90]">
                  {itemCount} {itemCount === 1 ? 'servicio agregado' : 'servicios agregados'}
                </p>
              </div>
            </div>
            <button
              onClick={closeCart}
              className="p-1.5 rounded-lg text-[#7a7d90] hover:text-white hover:bg-[#1a1d2e] transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Items list */}
          <div className="flex-1 overflow-y-auto p-5 space-y-3 divide-y divide-[#252838]/50">
            {items.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 text-[#7a7d90]">
                <ShoppingBag className="w-12 h-12 stroke-[1.2] mb-3 text-[#252838]" />
                <p className="text-sm font-semibold text-[#b0b3c6]">Tu carrito está vacío</p>
                <p className="text-xs mt-1 max-w-xs">
                  Explora nuestras plataformas de streaming, juegos o combos y añade tus servicios preferidos.
                </p>
              </div>
            ) : (
              items.map(item => {
                const isSpotify = item.name.toLowerCase().includes('spotify') && item.type === 'PROFILE'
                return (
                  <div key={item.id} className="pt-3 first:pt-0 space-y-2.5">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <h4 className="font-semibold text-sm text-white truncate">{item.name}</h4>
                          <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#1a1d2e] text-[#F5A623] font-medium">
                            {item.sub}
                          </span>
                        </div>
                        <p className="text-xs text-[#7a7d90] mt-0.5">
                          Q{item.price} c/u
                        </p>
                      </div>

                      <div className="flex items-center gap-2">
                        <div className="flex items-center border border-[#252838] rounded-lg bg-[#1a1d2e]">
                          <button
                            onClick={() => updateQuantity(item.serviceId, item.type, item.quantity - 1)}
                            className="p-1 text-[#7a7d90] hover:text-white transition-colors cursor-pointer"
                          >
                            <Minus className="w-3 h-3" />
                          </button>
                          <span className="px-2 text-xs font-bold">{item.quantity}</span>
                          <button
                            onClick={() => updateQuantity(item.serviceId, item.type, item.quantity + 1)}
                            className="p-1 text-[#7a7d90] hover:text-white transition-colors cursor-pointer"
                          >
                            <Plus className="w-3 h-3" />
                          </button>
                        </div>

                        <button
                          onClick={() => removeFromCart(item.serviceId, item.type)}
                          className="p-1.5 text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-lg transition-colors cursor-pointer"
                          title="Eliminar"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>

                    {/* Spotify Account Preference Selector */}
                    {isSpotify && (
                      <div className="p-2.5 rounded-lg bg-[#1a1d2e] border border-[#252838] space-y-1.5 text-xs">
                        <span className="block text-[11px] font-semibold text-[#b0b3c6]">
                          Modalidad para Spotify:
                        </span>
                        <div className="grid grid-cols-2 gap-1.5">
                          <button
                            type="button"
                            onClick={() => setSpotifyPreference(item.serviceId, 'CUENTA_NUEVA')}
                            className={`px-2 py-1.5 rounded text-center text-xs font-medium cursor-pointer transition-colors ${
                              item.spotifyAccountPreference === 'CUENTA_NUEVA'
                                ? 'bg-[#F5A623] text-[#0d0e18] font-bold'
                                : 'bg-[#131623] text-[#7a7d90] hover:text-white'
                            }`}
                          >
                            Cuenta Nueva
                          </button>
                          <button
                            type="button"
                            onClick={() => setSpotifyPreference(item.serviceId, 'CUENTA_PROPIA')}
                            className={`px-2 py-1.5 rounded text-center text-xs font-medium cursor-pointer transition-colors ${
                              item.spotifyAccountPreference === 'CUENTA_PROPIA'
                                ? 'bg-[#F5A623] text-[#0d0e18] font-bold'
                                : 'bg-[#131623] text-[#7a7d90] hover:text-white'
                            }`}
                          >
                            En mi Cuenta
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )
              })
            )}
          </div>

          {/* Footer calculation */}
          {items.length > 0 && (
            <div className="p-5 bg-[#0e101b] border-t border-[#252838] space-y-4">
              <div className="space-y-1.5 text-xs">
                <div className="flex justify-between text-[#7a7d90]">
                  <span>Subtotal</span>
                  <span>Q{rawTotal}</span>
                </div>

                {discountAmount > 0 && (
                  <div className="flex justify-between text-emerald-400 font-semibold items-center">
                    <span className="flex items-center gap-1">
                      <Sparkles className="w-3.5 h-3.5" />
                      Descuento Combo ({discountPercent}%)
                    </span>
                    <span>-Q{discountAmount}</span>
                  </div>
                )}

                <div className="flex justify-between text-white font-bold text-base pt-2 border-t border-[#252838]">
                  <span>Total</span>
                  <span className="text-[#F5A623]">Q{finalTotal}</span>
                </div>
              </div>

              <div className="space-y-2">
                <button
                  onClick={handleCheckoutClick}
                  className="w-full py-3 px-4 rounded-xl bg-[#F5A623] hover:bg-[#e09516] text-[#0d0e18] font-bold text-sm flex items-center justify-center gap-2 transition-all cursor-pointer shadow-lg shadow-[#F5A623]/20"
                >
                  <span>Continuar al Pago</span>
                  <ArrowRight className="w-4 h-4" />
                </button>

                <a
                  href={getWhatsAppLink(decodeURIComponent(generateWhatsAppMessage()))}
                  target="_blank"
                  rel="noreferrer"
                  className="w-full py-2.5 px-4 rounded-xl border border-emerald-500/40 text-emerald-400 hover:bg-emerald-500/10 font-semibold text-xs flex items-center justify-center gap-2 transition-all"
                >
                  <MessageCircle className="w-4 h-4" />
                  <span>Comprar directo por WhatsApp</span>
                </a>
              </div>
            </div>
          )}
        </div>
      </aside>
    </div>
  )
}

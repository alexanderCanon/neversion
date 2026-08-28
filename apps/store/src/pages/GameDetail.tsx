import { useParams, useNavigate } from 'react-router-dom'
import { useGames, useGameSkus } from '../hooks/useQueries'
import { SectionHeader } from '../components/ui/SectionHeader'
import { getWhatsAppLink } from '../config/constants'
import { resolveServiceImageUrl } from '../lib/image'
import { ArrowLeft, MessageCircle, Loader2, AlertCircle, Sparkles, Gamepad2 } from 'lucide-react'

export function GameDetail() {
  const { slug } = useParams<{ slug: string }>()
  const navigate = useNavigate()

  const { data: games, isLoading: loadingGames } = useGames()
  const { data: skus, isLoading: loadingSkus, error: errorSkus } = useGameSkus(slug)

  const game = games?.find(g => g.slug === slug)

  const handleContactToBuy = (sku: { name: string; price: number }) => {
    const gameName = game?.name || 'Juego'
    const text = `Hola Neversion, quiero comprar el paquete: *${sku.name}* (${gameName}) - Precio: Q${sku.price}. Por favor envíenme los datos de pago.`
    const url = getWhatsAppLink(text)
    window.open(url, '_blank')
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-12 animate-fade-up space-y-10">
      {/* Back button */}
      <div>
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#131623] hover:bg-[#1a1d2e] text-[#7a7d90] hover:text-white border border-[#252838] text-xs font-semibold transition-all cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver al Catálogo</span>
        </button>
      </div>

      {/* Game Header */}
      {loadingGames ? (
        <div className="py-12 flex justify-center text-[#F5A623]">
          <Loader2 className="w-8 h-8 animate-spin" />
        </div>
      ) : game ? (
        <div className="bg-[#131623] border border-[#252838] rounded-3xl p-8 shadow-2xl flex flex-col sm:flex-row items-center gap-6 relative overflow-hidden">
          <div className="w-24 h-24 rounded-2xl bg-white/5 border border-[#252838] p-2 flex items-center justify-center shrink-0 overflow-hidden">
            {game.imageUrl ? (
              <img
                src={resolveServiceImageUrl(game.imageUrl)}
                alt={game.name}
                className="w-full h-full object-contain"
              />
            ) : (
              <Gamepad2 className="w-12 h-12 text-[#107C10]" />
            )}
          </div>

          <div className="text-center sm:text-left">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#107C10]/20 text-[#107C10] text-[11px] font-bold uppercase tracking-wider mb-2">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Recargas & Pases Oficiales</span>
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold text-white">{game.name}</h1>
            <p className="text-xs sm:text-sm text-[#7a7d90] mt-1">
              Selecciona el paquete de diamantes, monedas o pases que deseas recargar.
            </p>
          </div>
        </div>
      ) : (
        <div className="p-6 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400 text-center text-xs">
          No se encontró la información del juego solicitado.
        </div>
      )}

      {/* Packages / SKUs Grid */}
      <section className="space-y-6">
        <SectionHeader
          label="Paquetes Disponibles"
          title={`Recargas para ${game?.name || 'este juego'}`}
          sub="Entrega directa por ID de jugador. Activación rápida y segura."
        />

        {loadingSkus && (
          <div className="py-16 flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
            <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
            <span className="text-sm">Consultando paquetes en Supabase...</span>
          </div>
        )}

        {errorSkus && (
          <div className="p-6 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-center space-y-2">
            <AlertCircle className="w-8 h-8 mx-auto" />
            <h3 className="font-bold text-base">Error al cargar los paquetes</h3>
            <p className="text-xs">{(errorSkus as Error)?.message}</p>
          </div>
        )}

        {!loadingSkus && !errorSkus && (
          <>
            {!skus || skus.length === 0 ? (
              <div className="py-16 text-center text-[#7a7d90] bg-[#131623] border border-[#252838] rounded-2xl p-8 space-y-3">
                <Gamepad2 className="w-12 h-12 mx-auto text-[#252838]" />
                <h4 className="font-bold text-white text-base">Sin paquetes configurados</h4>
                <p className="text-xs max-w-md mx-auto">
                  Actualmente no hay paquetes o SKUs activos para este juego en la base de datos.
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
                {skus.map(sku => (
                  <div
                    key={sku.id}
                    className="bg-[#131623] border border-[#252838] hover:border-[#F5A623]/50 rounded-2xl p-5 flex flex-col justify-between transition-all duration-300 hover:-translate-y-1 shadow-xl group"
                  >
                    <div>
                      <div className="flex items-center justify-between mb-3">
                        <span className="text-[10px] uppercase tracking-wider font-bold px-2 py-0.5 rounded-full bg-[#F5A623]/15 text-[#F5A623]">
                          {sku.code || 'SKU'}
                        </span>
                      </div>

                      {sku.imageUrl && (
                        <div className="w-full h-24 rounded-xl bg-white/5 border border-[#252838] p-2 mb-3 flex items-center justify-center overflow-hidden">
                          <img
                            src={resolveServiceImageUrl(sku.imageUrl)}
                            alt={sku.name}
                            className="max-h-full object-contain"
                          />
                        </div>
                      )}

                      <h3 className="font-bold text-white text-base leading-tight mb-2">
                        {sku.name}
                      </h3>
                    </div>

                    <div className="pt-4 border-t border-[#252838] space-y-3">
                      <div>
                        <span className="text-[10px] uppercase text-[#7a7d90] font-semibold block">Precio</span>
                        <div className="font-[Barlow_Condensed] text-2xl font-bold text-white">
                          Q{sku.price}
                        </div>
                      </div>

                      <button
                        onClick={() => handleContactToBuy(sku)}
                        className="w-full py-2.5 rounded-xl bg-emerald-500/15 hover:bg-emerald-500 text-emerald-400 hover:text-[#0d0e18] font-bold text-xs flex items-center justify-center gap-2 transition-all cursor-pointer"
                      >
                        <MessageCircle className="w-4 h-4" />
                        <span>Solicitar por WhatsApp</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </section>
    </div>
  )
}

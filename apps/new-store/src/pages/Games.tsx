import { Link } from 'react-router-dom'
import { useGames } from '../hooks/useQueries'
import { SectionHeader } from '../components/ui/SectionHeader'
import { PlatformBadge } from '../components/ui/PlatformBadge'
import { resolveServiceImageUrl } from '../lib/image'
import { Loader2, AlertCircle, Gamepad2, ChevronRight } from 'lucide-react'

function getGameInitials(name: string): string {
  if (!name) return 'GM'
  const parts = name.trim().split(' ')
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase()
  return name.slice(0, 3).toUpperCase()
}

export function Games() {
  const { data: games, isLoading, isError, error } = useGames()

  return (
    <div className="min-h-screen pb-16">
      {/* Hero */}
      <div className="bg-[#131623] border-b border-[#252838] py-12 mb-8">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#107C10]/20 text-[#107C10] text-[11px] font-bold uppercase tracking-wider mb-4">
            <Gamepad2 className="w-3.5 h-3.5" />
            <span>Mundo Gamer</span>
          </div>
          <h1 className="font-[Barlow_Condensed] text-4xl sm:text-5xl font-extrabold text-white mb-3 tracking-tight">
            JUEGOS Y RECARGAS
          </h1>
          <p className="text-[#7a7d90] text-base max-w-xl mx-auto">
            Las mejores recargas, diamantes y pases para tus videojuegos favoritos en un solo lugar.
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4">
        {/* Loading */}
        {isLoading && (
          <div className="py-20 flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
            <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
            <span className="text-sm">Cargando catálogo de juegos...</span>
          </div>
        )}

        {/* Error */}
        {isError && (
          <div className="p-6 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-center space-y-2">
            <AlertCircle className="w-8 h-8 mx-auto" />
            <h3 className="font-bold text-base">Error al obtener los juegos</h3>
            <p className="text-xs max-w-md mx-auto">
              {(error as Error)?.message || 'No se pudo conectar con el catálogo de juegos.'}
            </p>
          </div>
        )}

        {/* Content */}
        {!isLoading && !isError && (
          <>
            {!games || games.length === 0 ? (
              <div className="py-16 text-center text-[#7a7d90] bg-[#131623] border border-[#252838] rounded-2xl p-8 space-y-3">
                <Gamepad2 className="w-12 h-12 mx-auto text-[#252838]" />
                <h4 className="font-bold text-white text-base">Próximamente más juegos</h4>
                <p className="text-xs max-w-md mx-auto">
                  Estamos trabajando para traerte las mejores recargas y gift cards del mercado.
                </p>
                <Link
                  to="/platforms"
                  className="inline-block mt-4 px-4 py-2 rounded-lg border border-[#F5A623]/40 text-[#F5A623] text-xs font-semibold hover:bg-[#F5A623]/10"
                >
                  Ver otras plataformas
                </Link>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {games.map(game => (
                  <Link
                    key={game.id}
                    to={`/games/${game.slug}`}
                    className="bg-[#131623] rounded-2xl border border-[#252838] hover:border-[#F5A623]/40 p-6 flex flex-col justify-between transition-all hover:-translate-y-1 shadow-xl group"
                  >
                    {/* Game image or badge */}
                    <div>
                      <div className="flex items-start gap-3 mb-4">
                        {game.imageUrl ? (
                          <div className="w-16 h-16 rounded-xl bg-white/5 border border-[#252838] overflow-hidden flex items-center justify-center shrink-0">
                            <img
                              src={resolveServiceImageUrl(game.imageUrl)}
                              alt={game.name}
                              className="w-12 h-12 object-contain"
                            />
                          </div>
                        ) : (
                          <PlatformBadge letters={getGameInitials(game.name)} color="#107C10" />
                        )}
                        <div>
                          <h3 className="font-bold text-white text-base leading-tight group-hover:text-[#F5A623] transition-colors">
                            {game.name}
                          </h3>
                          <span className="text-[#7a7d90] text-xs mt-1 block">
                            Recargas y pases disponibles
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="pt-4 border-t border-[#252838]">
                      <span className="w-full py-2.5 rounded-xl bg-[#1a1d2e] group-hover:bg-[#F5A623] group-hover:text-[#0d0e18] text-white text-center text-xs font-bold transition-all flex items-center justify-center gap-1">
                        Ver Paquetes
                        <ChevronRight className="w-3.5 h-3.5" />
                      </span>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}

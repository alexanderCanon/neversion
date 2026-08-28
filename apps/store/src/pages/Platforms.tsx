import { useState, useMemo } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useServices, type StoreServiceItem } from '../hooks/useQueries'
import { useCart } from '../hooks/useCart'
import { SectionHeader } from '../components/ui/SectionHeader'
import { AmberBtn } from '../components/ui/AmberBtn'
import { Badge } from '../components/ui/Badge'
import { PlatformBadge } from '../components/ui/PlatformBadge'
import { resolveServiceImageUrl } from '../lib/image'
import {
  Search,
  X,
  Loader2,
  AlertCircle,
  Tv,
  Check,
  ArrowRight,
  Clock,
  ChevronRight,
} from 'lucide-react'

// Helper for consistent brand colors based on name
function getServiceColor(name: string): string {
  const n = name.toLowerCase()
  if (n.includes('netflix')) return '#E50914'
  if (n.includes('disney')) return '#113CCF'
  if (n.includes('hbo') || n.includes('max')) return '#7B2FBE'
  if (n.includes('crunchyroll')) return '#F47521'
  if (n.includes('spotify')) return '#1DB954'
  if (n.includes('prime') || n.includes('amazon')) return '#00A8E0'
  if (n.includes('youtube')) return '#FF0000'
  if (n.includes('vix')) return '#FF5A00'
  if (n.includes('iptv')) return '#6C3CE1'
  if (n.includes('paramount')) return '#0064FF'
  if (n.includes('apple')) return '#A2AAAD'
  return '#F5A623'
}

function getServiceInitials(name: string): string {
  if (!name) return 'SRV'
  const parts = name.trim().split(' ')
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase()
  return name.slice(0, 3).toUpperCase()
}

export function Platforms() {
  const [searchParams, setSearchParams] = useSearchParams()
  const searchQuery = searchParams.get('q') || ''
  const [localSearch, setLocalSearch] = useState(searchQuery)
  const [selectedCategory, setSelectedCategory] = useState<string>('todos')
  const { addToCart } = useCart()
  const { data: services, isLoading, isError, error } = useServices()

  // Derive categories from data
  const categories = useMemo(() => {
    if (!services || services.length === 0) return ['todos']
    const unique = Array.from(new Set(services.map(s => s.category).filter(Boolean))) as string[]
    return ['todos', ...unique]
  }, [services])

  // Filter by category + search
  const filteredServices = useMemo(() => {
    if (!services) return []
    let result = services

    if (selectedCategory !== 'todos') {
      result = result.filter(s => s.category === selectedCategory)
    }

    const q = (searchQuery || localSearch).toLowerCase().trim()
    if (q) {
      result = result.filter(
        s =>
          s.name.toLowerCase().includes(q) ||
          s.category?.toLowerCase().includes(q) ||
          s.description?.toLowerCase().includes(q)
      )
    }

    return result
  }, [services, selectedCategory, searchQuery, localSearch])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (localSearch.trim()) {
      setSearchParams({ q: localSearch.trim() })
    } else {
      setSearchParams({})
    }
  }

  const clearSearch = () => {
    setLocalSearch('')
    setSearchParams({})
  }

  return (
    <div className="min-h-screen pb-16">
      {/* Hero */}
      <div className="bg-[#131623] border-b border-[#252838] py-12 mb-8">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="font-[Barlow_Condensed] text-4xl sm:text-5xl font-extrabold text-white mb-3 tracking-tight">
            CATÁLOGO DE SERVICIOS
          </h1>
          <p className="text-[#7a7d90] text-base max-w-xl mx-auto mb-8">
            Elige tu plataforma y activa en minutos. Perfiles individuales o cuentas completas con garantía total.
          </p>

          {/* Search bar */}
          <form onSubmit={handleSearch} className="max-w-md mx-auto relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#7a7d90]" />
            <input
              type="text"
              value={localSearch}
              onChange={e => setLocalSearch(e.target.value)}
              placeholder="Buscar plataforma..."
              className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-[#0d0e18] border border-[#252838] text-white placeholder:text-[#7a7d90] text-sm focus:outline-none focus:border-[#F5A623]/50"
            />
            {localSearch && (
              <button
                type="button"
                onClick={clearSearch}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-[#7a7d90] hover:text-white cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </form>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4">
        {/* Active search banner */}
        {searchQuery && (
          <div className="flex items-center justify-between p-3 mb-6 rounded-xl bg-[#1a1d2e] border border-[#252838] text-sm">
            <span className="text-[#b0b3c6]">
              <Search className="w-4 h-4 inline mr-2" />
              Resultados para: <strong className="text-white">"{searchQuery}"</strong>
            </span>
            <button
              onClick={clearSearch}
              className="px-3 py-1 rounded-lg border border-[#252838] text-[#7a7d90] hover:text-white text-xs cursor-pointer"
            >
              <X className="w-3 h-3 inline mr-1" /> Limpiar
            </button>
          </div>
        )}

        {/* Loading */}
        {isLoading && (
          <div className="py-20 flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
            <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
            <span className="text-sm">Cargando catálogo de servicios...</span>
          </div>
        )}

        {/* Error */}
        {isError && (
          <div className="p-6 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-center space-y-2">
            <AlertCircle className="w-8 h-8 mx-auto" />
            <h3 className="font-bold text-base">Error al obtener las plataformas</h3>
            <p className="text-xs max-w-md mx-auto">
              {(error as Error)?.message || 'No se pudo conectar con el catálogo de servicios.'}
            </p>
          </div>
        )}

        {/* Content */}
        {!isLoading && !isError && (
          <>
            {/* Category filters */}
            {categories.length > 1 && (
              <div className="flex items-center gap-2 mb-8 flex-wrap">
                {categories.map(c => (
                  <button
                    key={c}
                    onClick={() => setSelectedCategory(c)}
                    className={`px-4 py-1.5 rounded-full text-xs font-semibold tracking-wider uppercase transition-all cursor-pointer ${
                      selectedCategory === c
                        ? 'bg-[#F5A623] text-[#0d0e18]'
                        : 'border border-[#252838] text-[#7a7d90] hover:border-[#F5A623]/40 hover:text-[#b0b3c6]'
                    }`}
                  >
                    {c}
                  </button>
                ))}
              </div>
            )}

            {filteredServices.length === 0 ? (
              <div className="py-16 text-center text-[#7a7d90] bg-[#131623] border border-[#252838] rounded-2xl p-8">
                <Tv className="w-12 h-12 mx-auto mb-3 text-[#252838]" />
                <h4 className="font-bold text-white text-base">
                  {searchQuery
                    ? `No se encontraron servicios para "${searchQuery}"`
                    : 'No hay plataformas activas'}
                </h4>
                <p className="text-xs mt-1">
                  {searchQuery
                    ? 'Intenta con otro término de búsqueda.'
                    : 'Actualmente no hay servicios disponibles.'}
                </p>
                {searchQuery && (
                  <button
                    onClick={clearSearch}
                    className="mt-4 px-4 py-2 rounded-lg border border-[#F5A623]/40 text-[#F5A623] text-xs font-semibold hover:bg-[#F5A623]/10 cursor-pointer"
                  >
                    Ver todos los servicios
                  </button>
                )}
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredServices.map(service => (
                  <PlatformCard key={service.id} service={service} onAdd={addToCart} />
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}

// Platform card with price selector and add-to-cart
function PlatformCard({ service, onAdd }: { service: StoreServiceItem; onAdd: any }) {
  const [mode, setMode] = useState<'perfil' | 'cuenta'>('perfil')

  const hasProfile = service.priceProfile !== undefined && service.priceProfile > 0
  const hasComplete = service.priceComplete !== undefined && service.priceComplete > 0
  const activeMode = mode === 'cuenta' && hasComplete ? 'cuenta' : 'perfil'
  const activePrice = activeMode === 'perfil' ? service.priceProfile ?? 0 : service.priceComplete ?? 0
  const color = getServiceColor(service.name)
  const letters = getServiceInitials(service.name)

  const handleAdd = () => {
    onAdd(
      {
        id: service.id,
        name: service.name,
        sub: activeMode === 'perfil' ? 'Perfil individual' : 'Cuenta completa',
        category: service.category,
        price: activePrice,
        color,
        letters,
      },
      activeMode === 'perfil' ? 'PROFILE' : 'COMPLETE'
    )
  }

  return (
    <div
      className="bg-[#131623] rounded-2xl border border-[#252838] hover:border-[#F5A623]/40 transition-all duration-300 hover:-translate-y-1 flex flex-col justify-between overflow-hidden shadow-xl"
      style={{ borderTop: `3px solid ${color}` }}
    >
      <div className="p-6">
        {/* Header */}
        <div className="flex items-start gap-3 mb-4">
          {service.imageUrl ? (
            <div className="w-14 h-14 rounded-xl bg-white/5 border border-[#252838] p-2 flex items-center justify-center shrink-0 overflow-hidden">
              <img
                src={resolveServiceImageUrl(service.imageUrl)}
                alt={service.name}
                className="w-full h-full object-contain"
              />
            </div>
          ) : (
            <PlatformBadge letters={letters} color={color} />
          )}
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h3 className="font-bold text-white text-lg leading-tight truncate">{service.name}</h3>
              {service.category && <Badge>{service.category}</Badge>}
            </div>
            <p className="text-[#7a7d90] text-xs mt-1 leading-snug line-clamp-2">
              {service.description || 'Suscripción digital con activación garantizada.'}
            </p>
          </div>
        </div>

        {/* Features */}
        <ul className="space-y-1.5 mb-4">
          <li className="flex items-center gap-2 text-xs text-[#b0b3c6]">
            <Check className="w-3.5 h-3.5 text-[#F5A623] shrink-0" />
            <span>Duración: {service.durationDays || 30} días de servicio</span>
          </li>
          {service.maxProfiles && (
            <li className="flex items-center gap-2 text-xs text-[#b0b3c6]">
              <Check className="w-3.5 h-3.5 text-[#F5A623] shrink-0" />
              <span>Hasta {service.maxProfiles} perfiles por cuenta</span>
            </li>
          )}
          <li className="flex items-center gap-2 text-xs text-[#b0b3c6]">
            <Check className="w-3.5 h-3.5 text-[#F5A623] shrink-0" />
            <span>Garantía y reemplazo durante todo el período</span>
          </li>
        </ul>

        {/* Price selector */}
        <div className="border border-[#252838] rounded-xl overflow-hidden mb-5 bg-[#0d0e18]">
          <div className="grid grid-cols-2">
            <button
              type="button"
              disabled={!hasProfile}
              onClick={() => setMode('perfil')}
              className={`py-2.5 px-3 text-center transition-colors border-r border-[#252838] ${
                !hasProfile
                  ? 'opacity-40 cursor-not-allowed text-[#7a7d90]'
                  : activeMode === 'perfil'
                  ? 'bg-[#F5A623]/15 text-[#F5A623] cursor-pointer'
                  : 'text-[#7a7d90] hover:bg-[#131623] cursor-pointer'
              }`}
            >
              <div className="text-[10px] uppercase font-bold tracking-wider">Perfil ind.</div>
              <div className="font-bold text-base mt-0.5">
                {hasProfile ? `Q${service.priceProfile}` : 'N/D'}
              </div>
            </button>

            <button
              type="button"
              disabled={!hasComplete}
              onClick={() => hasComplete && setMode('cuenta')}
              className={`py-2.5 px-3 text-center transition-colors ${
                !hasComplete
                  ? 'opacity-40 cursor-not-allowed text-[#7a7d90]'
                  : activeMode === 'cuenta'
                  ? 'bg-[#F5A623]/15 text-[#F5A623] cursor-pointer'
                  : 'text-[#7a7d90] hover:bg-[#131623] cursor-pointer'
              }`}
            >
              <div className="text-[10px] uppercase font-bold tracking-wider">Cuenta comp.</div>
              <div className="font-bold text-base mt-0.5">
                {hasComplete ? `Q${service.priceComplete}` : 'N/D'}
              </div>
            </button>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="px-6 pb-4 space-y-2">
        <AmberBtn onClick={handleAdd} disabled={!hasProfile && !hasComplete} className="w-full">
          <span>Agregar al Carrito</span>
          <ArrowRight className="w-4 h-4" />
        </AmberBtn>
        <Link
          to={`/platforms/${service.id}`}
          className="block w-full py-2 text-center text-[#7a7d90] hover:text-[#F5A623] text-xs font-semibold transition-colors"
        >
          Ver detalles <ChevronRight className="w-3 h-3 inline" />
        </Link>
      </div>
    </div>
  )
}

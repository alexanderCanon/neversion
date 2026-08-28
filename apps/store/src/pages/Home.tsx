import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../hooks/useCart'
import { useServices, useGames, useVendorPublic, type StoreServiceItem, type StoreGameItem } from '../hooks/useQueries'
import { SectionHeader } from '../components/ui/SectionHeader'
import { AmberBtn } from '../components/ui/AmberBtn'
import { Badge } from '../components/ui/Badge'
import { PlatformBadge } from '../components/ui/PlatformBadge'
import { resolveServiceImageUrl } from '../lib/image'
import {
  Sparkles,
  Check,
  ChevronDown,
  ArrowRight,
  Loader2,
  AlertCircle,
  Gamepad2,
  Tv,
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
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase()
  }
  return name.slice(0, 3).toUpperCase()
}

export function Home() {
  const { addToCart } = useCart()

  return (
    <div className="space-y-12">
      <HeroSection />
      <PlatformsSection onAdd={addToCart} />
      <ComboBuilderSection onAdd={addToCart} />
      <GamesSection onAdd={addToCart} />
    </div>
  )
}

function HeroSection() {
  const scrollToPlatforms = () => {
    document.getElementById('plataformas')?.scrollIntoView({ behavior: 'smooth' })
  }

  return (
    <section className="relative min-h-[85vh] flex items-center justify-center overflow-hidden pt-12 pb-16">
      {/* Glow */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#F5A623]/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-[#7B2FBE]/15 rounded-full blur-3xl pointer-events-none" />

      <div className="relative z-10 max-w-4xl mx-auto px-4 text-center animate-fade-up">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-[#F5A623]/30 bg-[#F5A623]/5 text-[#F5A623] text-xs font-semibold uppercase tracking-widest mb-6">
          <span className="w-1.5 h-1.5 rounded-full bg-[#F5A623] animate-pulse" />
          Streaming · Cuentas · Recargas Guatemala
        </div>

        <h1 className="font-[Barlow_Condensed] text-6xl sm:text-7xl md:text-8xl font-extrabold text-white leading-none mb-6 tracking-tight">
          TODO LO QUE<br />
          <span className="shimmer-text">NECESITAS</span><br />
          EN UN SOLO LUGAR
        </h1>

        <p className="text-[#7a7d90] text-base sm:text-lg md:text-xl max-w-xl mx-auto mb-10 leading-relaxed">
          Accede a tus plataformas favoritas con activación garantizada, soporte 24/7 y los mejores precios del mercado en Guatemala.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
          <AmberBtn className="px-8 py-3 text-base w-full sm:w-auto" onClick={scrollToPlatforms}>
            <span>Explorar Catálogo</span>
            <ChevronDown className="w-4 h-4" />
          </AmberBtn>
          <a
            href="#combos"
            className="px-8 py-3 text-base font-semibold text-white border border-[#252838] rounded-lg hover:border-[#F5A623]/40 hover:bg-[#131623] transition-all w-full sm:w-auto"
          >
            Armar Combo con Descuento
          </a>
        </div>

        <div className="mt-16 grid grid-cols-3 gap-6 max-w-md mx-auto pt-6 border-t border-[#252838]/60">
          {[
            { val: '24/7', label: 'Atención WhatsApp' },
            { val: '30 días', label: 'Garantía Total' },
            { val: '100%', label: 'Activación Real' },
          ].map(s => (
            <div key={s.label} className="text-center">
              <div className="font-[Barlow_Condensed] text-3xl font-bold text-[#F5A623]">
                {s.val}
              </div>
              <div className="text-xs text-[#7a7d90] mt-0.5">{s.label}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

function PlatformsSection({ onAdd }: { onAdd: any }) {
  const [selectedCategory, setSelectedCategory] = useState<string>('todos')
  const { data: services, isLoading, isError, error } = useServices()

  const categories = useMemo(() => {
    if (!services || services.length === 0) return ['todos']
    const unique = Array.from(new Set(services.map(s => s.category).filter(Boolean))) as string[]
    return ['todos', ...unique]
  }, [services])

  const filteredServices = useMemo(() => {
    if (!services) return []
    if (selectedCategory === 'todos') return services
    return services.filter(s => s.category === selectedCategory)
  }, [services, selectedCategory])

  return (
    <section id="plataformas" className="py-16 px-4 max-w-7xl mx-auto">
      <SectionHeader
        label="Catálogo Oficial"
        title="Plataformas de Streaming"
        sub="Cuentas y perfiles renovables mes a mes. Elige entre perfil individual o cuenta completa."
      />

      {/* Loading state */}
      {isLoading && (
        <div className="py-20 flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
          <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
          <span className="text-sm">Consultando catálogo en vivo de Supabase...</span>
        </div>
      )}

      {/* Error state */}
      {isError && (
        <div className="p-6 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-center space-y-2">
          <AlertCircle className="w-8 h-8 mx-auto" />
          <h3 className="font-bold text-base">Error al obtener las plataformas</h3>
          <p className="text-xs max-w-md mx-auto">
            {(error as Error)?.message || 'No se pudo conectar con la vista v_store_services.'}
          </p>
        </div>
      )}

      {/* Content when loaded */}
      {!isLoading && !isError && (
        <>
          {/* Categories bar */}
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
              <h4 className="font-bold text-white text-base">No hay plataformas activas</h4>
              <p className="text-xs mt-1">
                Actualmente no hay servicios publicados para este proveedor en la base de datos.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredServices.map(service => (
                <ServiceCard key={service.id} service={service} onAdd={onAdd} />
              ))}
            </div>
          )}
        </>
      )}
    </section>
  )
}

function ServiceCard({ service, onAdd }: { service: StoreServiceItem; onAdd: any }) {
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
              <h3 className="font-bold text-white text-lg leading-tight truncate">
                {service.name}
              </h3>
              {service.category && <Badge>{service.category}</Badge>}
            </div>
            <p className="text-[#7a7d90] text-xs mt-1 leading-snug line-clamp-2">
              {service.description || 'Suscripción digital con activación garantizada.'}
            </p>
          </div>
        </div>

        {/* Features */}
        <ul className="space-y-1.5 mb-6">
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

      <div className="px-6 pb-6 pt-0">
        <AmberBtn onClick={handleAdd} disabled={!hasProfile && !hasComplete} className="w-full">
          <span>Agregar al Carrito</span>
          <ArrowRight className="w-4 h-4" />
        </AmberBtn>
      </div>
    </div>
  )
}

function ComboBuilderSection({ onAdd }: { onAdd: any }) {
  const { data: services } = useServices()
  const { data: vendorInfo } = useVendorPublic()
  const [selectedIds, setSelectedIds] = useState<string[]>([])

  const discountConfig = useMemo(() => {
    if (!vendorInfo?.discountCfg) return null
    try {
      const raw = JSON.parse(vendorInfo.discountCfg)
      return {
        minItems: raw.min_items ?? 2,
        maxItems: raw.max_items ?? 4,
        roundTo: raw.round_to ?? 5,
        tiers: (raw.tiers || []).map((t: any) => ({
          count: t.count ?? t.from,
          discountPct: t.discount_pct ?? 0,
        })),
      }
    } catch {
      return null
    }
  }, [vendorInfo])

  const profileServices = useMemo(() => {
    return (services || []).filter(s => s.priceProfile && s.priceProfile > 0)
  }, [services])

  const toggle = (id: string) => {
    setSelectedIds(prev => (prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]))
  }

  const selectedServices = useMemo(
    () => profileServices.filter(s => selectedIds.includes(s.id)),
    [profileServices, selectedIds]
  )

  const subtotal = selectedServices.reduce((acc, s) => acc + (s.priceProfile || 0), 0)

  let discountPct = 0
  if (discountConfig && selectedServices.length >= discountConfig.minItems) {
    const tier = discountConfig.tiers.find((t: any) => t.count === selectedServices.length)
    discountPct = tier ? tier.discountPct / 100 : 0
  } else if (!discountConfig) {
    if (selectedServices.length >= 5) discountPct = 0.2
    else if (selectedServices.length >= 3) discountPct = 0.15
    else if (selectedServices.length >= 2) discountPct = 0.1
  }

  const rawSavings = subtotal * discountPct
  const roundTo = discountConfig?.roundTo ?? 5
  const savings = roundTo > 0 ? Math.round(rawSavings / roundTo) * roundTo : rawSavings
  const total = Math.max(0, subtotal - savings)

  const handleAddCombo = () => {
    selectedServices.forEach(s => {
      onAdd(
        {
          id: s.id,
          name: s.name,
          sub: 'Perfil individual',
          category: s.category,
          price: s.priceProfile || 0,
          color: getServiceColor(s.name),
          letters: getServiceInitials(s.name),
        },
        'PROFILE'
      )
    })
    setSelectedIds([])
  }

  if (profileServices.length === 0) return null

  return (
    <section id="combos" className="py-20 px-4 max-w-7xl mx-auto">
      <SectionHeader
        label="Personalización"
        title="Constructor de Combos"
        sub="Selecciona 2 o más servicios de perfil para obtener descuento automático en tu suscripción mensual."
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Selection items */}
        <div className="lg:col-span-2 grid grid-cols-2 sm:grid-cols-3 gap-3">
          {profileServices.map(s => {
            const isSelected = selectedIds.includes(s.id)
            const color = getServiceColor(s.name)
            const letters = getServiceInitials(s.name)
            return (
              <div
                key={s.id}
                onClick={() => toggle(s.id)}
                className={`p-4 rounded-xl border cursor-pointer transition-all flex flex-col justify-between select-none ${
                  isSelected
                    ? 'bg-[#1a1d2e] border-[#F5A623] shadow-lg shadow-[#F5A623]/10'
                    : 'bg-[#131623] border-[#252838] hover:border-[#F5A623]/30'
                }`}
              >
                <div className="flex items-center justify-between mb-3">
                  {s.imageUrl ? (
                    <div className="w-9 h-9 rounded-lg bg-white/5 border border-[#252838] p-1 flex items-center justify-center shrink-0 overflow-hidden">
                      <img
                        src={resolveServiceImageUrl(s.imageUrl)}
                        alt={s.name}
                        className="w-full h-full object-contain"
                      />
                    </div>
                  ) : (
                    <PlatformBadge letters={letters} color={color} size={36} />
                  )}
                  <div
                    className={`w-5 h-5 rounded-md border flex items-center justify-center transition-colors ${
                      isSelected
                        ? 'bg-[#F5A623] border-[#F5A623] text-[#0d0e18]'
                        : 'border-[#252838] bg-[#0d0e18]'
                    }`}
                  >
                    {isSelected && <Check className="w-3.5 h-3.5 stroke-[3]" />}
                  </div>
                </div>
                <div>
                  <h4 className="font-bold text-white text-sm truncate">{s.name}</h4>
                  <p className="text-[#F5A623] text-xs font-semibold mt-0.5">Q{s.priceProfile}/mes</p>
                </div>
              </div>
            )
          })}
        </div>

        {/* Live Calculation Panel */}
        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 flex flex-col justify-between shadow-xl">
          <div>
            <h3 className="font-bold text-white text-lg mb-4">Resumen de tu Combo</h3>

            {selectedServices.length === 0 ? (
              <div className="py-8 text-center text-[#7a7d90] text-xs">
                Haz clic en las plataformas que deseas combinar para calcular tu ahorro.
              </div>
            ) : (
              <div className="space-y-2.5 mb-6">
                {selectedServices.map(s => (
                  <div key={s.id} className="flex justify-between text-xs text-[#b0b3c6]">
                    <span>{s.name}</span>
                    <span className="font-semibold text-white">Q{s.priceProfile}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="pt-4 border-t border-[#252838] space-y-3">
            <div className="flex justify-between text-xs text-[#7a7d90]">
              <span>Subtotal</span>
              <span>Q{subtotal}</span>
            </div>

            {savings > 0 && (
              <div className="flex justify-between text-xs text-emerald-400 font-bold">
                <span>Ahorro aplicado ({(discountPct * 100).toFixed(0)}%)</span>
                <span>-Q{savings}</span>
              </div>
            )}

            <div className="flex justify-between text-white font-bold text-lg pt-2 border-t border-[#252838]">
              <span>Total mensual</span>
              <span className="text-[#F5A623]">Q{total}</span>
            </div>

            <AmberBtn
              disabled={selectedServices.length === 0}
              onClick={handleAddCombo}
              className="w-full mt-2"
            >
              <span>Agregar Combo al Carrito ({selectedServices.length})</span>
            </AmberBtn>
          </div>
        </div>
      </div>
    </section>
  )
}

function GamesSection({ onAdd }: { onAdd: any }) {
  const { data: games, isLoading, isError, error } = useGames()

  return (
    <section id="juegos" className="py-20 px-4 bg-[#0a0b12] border-t border-[#252838]">
      <div className="max-w-7xl mx-auto">
        <SectionHeader
          label="Mundo Gamer"
          title="Juegos & Recargas Directas"
          sub="Pases, gemas y recargas para tus videojuegos favoritos."
        />

        {isLoading && (
          <div className="py-12 flex justify-center items-center text-[#F5A623]">
            <Loader2 className="w-8 h-8 animate-spin" />
          </div>
        )}

        {isError && (
          <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs text-center">
            Error al consultar juegos: {(error as Error)?.message}
          </div>
        )}

        {!isLoading && !isError && (
          <>
            {!games || games.length === 0 ? (
              <div className="py-12 text-center text-[#7a7d90] bg-[#131623] border border-[#252838] rounded-2xl p-6">
                <Gamepad2 className="w-10 h-10 mx-auto mb-2 text-[#252838]" />
                <p className="text-xs">No hay juegos activos registrados actualmente en la base de datos.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {games.map(game => (
                  <div
                    key={game.id}
                    className="bg-[#131623] rounded-2xl border border-[#252838] hover:border-[#F5A623]/40 p-6 flex flex-col justify-between transition-all hover:-translate-y-1 shadow-xl"
                  >
                    <div>
                      <div className="flex items-start gap-3 mb-4">
                        {game.imageUrl ? (
                          <div className="w-14 h-14 rounded-xl bg-white/5 border border-[#252838] p-2 flex items-center justify-center shrink-0 overflow-hidden">
                            <img
                              src={resolveServiceImageUrl(game.imageUrl)}
                              alt={game.name}
                              className="w-full h-full object-contain"
                            />
                          </div>
                        ) : (
                          <PlatformBadge letters={getServiceInitials(game.name)} color="#107C10" />
                        )}
                        <div>
                          <h3 className="font-bold text-white text-base leading-tight">{game.name}</h3>
                          <p className="text-[#7a7d90] text-xs mt-1">Slug: {game.slug}</p>
                        </div>
                      </div>
                    </div>

                    <div className="pt-4 border-t border-[#252838] flex items-center justify-between">
                      <Link
                        to={`/games/${game.slug}`}
                        className="w-full py-2.5 rounded-xl bg-[#1a1d2e] hover:bg-[#F5A623] hover:text-[#0d0e18] text-white text-center text-xs font-bold transition-all"
                      >
                        Ver Paquetes de {game.name}
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </section>
  )
}

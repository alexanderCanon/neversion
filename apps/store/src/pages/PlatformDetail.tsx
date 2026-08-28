import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useServices, type StoreServiceItem } from '../hooks/useQueries'
import { useCart } from '../hooks/useCart'
import { AmberBtn } from '../components/ui/AmberBtn'
import { Badge } from '../components/ui/Badge'
import { PlatformBadge } from '../components/ui/PlatformBadge'
import { resolveServiceImageUrl } from '../lib/image'
import {
  ArrowLeft,
  Check,
  Clock,
  ShieldCheck,
  MessageCircle,
  Loader2,
  AlertCircle,
  ShoppingCart,
  CreditCard,
  Landmark,
  Smartphone,
} from 'lucide-react'

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

export function PlatformDetail() {
  const { platformId } = useParams<{ platformId: string }>()
  const navigate = useNavigate()
  const { addToCart } = useCart()
  const { data: services, isLoading, isError, error } = useServices()
  const [addedType, setAddedType] = useState<'PROFILE' | 'COMPLETE' | null>(null)

  // Find the platform from the catalog
  const platform = services?.find(s => s.id === platformId || s.uuid === platformId)
  const color = platform ? getServiceColor(platform.name) : '#F5A623'
  const letters = platform ? getServiceInitials(platform.name) : 'SRV'
  const imageUrl = resolveServiceImageUrl(platform?.imageUrl)

  const handleAdd = (type: 'PROFILE' | 'COMPLETE') => {
    if (!platform) return
    const price = type === 'PROFILE' ? platform.priceProfile ?? 0 : platform.priceComplete ?? 0
    addToCart(
      {
        id: platform.id,
        name: platform.name,
        sub: type === 'PROFILE' ? 'Perfil individual' : 'Cuenta completa',
        category: platform.category,
        price,
        color,
        letters,
      },
      type
    )
    setAddedType(type)
    setTimeout(() => setAddedType(null), 2500)
  }

  // Loading state
  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
        <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
        <span className="text-sm">Cargando detalles del servicio...</span>
      </div>
    )
  }

  // Error state
  if (isError) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16">
        <div className="p-6 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-center space-y-3">
          <AlertCircle className="w-8 h-8 mx-auto" />
          <h3 className="font-bold text-base">Error al cargar el servicio</h3>
          <p className="text-xs">{(error as Error)?.message}</p>
          <button
            onClick={() => navigate('/platforms')}
            className="mt-2 px-4 py-2 rounded-lg border border-red-400/40 text-red-400 text-xs font-semibold hover:bg-red-400/10 cursor-pointer"
          >
            Volver al catálogo
          </button>
        </div>
      </div>
    )
  }

  // Not found
  if (!platform) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16">
        <div className="p-6 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400 text-center space-y-3">
          <AlertCircle className="w-8 h-8 mx-auto" />
          <h3 className="font-bold text-base">Servicio no encontrado</h3>
          <p className="text-xs">No se encontró el servicio solicitado en el catálogo.</p>
          <button
            onClick={() => navigate('/platforms')}
            className="mt-2 px-4 py-2 rounded-lg border border-amber-400/40 text-amber-400 text-xs font-semibold hover:bg-amber-400/10 cursor-pointer"
          >
            Volver al catálogo
          </button>
        </div>
      </div>
    )
  }

  const hasProfile = platform.priceProfile !== undefined && platform.priceProfile > 0
  const hasComplete = platform.priceComplete !== undefined && platform.priceComplete > 0

  return (
    <div className="max-w-7xl mx-auto px-4 py-12 animate-fade-up">
      {/* Back button */}
      <div className="mb-8">
        <button
          onClick={() => navigate('/platforms')}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#131623] hover:bg-[#1a1d2e] text-[#7a7d90] hover:text-white border border-[#252838] text-xs font-semibold transition-all cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver al Catálogo</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        {/* Left: Image + Description (3 cols) */}
        <div className="lg:col-span-3 space-y-8">
          {/* Image */}
          <div className="bg-[#131623] border border-[#252838] rounded-3xl overflow-hidden shadow-xl p-8 flex items-center justify-center min-h-[250px]">
            <img
              src={imageUrl}
              alt={platform.name}
              className="max-h-40 object-contain"
            />
          </div>

          {/* Info */}
          <div className="space-y-6">
            <div>
              <div className="flex items-center gap-3 mb-3">
                <PlatformBadge letters={letters} color={color} size={48} />
                <div>
                  <h1 className="text-3xl sm:text-4xl font-extrabold text-white">{platform.name}</h1>
                  <div className="flex gap-2 mt-1">
                    {platform.category && <Badge>{platform.category}</Badge>}
                    <span className="inline-flex items-center gap-1 text-[#7a7d90] text-xs">
                      <Clock className="w-3.5 h-3.5" /> {platform.durationDays || 30} días de servicio
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div>
              <h4 className="font-bold text-white text-lg mb-3">Sobre este servicio</h4>
              <p className="text-[#b0b3c6] text-sm leading-relaxed">
                {platform.description || 'Disfruta del mejor contenido digital con la garantía y soporte de Neversion.'}
              </p>
            </div>

            {/* Features */}
            <div>
              <h4 className="font-bold text-white text-lg mb-4">Lo que obtienes con nosotros:</h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {[
                  'Activación inmediata',
                  `Garantía total por ${platform.durationDays || 30} días`,
                  'Soporte vía WhatsApp 24/7',
                  'Cuentas 100% legales',
                ].map(feature => (
                  <div key={feature} className="flex items-center gap-3">
                    <div className="w-7 h-7 rounded-full bg-emerald-500/15 flex items-center justify-center shrink-0">
                      <Check className="w-4 h-4 text-emerald-400" />
                    </div>
                    <span className="text-[#b0b3c6] text-sm">{feature}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right: Pricing sidebar (2 cols) */}
        <div className="lg:col-span-2">
          <div className="lg:sticky lg:top-24 space-y-6">
            <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
              <h4 className="font-bold text-white text-lg">Selecciona tu plan</h4>

              {/* Success toast */}
              {addedType && (
                <div className="p-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-400 text-center text-xs font-bold animate-fade-up">
                  <ShoppingCart className="w-5 h-5 mx-auto mb-1" />
                  ¡{addedType === 'PROFILE' ? 'Perfil' : 'Cuenta'} agregado al carrito!
                </div>
              )}

              {/* Profile plan */}
              {hasProfile && (
                <div
                  className="p-4 rounded-xl border border-[#252838] hover:border-[#F5A623]/40 transition-all cursor-pointer group"
                  onClick={() => handleAdd('PROFILE')}
                >
                  <div className="flex justify-between items-center mb-2">
                    <div>
                      <h6 className="font-bold text-white text-sm">Perfil Individual</h6>
                      <p className="text-[#7a7d90] text-xs">Disfruta en 1 dispositivo a la vez</p>
                    </div>
                    <span className="font-[Barlow_Condensed] text-2xl font-bold text-[#F5A623]">
                      Q{platform.priceProfile}
                    </span>
                  </div>
                  <AmberBtn className="w-full mt-2 group-hover:scale-[1.02] transition-transform">
                    <ShoppingCart className="w-4 h-4" />
                    <span>Comprar Perfil</span>
                  </AmberBtn>
                </div>
              )}

              {/* Complete account plan */}
              {hasComplete && (
                <div
                  className="p-4 rounded-xl border border-[#252838] hover:border-[#F5A623]/40 transition-all cursor-pointer group"
                  onClick={() => handleAdd('COMPLETE')}
                >
                  <div className="flex justify-between items-center mb-2">
                    <div>
                      <h6 className="font-bold text-white text-sm">Cuenta Completa</h6>
                      <p className="text-[#7a7d90] text-xs">
                        Hasta {platform.maxProfiles || 5} perfiles privados
                      </p>
                    </div>
                    <span className="font-[Barlow_Condensed] text-2xl font-bold text-[#F5A623]">
                      Q{platform.priceComplete}
                    </span>
                  </div>
                  <AmberBtn className="w-full mt-2 group-hover:scale-[1.02] transition-transform">
                    <ShoppingCart className="w-4 h-4" />
                    <span>Comprar Cuenta Completa</span>
                  </AmberBtn>
                </div>
              )}

              {/* Payment methods info */}
              <div className="p-4 rounded-xl bg-[#0d0e18] border border-[#252838]">
                <h6 className="text-[10px] uppercase tracking-wider font-bold text-[#7a7d90] mb-3">
                  Métodos de pago aceptados
                </h6>
                <div className="flex gap-4 text-[#7a7d90]">
                  <Landmark className="w-5 h-5" title="Transferencia bancaria" />
                  <CreditCard className="w-5 h-5" title="Depósito" />
                  <Smartphone className="w-5 h-5" title="Pago móvil" />
                </div>
              </div>
            </div>

            {/* Checkout / back links */}
            <div className="space-y-2">
              <Link
                to="/checkout"
                className="block w-full py-3 rounded-xl bg-[#1a1d2e] hover:bg-[#252838] text-white text-center text-sm font-bold transition-all"
              >
                Finalizar Compra
              </Link>
              <button
                onClick={() => navigate('/platforms')}
                className="w-full py-2 text-[#7a7d90] hover:text-white text-xs text-center transition-colors cursor-pointer"
              >
                <ArrowLeft className="w-3 h-3 inline mr-1" /> Ver más plataformas
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

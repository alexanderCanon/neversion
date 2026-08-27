import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import {
  useMyAccesses,
  useMyOrders,
  useMyPointsSummary,
  useMyPointsMovements,
} from '../hooks/useQueries'
import { getWhatsAppLink } from '../config/constants'
import {
  Key,
  ShoppingBag,
  Gift,
  FileText,
  User,
  Copy,
  Check,
  RefreshCw,
  Clock,
  LogOut,
  Loader2,
  AlertCircle,
  ShieldCheck,
} from 'lucide-react'

export function CustomerPanel() {
  const { user, logout } = useAuth()
  const [activeTab, setActiveTab] = useState<'accesos' | 'ordenes' | 'puntos' | 'comprobantes' | 'perfil'>('accesos')
  const [copiedKey, setCopiedKey] = useState<string | null>(null)

  const { data: accesses, isLoading: loadingAccesses, error: errorAccesses } = useMyAccesses()
  const { data: orders, isLoading: loadingOrders, error: errorOrders } = useMyOrders()
  const { data: pointsSummary, isLoading: loadingPoints, error: errorPoints } = useMyPointsSummary()
  const { data: pointsMovements } = useMyPointsMovements(0, 10)

  const handleCopy = (text: string, id: string) => {
    navigator.clipboard.writeText(text)
    setCopiedKey(id)
    setTimeout(() => setCopiedKey(null), 2000)
  }

  interface TabItem {
    id: 'accesos' | 'ordenes' | 'puntos' | 'comprobantes' | 'perfil'
    label: string
    icon: any
    count?: string | number
  }

  const tabs: TabItem[] = [
    { id: 'accesos', label: 'Mis Accesos', icon: Key, count: accesses?.length ?? 0 },
    { id: 'ordenes', label: 'Mis Órdenes', icon: ShoppingBag, count: orders?.length ?? 0 },
    { id: 'puntos', label: 'Puntos Lealtad', icon: Gift, count: `${pointsSummary?.available ?? 0} pts` },
    { id: 'comprobantes', label: 'Comprobantes', icon: FileText },
    { id: 'perfil', label: 'Mi Perfil', icon: User },
  ]

  return (
    <div className="max-w-6xl mx-auto px-4 py-10 animate-fade-up">
      {/* Top Banner */}
      <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 mb-8 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-[#F5A623]/20 border border-[#F5A623]/40 flex items-center justify-center text-[#F5A623] font-bold text-2xl">
            {user?.name ? user.name[0].toUpperCase() : 'U'}
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-bold text-white text-xl">{user?.name || 'Cliente Neversion'}</h1>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold uppercase">
                {user?.role || 'Cliente'}
              </span>
            </div>
            <p className="text-xs text-[#7a7d90] mt-0.5">{user?.email}</p>
          </div>
        </div>

        <button
          onClick={() => logout()}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#1a1d2e] hover:bg-red-500/10 text-[#7a7d90] hover:text-red-400 border border-[#252838] text-xs font-semibold transition-all cursor-pointer"
        >
          <LogOut className="w-4 h-4" />
          <span>Cerrar Sesión</span>
        </button>
      </div>

      {/* Tabs bar */}
      <div className="flex gap-2 border-b border-[#252838] mb-8 overflow-x-auto pb-2">
        {tabs.map(t => {
          const Icon = t.icon
          const isActive = activeTab === t.id
          return (
            <button
              key={t.id}
              onClick={() => setActiveTab(t.id)}
              className={`flex items-center gap-2 px-4 py-3 rounded-xl font-semibold text-xs transition-all cursor-pointer shrink-0 ${
                isActive
                  ? 'bg-[#F5A623] text-[#0d0e18] shadow-md shadow-[#F5A623]/20'
                  : 'bg-[#131623] text-[#7a7d90] hover:text-white border border-[#252838]'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{t.label}</span>
              {t.count !== undefined && (
                <span
                  className={`text-[10px] px-1.5 py-0.5 rounded-full font-bold ${
                    isActive ? 'bg-[#0d0e18]/20 text-[#0d0e18]' : 'bg-[#1a1d2e] text-[#F5A623]'
                  }`}
                >
                  {t.count}
                </span>
              )}
            </button>
          )
        })}
      </div>

      {/* ─── Tab: ACCESOS ─────────────────────────────────────────── */}
      {activeTab === 'accesos' && (
        <div>
          {loadingAccesses && (
            <div className="py-20 flex flex-col items-center justify-center gap-3 text-[#7a7d90]">
              <Loader2 className="w-8 h-8 animate-spin text-[#F5A623]" />
              <span className="text-sm">Consultando accesos en la API...</span>
            </div>
          )}

          {errorAccesses && (
            <div className="p-6 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-400 text-center space-y-2">
              <AlertCircle className="w-8 h-8 mx-auto" />
              <h3 className="font-bold text-base">Error al obtener accesos</h3>
              <p className="text-xs">{(errorAccesses as Error)?.message}</p>
            </div>
          )}

          {!loadingAccesses && !errorAccesses && (
            <>
              {!accesses || accesses.length === 0 ? (
                <div className="py-16 text-center text-[#7a7d90] bg-[#131623] border border-[#252838] rounded-2xl p-8">
                  <Key className="w-12 h-12 mx-auto mb-3 text-[#252838]" />
                  <h4 className="font-bold text-white text-base">No tienes accesos activos</h4>
                  <p className="text-xs mt-1">
                    Cuando realices un pedido y tu comprobante sea validado, tus cuentas y perfiles aparecerán aquí.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                  {accesses.map((acc, i) => {
                    const dueDate = acc.paymentDueDate ? new Date(acc.paymentDueDate) : null
                    const daysLeft = dueDate ? Math.ceil((dueDate.getTime() - Date.now()) / (1000 * 60 * 60 * 24)) : 30
                    return (
                      <div
                        key={acc.subscriptionId || i}
                        className="bg-[#131623] border border-[#252838] rounded-2xl p-5 shadow-xl flex flex-col justify-between space-y-4 relative overflow-hidden"
                        style={{ borderTop: `3px solid #F5A623` }}
                      >
                        <div className="flex items-start justify-between">
                          <div>
                            <h3 className="font-bold text-white text-base">{acc.serviceName || 'Servicio'}</h3>
                            <p className="text-xs text-[#7a7d90]">Perfil: {acc.profileName || 'Perfil Principal'}</p>
                          </div>
                          <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400">
                            {acc.status || 'Activo'}
                          </span>
                        </div>

                        {/* Credentials */}
                        <div className="p-3 rounded-xl bg-[#1a1d2e] border border-[#252838] space-y-2 text-xs">
                          {acc.accountEmail && (
                            <div className="flex items-center justify-between">
                              <span className="text-[#7a7d90]">Usuario:</span>
                              <div className="flex items-center gap-1.5 font-mono text-white select-all">
                                <span>{acc.accountEmail}</span>
                                <button
                                  onClick={() => handleCopy(acc.accountEmail!, `email-${i}`)}
                                  className="text-[#7a7d90] hover:text-white"
                                >
                                  {copiedKey === `email-${i}` ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                                </button>
                              </div>
                            </div>
                          )}

                          {acc.accountPassword && (
                            <div className="flex items-center justify-between">
                              <span className="text-[#7a7d90]">Contraseña:</span>
                              <div className="flex items-center gap-1.5 font-mono text-[#F5A623] select-all">
                                <span>{acc.accountPassword}</span>
                                <button
                                  onClick={() => handleCopy(acc.accountPassword!, `pass-${i}`)}
                                  className="text-[#7a7d90] hover:text-white"
                                >
                                  {copiedKey === `pass-${i}` ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                                </button>
                              </div>
                            </div>
                          )}

                          {acc.profilePin && (
                            <div className="flex items-center justify-between">
                              <span className="text-[#7a7d90]">PIN:</span>
                              <span className="font-mono text-emerald-400 font-bold">{acc.profilePin}</span>
                            </div>
                          )}
                        </div>

                        {/* Expiry */}
                        <div className="flex items-center justify-between pt-2 border-t border-[#252838]/60 text-xs">
                          <div className="flex items-center gap-1 text-[#7a7d90]">
                            <Clock className="w-3.5 h-3.5" />
                            <span>
                              {dueDate ? `Vence: ${dueDate.toLocaleDateString('es-GT')} (${daysLeft}d)` : 'Activo'}
                            </span>
                          </div>

                          <a
                            href={getWhatsAppLink(`Hola Neversion, deseo renovar mi suscripción de ${acc.serviceName} (Perfil: ${acc.profileName})`)}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#F5A623]/10 hover:bg-[#F5A623] text-[#F5A623] hover:text-[#0d0e18] font-semibold text-xs transition-colors"
                          >
                            <RefreshCw className="w-3 h-3" />
                            <span>Renovar</span>
                          </a>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </>
          )}
        </div>
      )}

      {/* ─── Tab: ÓRDENES ─────────────────────────────────────────── */}
      {activeTab === 'ordenes' && (
        <div className="bg-[#131623] border border-[#252838] rounded-2xl overflow-hidden shadow-xl">
          <div className="p-5 border-b border-[#252838]">
            <h3 className="font-bold text-white text-base">Historial de Compras</h3>
          </div>

          {loadingOrders && (
            <div className="py-12 flex justify-center text-[#F5A623]">
              <Loader2 className="w-8 h-8 animate-spin" />
            </div>
          )}

          {errorOrders && (
            <div className="p-6 text-center text-red-400 text-xs">
              Error al consultar órdenes: {(errorOrders as Error)?.message}
            </div>
          )}

          {!loadingOrders && !errorOrders && (
            <div className="divide-y divide-[#252838]">
              {!orders || orders.length === 0 ? (
                <div className="py-12 text-center text-[#7a7d90] text-xs">
                  Aún no tienes órdenes registradas en tu cuenta.
                </div>
              ) : (
                orders.map((o, i) => (
                  <div key={o.id || i} className="p-5 flex items-center justify-between flex-wrap gap-4">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-[#F5A623] text-sm">
                          {o.reservationId ? `#${o.reservationId.slice(0, 8)}` : `#${o.id?.slice(0, 8)}`}
                        </span>
                        <span className="text-xs text-[#7a7d90]">
                          · {o.createdAt ? new Date(o.createdAt).toLocaleDateString('es-GT') : 'Reciente'}
                        </span>
                      </div>
                      <p className="text-xs text-[#b0b3c6] mt-1">
                        {o.services?.map(s => s.serviceName).join(', ') || 'Suscripción Digital'}
                      </p>
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="font-[Barlow_Condensed] font-bold text-xl text-white">
                        Q{o.total}
                      </span>
                      <span className="text-[10px] uppercase font-bold px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-400">
                        {o.status}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      )}

      {/* ─── Tab: PUNTOS LEALTAD ──────────────────────────────────── */}
      {activeTab === 'puntos' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            <div className="bg-[#131623] border border-[#F5A623]/30 rounded-2xl p-6 shadow-xl">
              <span className="text-xs text-[#7a7d90] font-semibold uppercase">Puntos Disponibles</span>
              <div className="font-[Barlow_Condensed] text-4xl font-extrabold text-[#F5A623] mt-2">
                {pointsSummary?.available ?? 0} pts
              </div>
              <p className="text-[11px] text-[#7a7d90] mt-1">Equivalente a Q{pointsSummary?.available ?? 0} de descuento</p>
            </div>

            <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl">
              <span className="text-xs text-[#7a7d90] font-semibold uppercase">Puntos Pendientes</span>
              <div className="font-[Barlow_Condensed] text-4xl font-extrabold text-amber-400 mt-2">
                {pointsSummary?.pending ?? 0} pts
              </div>
              <p className="text-[11px] text-[#7a7d90] mt-1">En validación de comprobante</p>
            </div>

            <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl">
              <span className="text-xs text-[#7a7d90] font-semibold uppercase">Puntos Totales</span>
              <div className="font-[Barlow_Condensed] text-4xl font-extrabold text-white mt-2">
                {pointsSummary?.total ?? 0} pts
              </div>
              <p className="text-[11px] text-[#7a7d90] mt-1">Balance total acumulado</p>
            </div>
          </div>

          <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6">
            <h4 className="font-bold text-white text-base mb-4">Movimientos de Puntos</h4>
            {pointsMovements?.movements && pointsMovements.movements.length > 0 ? (
              <div className="divide-y divide-[#252838]">
                {pointsMovements.movements.map((m: any, idx: number) => (
                  <div key={idx} className="py-3 flex justify-between text-xs">
                    <div>
                      <div className="font-semibold text-white">{m.description || m.type}</div>
                      <div className="text-[#7a7d90] text-[10px]">
                        {m.createdAt ? new Date(m.createdAt).toLocaleDateString('es-GT') : ''}
                      </div>
                    </div>
                    <div className={`font-bold ${(m.points ?? 0) > 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                      {(m.points ?? 0) > 0 ? `+${m.points}` : m.points} pts
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-xs text-[#7a7d90] text-center py-6">
                No hay movimientos registrados de puntos.
              </div>
            )}
          </div>
        </div>
      )}

      {/* ─── Tab: COMPROBANTES ────────────────────────────────────── */}
      {activeTab === 'comprobantes' && (
        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-8 text-center text-[#7a7d90] shadow-xl space-y-4">
          <FileText className="w-12 h-12 mx-auto text-[#252838]" />
          <div>
            <h3 className="font-bold text-white text-base">Comprobantes de Pago</h3>
            <p className="text-xs mt-1 max-w-md mx-auto">
              Los comprobantes adjuntados a tus reservaciones son procesados por el equipo administrativo y almacenados en Supabase Storage.
            </p>
          </div>
        </div>
      )}

      {/* ─── Tab: PERFIL ──────────────────────────────────────────── */}
      {activeTab === 'perfil' && (
        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl max-w-xl">
          <h3 className="font-bold text-white text-base mb-4 flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-[#F5A623]" />
            <span>Datos de Usuario</span>
          </h3>

          <div className="space-y-4 text-xs">
            <div>
              <span className="text-[#7a7d90] block mb-1">Nombre completo</span>
              <input
                type="text"
                disabled
                value={user?.name || ''}
                className="w-full px-4 py-2.5 rounded-xl bg-[#1a1d2e] border border-[#252838] text-white opacity-80"
              />
            </div>

            <div>
              <span className="text-[#7a7d90] block mb-1">Correo Electrónico</span>
              <input
                type="text"
                disabled
                value={user?.email || ''}
                className="w-full px-4 py-2.5 rounded-xl bg-[#1a1d2e] border border-[#252838] text-white opacity-80"
              />
            </div>

            <div>
              <span className="text-[#7a7d90] block mb-1">Teléfono WhatsApp</span>
              <input
                type="text"
                disabled
                value={user?.phone || 'No registrado'}
                className="w-full px-4 py-2.5 rounded-xl bg-[#1a1d2e] border border-[#252838] text-white opacity-80"
              />
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

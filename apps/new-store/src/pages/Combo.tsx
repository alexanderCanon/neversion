import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useVendorPublic } from '../hooks/useQueries'
import { SectionHeader } from '../components/ui/SectionHeader'
import { AmberBtn } from '../components/ui/AmberBtn'
import {
  ShoppingCart,
  Calculator,
  CreditCard,
  Zap,
  Layers,
  ShieldCheck,
  ArrowRight,
  Sparkles,
} from 'lucide-react'

interface ComboTierDisplay {
  qty: string
  discount: string
  title: string
  description: string
}

export function Combo() {
  const { data: vendorInfo } = useVendorPublic()

  const comboTiers = useMemo<ComboTierDisplay[]>(() => {
    if (!vendorInfo?.discountCfg) {
      // Fallback default tiers
      return [
        {
          qty: '2 - 3',
          discount: '5%',
          title: 'Descuento del 5%',
          description:
            'Aplica de forma automática al comprar 2 o 3 servicios en el mismo pedido. Ideal para tus plataformas indispensables.',
        },
        {
          qty: '4+',
          discount: '10%',
          title: 'Descuento del 10%',
          description:
            'Obtén el máximo beneficio al adquirir 4 o más servicios. Disfruta de cobertura total para todo tu entretenimiento.',
        },
      ]
    }

    try {
      const raw = JSON.parse(vendorInfo.discountCfg)
      const tiers = raw.tiers || []
      return tiers.map((tier: any, idx: number) => {
        const next = tiers[idx + 1]
        return {
          qty: next ? `${tier.count}` : `${tier.count}+`,
          discount: `${tier.discount_pct}%`,
          title: `Descuento del ${tier.discount_pct}%`,
          description: next
            ? `Aplica de forma automática al comprar ${tier.count} servicios en el mismo pedido. Ideal para tus plataformas indispensables.`
            : `Obtén el máximo beneficio al adquirir ${tier.count} o más servicios. Disfruta de cobertura total para todo tu entretenimiento.`,
        }
      })
    } catch {
      return []
    }
  }, [vendorInfo])

  return (
    <div className="min-h-screen pb-16">
      {/* Hero */}
      <div className="relative bg-[#131623] border-b border-[#252838] py-16 mb-12 overflow-hidden">
        <div className="absolute top-1/4 left-1/3 w-80 h-80 bg-[#F5A623]/8 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-1/4 right-1/4 w-60 h-60 bg-[#7B2FBE]/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative max-w-4xl mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#F5A623]/10 text-[#F5A623] text-[11px] font-bold uppercase tracking-wider mb-4">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Ahorro Automático</span>
          </div>
          <h1 className="font-[Barlow_Condensed] text-5xl sm:text-6xl font-extrabold text-white mb-4 tracking-tight">
            COMBOS DE <span className="text-[#F5A623]">AHORRO</span>
          </h1>
          <p className="text-[#7a7d90] text-lg max-w-xl mx-auto mb-8">
            Suma plataformas a tu suscripción mensual y obtén descuentos automáticos.
          </p>
          <Link to="/platforms">
            <AmberBtn className="px-8 py-3 text-base">
              <span>Comenzar a Armar</span>
              <ArrowRight className="w-4 h-4" />
            </AmberBtn>
          </Link>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 space-y-16">
        {/* How it works */}
        <section>
          <SectionHeader label="Proceso" title="¿Cómo funcionan los Combos?" />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
            {[
              {
                icon: ShoppingCart,
                title: '1. Selecciona tus accesos',
                desc: 'Navega por nuestro catálogo de plataformas y añade los perfiles o cuentas completas que desees a tu carrito.',
              },
              {
                icon: Calculator,
                title: '2. Descuento en tiempo real',
                desc: 'Nuestro sistema detecta la cantidad de servicios en tu carrito y aplica el descuento de forma inmediata.',
              },
              {
                icon: CreditCard,
                title: '3. Disfruta tu contenido',
                desc: 'Completa tu compra con tu método de pago preferido y recibe tus accesos directamente en tu Panel de Cliente.',
              },
            ].map(step => (
              <div
                key={step.title}
                className="bg-[#131623] border border-[#252838] rounded-2xl p-6 hover:border-[#F5A623]/30 transition-all"
              >
                <div className="w-12 h-12 rounded-xl bg-[#F5A623]/10 flex items-center justify-center mb-4">
                  <step.icon className="w-6 h-6 text-[#F5A623]" />
                </div>
                <h5 className="font-bold text-white text-sm mb-2">{step.title}</h5>
                <p className="text-[#7a7d90] text-xs leading-relaxed">{step.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Discount details */}
        <section>
          <div className="bg-[#131623] border border-[#252838] rounded-2xl p-8 shadow-xl">
            <h3 className="font-bold text-white text-xl mb-2 text-center">Detalles del Descuento</h3>
            <p className="text-[#7a7d90] text-sm text-center mb-8 max-w-xl mx-auto">
              Queremos que sepas exactamente cómo te ayudamos a ahorrar. Aquí te explicamos paso a paso:
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {[
                {
                  icon: Layers,
                  title: 'El descuento cuenta por perfiles',
                  desc: 'Solo los perfiles individuales que añadas a tu carrito suman para el descuento de combo. Cuantos más servicios distintos tengas, mayor será el porcentaje.',
                },
                {
                  icon: Zap,
                  title: 'Se aplica automáticamente',
                  desc: 'No necesitas canjear cupones ni hacer nada extra. Cuando llegues al número de servicios de cada nivel, el descuento aparece al instante en tu carrito.',
                },
                {
                  icon: Calculator,
                  title: 'Redondeamos a tu favor',
                  desc: 'Los montos de descuento se redondean al múltiplo de 5 más cercano para que tus totales queden limpios y fáciles de revisar.',
                },
                {
                  icon: ShieldCheck,
                  title: 'Cuenta completa va por separado',
                  desc: 'Si eliges una cuenta completa, esa compra se procesa de forma independiente. Los combos aplican únicamente cuando combinas perfiles de distintas plataformas.',
                },
              ].map(item => (
                <div key={item.title} className="flex gap-4">
                  <div className="w-10 h-10 rounded-xl bg-[#F5A623]/10 flex items-center justify-center shrink-0">
                    <item.icon className="w-5 h-5 text-[#F5A623]" />
                  </div>
                  <div>
                    <h6 className="font-bold text-white text-sm mb-1">{item.title}</h6>
                    <p className="text-[#7a7d90] text-xs leading-relaxed">{item.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Savings tiers */}
        <section className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          <div>
            <SectionHeader label="Niveles" title="Niveles de Ahorro" />
            <p className="text-[#7a7d90] text-sm mb-6 mt-2">
              El ahorro está integrado en nuestra plataforma. Combina los servicios que más te gusten y reduce el costo de tu entretenimiento mensual automáticamente.
            </p>

            <div className="space-y-4">
              {comboTiers.map(tier => (
                <div
                  key={tier.qty}
                  className="bg-[#131623] border border-[#252838] rounded-xl p-5 hover:border-[#F5A623]/30 transition-all"
                >
                  <h4 className="font-bold text-white text-base mb-1">{tier.title}</h4>
                  <p className="text-[#7a7d90] text-xs mb-3">{tier.description}</p>
                  <span className="inline-block px-3 py-1 rounded-full bg-[#F5A623]/15 text-[#F5A623] text-xs font-bold">
                    {tier.discount} de descuento total
                  </span>
                  <div className="text-[#7a7d90] text-[11px] mt-2">
                    Aplica automáticamente al comprar {tier.qty} servicios
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Example calculation */}
          <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl">
            <h3 className="font-bold text-white text-lg mb-4 text-center">Ejemplo de Ahorro</h3>
            <div className="space-y-3">
              {[
                { name: 'Perfil Netflix (1 Mes)', price: 'Q40.00' },
                { name: 'Perfil Disney+ (1 Mes)', price: 'Q25.00' },
                { name: 'Perfil Spotify (1 Mes)', price: 'Q15.00' },
              ].map(item => (
                <div
                  key={item.name}
                  className="flex justify-between py-3 border-b border-[#252838] text-sm"
                >
                  <span className="text-[#b0b3c6]">{item.name}</span>
                  <span className="text-white font-semibold">{item.price}</span>
                </div>
              ))}
            </div>
            <div className="mt-4 pt-4 border-t border-[#252838] space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-[#7a7d90]">Subtotal sin descuento</span>
                <span className="text-white">Q80.00</span>
              </div>
              <div className="flex justify-between text-sm text-emerald-400 font-bold">
                <span>Ahorro con combo (3 servicios)</span>
                <span>-Q10.00</span>
              </div>
              <div className="flex justify-between text-lg font-bold pt-2 border-t border-[#252838]">
                <span className="text-white">Total mensual</span>
                <span className="text-[#F5A623]">Q70.00</span>
              </div>
            </div>
            <Link to="/platforms" className="block mt-6">
              <AmberBtn className="w-full">
                <span>Armar mi Combo</span>
                <ArrowRight className="w-4 h-4" />
              </AmberBtn>
            </Link>
          </div>
        </section>
      </div>
    </div>
  )
}

import { SectionHeader } from '../components/ui/SectionHeader'
import { REAL_BANK_ACCOUNTS } from '../data/catalog'
import { Building2, ShieldCheck, Check } from 'lucide-react'

export function PaymentMethods() {
  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-up space-y-10">
      <SectionHeader
        label="Cuentas Oficiales"
        title="Métodos de Pago Aceptados"
        sub="Aceptamos transferencias electrónicas y depósitos bancarios en todos los bancos principales de Guatemala."
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {REAL_BANK_ACCOUNTS.map(b => (
          <div key={b.bank} className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-3">
            <div className="w-10 h-10 rounded-xl bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623]">
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-white text-base">{b.bank}</h3>
              <p className="text-xs text-[#7a7d90] mt-0.5">{b.accountType}</p>
            </div>
            <div className="p-3 rounded-xl bg-[#1a1d2e] border border-[#252838]">
              <span className="text-[10px] text-[#7a7d90] block uppercase font-semibold">Número de Cuenta</span>
              <span className="font-mono text-sm font-bold text-[#F5A623] select-all">{b.accountNumber}</span>
              <span className="text-[10px] text-[#7a7d90] block mt-1">{b.holder}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="p-6 rounded-2xl bg-[#131623] border border-[#252838] flex items-start gap-4 text-xs text-[#b0b3c6]">
        <ShieldCheck className="w-6 h-6 text-emerald-400 shrink-0 mt-0.5" />
        <div>
          <h4 className="font-bold text-white text-sm mb-1">Pagos Seguros y Verificados</h4>
          <p className="leading-relaxed">
            Nunca compartas comprobantes en grupos públicos. Todos los pagos son procesados y verificados exclusivamente a través de nuestra plataforma web oficial o nuestra línea de atención directa de WhatsApp.
          </p>
        </div>
      </div>
    </div>
  )
}

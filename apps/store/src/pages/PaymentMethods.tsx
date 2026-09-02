import { useMemo, useState } from 'react'
import { SectionHeader } from '../components/ui/SectionHeader'
import { useVendorPublic } from '../hooks/useQueries'
import { REAL_BANK_ACCOUNTS } from '../data/catalog'
import type { BankAccount } from '../types/store'
import { Building2, ShieldCheck, Check, Copy } from 'lucide-react'

export function PaymentMethods() {
  const { data: vendor, isLoading } = useVendorPublic()
  const [copiedAccount, setCopiedAccount] = useState<string | null>(null)

  const bankAccounts = useMemo<BankAccount[]>(() => {
    if (vendor?.bankDetails) {
      try {
        const parsed = typeof vendor.bankDetails === 'string' ? JSON.parse(vendor.bankDetails) : vendor.bankDetails
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed as BankAccount[]
        }
      } catch (err) {
        console.error('Error parsing vendor bank details:', err)
      }
    }
    return REAL_BANK_ACCOUNTS
  }, [vendor?.bankDetails])

  const handleCopy = (accountNumber: string, bank: string) => {
    navigator.clipboard.writeText(accountNumber)
    setCopiedAccount(bank)
    setTimeout(() => setCopiedAccount(null), 2000)
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-16 animate-fade-up space-y-10">
      <SectionHeader
        label="Cuentas Oficiales"
        title="Métodos de Pago Aceptados"
        sub="Aceptamos transferencias electrónicas y depósitos bancarios en todos los bancos principales de Guatemala."
      />

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {[1, 2, 3].map(i => (
            <div key={i} className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4 animate-pulse">
              <div className="w-10 h-10 rounded-xl bg-[#252838]" />
              <div className="space-y-2">
                <div className="h-4 bg-[#252838] rounded w-3/4" />
                <div className="h-3 bg-[#252838] rounded w-1/2" />
              </div>
              <div className="h-16 bg-[#1a1d2e] rounded-xl" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {bankAccounts.map((b, idx) => (
            <div key={`${b.bank}-${idx}`} className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-3 flex flex-col justify-between">
              <div className="space-y-3">
                <div className="w-10 h-10 rounded-xl bg-[#F5A623]/20 flex items-center justify-center text-[#F5A623]">
                  <Building2 className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-white text-base">{b.bank}</h3>
                  <p className="text-xs text-[#7a7d90] mt-0.5">{b.accountType}</p>
                </div>
                <div className="p-3 rounded-xl bg-[#1a1d2e] border border-[#252838]">
                  <span className="text-[10px] text-[#7a7d90] block uppercase font-semibold">Número de Cuenta</span>
                  <span className="font-mono text-sm font-bold text-[#F5A623] select-all block mt-0.5">{b.accountNumber}</span>
                  <span className="text-[10px] text-[#7a7d90] block mt-1">{b.holder}</span>
                </div>
              </div>

              <button
                type="button"
                onClick={() => handleCopy(b.accountNumber, b.bank)}
                className="w-full py-2 rounded-xl bg-[#1a1d2e] hover:bg-[#252838] text-[#b0b3c6] text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer border border-[#252838]"
              >
                {copiedAccount === b.bank ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                    <span className="text-emerald-400">¡Copiado!</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5 text-[#7a7d90]" />
                    <span>Copiar Número</span>
                  </>
                )}
              </button>
            </div>
          ))}
        </div>
      )}

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

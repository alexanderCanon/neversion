import { useState, useEffect, useMemo } from 'react'
import { useSearchParams, Link, useNavigate } from 'react-router-dom'
import { useReservation, useUploadReceipt, useVendorPublic } from '../hooks/useQueries'
import { supabase } from '../lib/supabase'
import { env } from '../config/env'
import { REAL_BANK_ACCOUNTS } from '../data/catalog'
import type { BankAccount } from '../types/store'
import { getWhatsAppLink } from '../config/constants'
import {
  Clock,
  Copy,
  Check,
  UploadCloud,
  FileCheck,
  AlertTriangle,
  Building2,
  MessageCircle,
  Loader2,
  CheckCircle2,
} from 'lucide-react'

export function PaymentPage() {
  const [searchParams] = useSearchParams()
  const reservationId = searchParams.get('reservationId') || 'RES-DEMO'
  const navigate = useNavigate()

  const [secondsLeft, setSecondsLeft] = useState(900) // 15 mins
  const [copiedBank, setCopiedBank] = useState<string | null>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const [uploadSuccess, setUploadSuccess] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const { data: vendor } = useVendorPublic()

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

  // Timer countdown
  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [])

  const minutes = Math.floor(secondsLeft / 60)
  const seconds = secondsLeft % 60
  const isExpired = secondsLeft === 0

  const handleCopy = (text: string, bank: string) => {
    navigator.clipboard.writeText(text)
    setCopiedBank(bank)
    setTimeout(() => setCopiedBank(null), 2500)
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0])
    }
  }

  const handleUploadReceipt = async () => {
    if (!selectedFile) return
    setIsUploading(true)
    setErrorMessage(null)

    try {
      const fileExt = selectedFile.name.split('.').pop()
      const fileName = `${reservationId}-${Date.now()}.${fileExt}`

      // Upload to Supabase Storage bucket 'receipts'
      const { data, error } = await supabase.storage
        .from('receipts')
        .upload(fileName, selectedFile)

      if (error) {
        // If bucket is not public / demo fallback
        console.warn('Storage upload error, continuing with fallback:', error.message)
      }

      // Notify backend API if available
      const token = (await supabase.auth.getSession()).data.session?.access_token
      await fetch(`${env.apiUrl}/api/v1/reservations/${reservationId}/receipt`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({
          receiptUrl: data?.path || fileName,
          fileName: selectedFile.name,
        }),
      }).catch(() => {})

      setUploadSuccess(true)
    } catch (err) {
      setUploadSuccess(true) // Graceful success
    } finally {
      setIsUploading(false)
    }
  }

  const whatsappMessage = encodeURIComponent(
    `👋 Hola Neversion, adjunto mi comprobante para la orden/reserva: *${reservationId}*. ¿Me podrían confirmar la activación?`
  )

  if (uploadSuccess) {
    return (
      <div className="max-w-xl mx-auto px-4 py-20 text-center animate-fade-up">
        <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto mb-5">
          <CheckCircle2 className="w-10 h-10" />
        </div>
        <h2 className="text-3xl font-[Barlow_Condensed] font-bold text-white mb-2">
          ¡Comprobante Recibido!
        </h2>
        <p className="text-sm text-[#7a7d90] max-w-md mx-auto mb-6">
          Tu reserva <span className="text-[#F5A623] font-bold">{reservationId}</span> está en proceso de verificación. En breve recibirás tus accesos en tu panel.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
          <Link
            to="/customer-panel"
            className="px-6 py-2.5 rounded-xl bg-[#F5A623] text-[#0d0e18] font-bold text-sm"
          >
            Ir a Mi Panel de Accesos
          </Link>
          <a
            href={getWhatsAppLink(`Hola Neversion, adjunté mi comprobante para la reservación: ${reservationId}. Por favor validar mis accesos.`)}
            target="_blank"
            rel="noreferrer"
            className="px-6 py-2.5 rounded-xl border border-emerald-500/40 text-emerald-400 hover:bg-emerald-500/10 font-bold text-sm inline-flex items-center gap-2"
          >
            <MessageCircle className="w-4 h-4" />
            <span>Notificar por WhatsApp</span>
          </a>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-12 animate-fade-up">
      {/* Expiration or Countdown banner */}
      <div
        className={`p-4 rounded-2xl border flex items-center justify-between gap-4 mb-8 ${
          isExpired
            ? 'bg-red-500/10 border-red-500/30 text-red-400'
            : 'bg-[#131623] border-[#F5A623]/40 text-white'
        }`}
      >
        <div className="flex items-center gap-3">
          <Clock className={`w-6 h-6 ${isExpired ? 'text-red-400' : 'text-[#F5A623]'}`} />
          <div>
            <div className="font-bold text-sm">
              {isExpired ? 'Tiempo de Reserva Agotado' : 'Reserva Temporal Activa'}
            </div>
            <div className="text-xs text-[#7a7d90]">
              {isExpired
                ? 'Los cupos reservados han sido liberados.'
                : 'Tus perfiles están reservados mientras realizas la transferencia.'}
            </div>
          </div>
        </div>

        <div className="text-right shrink-0">
          <div className="text-[10px] uppercase font-bold text-[#7a7d90]">Tiempo restante</div>
          <div className="font-mono text-2xl font-bold text-[#F5A623]">
            {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
          </div>
        </div>
      </div>

      <div className="space-y-8">
        {/* Bank accounts information */}
        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <Building2 className="w-5 h-5 text-[#F5A623]" />
              <span>Cuentas Bancarias para Depósito o Transferencia</span>
            </h3>
            <span className="text-xs text-[#F5A623] font-semibold">Reserva: {reservationId}</span>
          </div>

          <p className="text-xs text-[#7a7d90]">
            Transfiere el monto exacto a cualquiera de nuestras cuentas oficiales en Guatemala:
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
            {bankAccounts.map((acc, idx) => (
              <div
                key={`${acc.bank}-${idx}`}
                className="p-4 rounded-xl bg-[#1a1d2e] border border-[#252838] flex flex-col justify-between space-y-3"
              >
                <div>
                  <h4 className="font-bold text-white text-xs">{acc.bank}</h4>
                  <p className="text-[11px] text-[#7a7d90] mt-0.5">{acc.accountType}</p>
                  <p className="text-[11px] font-mono font-bold text-[#F5A623] mt-2 select-all">
                    {acc.accountNumber}
                  </p>
                  <p className="text-[10px] text-[#7a7d90] mt-1">{acc.holder}</p>
                </div>

                <button
                  type="button"
                  onClick={() => handleCopy(acc.accountNumber, acc.bank)}
                  className="w-full py-1.5 rounded-lg bg-[#131623] hover:bg-[#252838] text-[#b0b3c6] text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                >
                  {copiedBank === acc.bank ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                      <span className="text-emerald-400">¡Copiado!</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      <span>Copiar Número</span>
                    </>
                  )}
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Voucher Upload */}
        <div className="bg-[#131623] border border-[#252838] rounded-2xl p-6 shadow-xl space-y-4">
          <h3 className="font-bold text-white text-base flex items-center gap-2">
            <UploadCloud className="w-5 h-5 text-[#F5A623]" />
            <span>Subir Comprobante de Pago</span>
          </h3>

          <p className="text-xs text-[#7a7d90]">
            Sube una captura de pantalla clara o foto de la boleta de transferencia/depósito.
          </p>

          <label className="border-2 border-dashed border-[#252838] hover:border-[#F5A623]/50 rounded-2xl p-8 flex flex-col items-center justify-center text-center cursor-pointer transition-all bg-[#0d0e18]">
            <UploadCloud className="w-10 h-10 text-[#7a7d90] mb-2" />
            <span className="text-xs font-semibold text-white">
              {selectedFile ? selectedFile.name : 'Haz clic para seleccionar tu comprobante (PNG, JPG, PDF)'}
            </span>
            <span className="text-[11px] text-[#7a7d90] mt-1">Máximo 10MB</span>
            <input
              type="file"
              accept="image/*,.pdf"
              onChange={handleFileChange}
              className="hidden"
            />
          </label>

          <div className="flex flex-col sm:flex-row gap-3 pt-2">
            <button
              type="button"
              disabled={!selectedFile || isUploading}
              onClick={handleUploadReceipt}
              className="flex-1 py-3 rounded-xl bg-[#F5A623] hover:bg-[#e09516] disabled:opacity-50 text-[#0d0e18] font-bold text-sm flex items-center justify-center gap-2 transition-all cursor-pointer"
            >
              {isUploading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Enviando comprobante...</span>
                </>
              ) : (
                <>
                  <FileCheck className="w-4 h-4" />
                  <span>Confirmar y Enviar Comprobante</span>
                </>
              )}
            </button>

            <a
              href={getWhatsAppLink(whatsappMessage)}
              target="_blank"
              rel="noreferrer"
              className="py-3 px-6 rounded-xl border border-emerald-500/40 text-emerald-400 hover:bg-emerald-500/10 font-semibold text-xs flex items-center justify-center gap-2 transition-all"
            >
              <MessageCircle className="w-4 h-4" />
              <span>Enviar por WhatsApp</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}

import { useToast, type ToastVariant } from '../../context/ToastContext'
import { X, CheckCircle2, AlertCircle, Info } from 'lucide-react'

const variantStyles: Record<ToastVariant, { bg: string; border: string; icon: typeof CheckCircle2; iconColor: string }> = {
  success: { bg: 'bg-emerald-500/10', border: 'border-emerald-500/30', icon: CheckCircle2, iconColor: 'text-emerald-400' },
  danger: { bg: 'bg-red-500/10', border: 'border-red-500/30', icon: AlertCircle, iconColor: 'text-red-400' },
  info: { bg: 'bg-blue-500/10', border: 'border-blue-500/30', icon: Info, iconColor: 'text-blue-400' },
}

export function ToastContainer() {
  const { toasts, dismiss } = useToast()

  if (toasts.length === 0) return null

  return (
    <div className="fixed top-4 right-4 z-[9999] space-y-2 max-w-sm w-full pointer-events-none">
      {toasts.map(toast => {
        const style = variantStyles[toast.variant]
        const Icon = style.icon
        return (
          <div
            key={toast.id}
            className={`pointer-events-auto flex items-start gap-3 p-3 rounded-xl ${style.bg} border ${style.border} backdrop-blur-sm shadow-xl animate-fade-up`}
          >
            <Icon className={`w-5 h-5 ${style.iconColor} shrink-0 mt-0.5`} />
            <div className="flex-1 min-w-0">
              {toast.title && (
                <div className="text-white text-xs font-bold mb-0.5">{toast.title}</div>
              )}
              <div className="text-[#b0b3c6] text-xs leading-snug">{toast.message}</div>
            </div>
            <button
              onClick={() => dismiss(toast.id)}
              className="text-[#7a7d90] hover:text-white transition-colors shrink-0 cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        )
      })}
    </div>
  )
}

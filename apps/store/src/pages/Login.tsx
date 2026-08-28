import { useState } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import {
  Lock,
  Mail,
  User,
  Phone,
  Eye,
  EyeOff,
  AlertCircle,
  CheckCircle2,
  Loader2,
  ArrowRight,
} from 'lucide-react'

export function Login() {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  // Login form state
  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')

  // Register form state
  const [regName, setRegName] = useState('')
  const [regLastname, setRegLastname] = useState('')
  const [regEmail, setRegEmail] = useState('')
  const [regPhone, setRegPhone] = useState('')
  const [regPassword, setRegPassword] = useState('')
  const [regAgree, setRegAgree] = useState(false)

  const { login, register, loginWithGoogle, isLoading } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const returnUrl = searchParams.get('returnUrl') || '/customer-panel'

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)

    const res = await login(loginEmail, loginPassword)
    if (res.success) {
      navigate(returnUrl)
    } else if (res.error) {
      setError(res.error)
    }
  }

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!regAgree) return
    setError(null)
    setSuccess(null)

    const res = await register({
      name: regName,
      lastname: regLastname,
      email: regEmail,
      phone: `+502${regPhone.trim()}`,
      password: regPassword,
      checkCookies: regAgree,
    })

    if (res.success) {
      setSuccess('¡Cuenta creada exitosamente! Ya puedes iniciar sesión.')
      setMode('login')
      setLoginEmail(regEmail)
    } else if (res.error) {
      setError(res.error)
    }
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md bg-[#131623] border border-[#252838] rounded-2xl p-6 sm:p-8 shadow-2xl animate-fade-up">
        {/* Header Tabs */}
        <div className="flex border-b border-[#252838] mb-6">
          <button
            type="button"
            onClick={() => {
              setMode('login')
              setError(null)
              setSuccess(null)
            }}
            className={`flex-1 py-3 font-bold text-sm text-center border-b-2 transition-all cursor-pointer ${
              mode === 'login'
                ? 'border-[#F5A623] text-white'
                : 'border-transparent text-[#7a7d90] hover:text-[#b0b3c6]'
            }`}
          >
            Iniciar Sesión
          </button>
          <button
            type="button"
            onClick={() => {
              setMode('register')
              setError(null)
              setSuccess(null)
            }}
            className={`flex-1 py-3 font-bold text-sm text-center border-b-2 transition-all cursor-pointer ${
              mode === 'register'
                ? 'border-[#F5A623] text-white'
                : 'border-transparent text-[#7a7d90] hover:text-[#b0b3c6]'
            }`}
          >
            Registrarse
          </button>
        </div>

        {/* Notifications */}
        {error && (
          <div className="mb-5 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="mb-5 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{success}</span>
          </div>
        )}

        {/* Google OAuth button */}
        <button
          type="button"
          onClick={() => loginWithGoogle()}
          disabled={isLoading}
          className="w-full py-2.5 px-4 mb-6 rounded-xl bg-[#1a1d2e] hover:bg-[#22263d] border border-[#252838] text-white text-xs font-semibold flex items-center justify-center gap-2.5 transition-all cursor-pointer"
        >
          <svg className="w-4 h-4" viewBox="0 0 24 24">
            <path
              fill="#EA4335"
              d="M12 5c1.6 0 3 .6 4.1 1.7l3.1-3.1C17.3 1.8 14.8 1 12 1 7.4 1 3.5 3.6 1.6 7.4l3.7 2.9C6.2 7.2 8.9 5 12 5z"
            />
            <path
              fill="#4285F4"
              d="M23.5 12.3c0-.8-.1-1.7-.2-2.3H12v4.6h6.5c-.3 1.5-1.1 2.8-2.4 3.7l3.7 2.9c2.2-2 3.7-5 3.7-8.9z"
            />
            <path
              fill="#FBBC05"
              d="M5.3 14.7c-.2-.7-.4-1.5-.4-2.7s.1-2 .4-2.7L1.6 6.4C.6 8.4 0 10.6 0 13s.6 4.6 1.6 6.6l3.7-4.9z"
            />
            <path
              fill="#34A853"
              d="M12 23c3.2 0 6-1.1 8-3l-3.7-2.9c-1.1.7-2.5 1.2-4.3 1.2-3.1 0-5.8-2.2-6.7-5.3L1.6 16c1.9 3.8 5.8 7 10.4 7z"
            />
          </svg>
          <span>Continuar con Google</span>
        </button>

        <div className="relative flex items-center justify-center mb-6">
          <div className="border-t border-[#252838] w-full" />
          <span className="bg-[#131623] px-3 text-[11px] uppercase font-bold text-[#7a7d90] absolute">
            o con correo
          </span>
        </div>

        {/* Login Form */}
        {mode === 'login' ? (
          <form onSubmit={handleLoginSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">
                Correo Electrónico
              </label>
              <div className="relative flex items-center">
                <Mail className="w-4 h-4 text-[#7a7d90] absolute left-3" />
                <input
                  type="email"
                  placeholder="correo@ejemplo.com"
                  value={loginEmail}
                  onChange={e => setLoginEmail(e.target.value)}
                  className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl pl-10 pr-4 py-2.5 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">
                Contraseña
              </label>
              <div className="relative flex items-center">
                <Lock className="w-4 h-4 text-[#7a7d90] absolute left-3" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={loginPassword}
                  onChange={e => setLoginPassword(e.target.value)}
                  className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl pl-10 pr-10 py-2.5 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(v => !v)}
                  className="absolute right-3 text-[#7a7d90] hover:text-white"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 rounded-xl bg-[#F5A623] hover:bg-[#e09516] text-[#0d0e18] font-bold text-sm flex items-center justify-center gap-2 transition-all cursor-pointer shadow-lg shadow-[#F5A623]/20"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Ingresando...</span>
                </>
              ) : (
                <>
                  <span>Ingresar a Mi Cuenta</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        ) : (
          /* Register Form */
          <form onSubmit={handleRegisterSubmit} className="space-y-3.5">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">Nombre</label>
                <input
                  type="text"
                  placeholder="Alex"
                  value={regName}
                  onChange={e => setRegName(e.target.value)}
                  className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl px-3 py-2 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                  required
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">Apellido</label>
                <input
                  type="text"
                  placeholder="García"
                  value={regLastname}
                  onChange={e => setRegLastname(e.target.value)}
                  className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl px-3 py-2 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">
                Correo Electrónico
              </label>
              <input
                type="email"
                placeholder="correo@ejemplo.com"
                value={regEmail}
                onChange={e => setRegEmail(e.target.value)}
                className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl px-3 py-2 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">
                WhatsApp (Guatemala)
              </label>
              <div className="relative flex items-center">
                <span className="absolute left-3 text-xs font-bold text-[#7a7d90] select-none">
                  +502
                </span>
                <input
                  type="tel"
                  placeholder="51234567"
                  value={regPhone}
                  onChange={e => setRegPhone(e.target.value.replace(/\D/g, '').slice(0, 8))}
                  className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl pl-14 pr-4 py-2 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-[#b0b3c6] mb-1">Contraseña</label>
              <input
                type="password"
                placeholder="Mínimo 6 caracteres"
                value={regPassword}
                onChange={e => setRegPassword(e.target.value)}
                className="w-full bg-[#1a1d2e] border border-[#252838] rounded-xl px-3 py-2 text-sm text-white placeholder-[#7a7d90] focus:outline-none focus:border-[#F5A623]"
                minLength={6}
                required
              />
            </div>

            <label className="flex items-start gap-2 cursor-pointer select-none pt-1">
              <input
                type="checkbox"
                checked={regAgree}
                onChange={e => setRegAgree(e.target.checked)}
                className="mt-0.5 rounded border-[#252838] bg-[#1a1d2e] text-[#F5A623] focus:ring-0"
                required
              />
              <span className="text-[11px] text-[#7a7d90]">
                Acepto los términos del servicio y autorizo el contacto por WhatsApp para entrega de accesos.
              </span>
            </label>

            <button
              type="submit"
              disabled={isLoading || !regAgree}
              className="w-full py-3 rounded-xl bg-[#F5A623] hover:bg-[#e09516] text-[#0d0e18] font-bold text-sm flex items-center justify-center gap-2 transition-all cursor-pointer shadow-lg shadow-[#F5A623]/20"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Registrando...</span>
                </>
              ) : (
                <>
                  <span>Crear mi Cuenta</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}

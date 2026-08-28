import { createContext, useContext, useEffect, useState, useTransition, type ReactNode } from 'react'
import { supabase } from '../lib/supabase'
import { env } from '../config/env'
import type { User, UserRole, RegisterFormData, PendingOAuthUser, AuthResult } from '../types/auth'

interface AuthContextType {
  user: User | null
  isLoading: boolean
  isRestoring: boolean
  needsOnboarding: boolean
  pendingOAuthUser: PendingOAuthUser | null
  login: (email: string, password: string) => Promise<AuthResult>
  loginWithGoogle: () => Promise<void>
  register: (userData: RegisterFormData) => Promise<AuthResult>
  completeOnboarding: (phone: string) => Promise<AuthResult>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [isRestoring, setIsRestoring] = useState<boolean>(true)
  const [needsOnboarding, setNeedsOnboarding] = useState<boolean>(false)
  const [pendingOAuthUser, setPendingOAuthUser] = useState<PendingOAuthUser | null>(null)
  const [, startTransition] = useTransition()

  // Helper to check user against backend profile
  const checkBackendUser = async (uid: string, email: string, name: string) => {
    try {
      const token = (await supabase.auth.getSession()).data.session?.access_token
      const res = await fetch(`${env.apiUrl}/api/v1/auth/me`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      })

      if (res.ok) {
        const meData = await res.json()
        const mappedUser: User = {
          id: uid,
          email,
          role: (meData.role as UserRole) || 'client',
          name,
        }
        setUser(mappedUser)
        setNeedsOnboarding(false)
        setPendingOAuthUser(null)
      } else if (res.status === 404) {
        setUser(null)
        setPendingOAuthUser({ supabaseUid: uid, email, name })
        setNeedsOnboarding(true)
      } else {
        // Fallback for direct Supabase user
        setUser({
          id: uid,
          email,
          role: 'client',
          name,
        })
        setNeedsOnboarding(false)
        setPendingOAuthUser(null)
      }
    } catch {
      // Offline / dev fallback: establish Supabase session directly
      setUser({
        id: uid,
        email,
        role: 'client',
        name,
      })
      setNeedsOnboarding(false)
      setPendingOAuthUser(null)
    } finally {
      setIsLoading(false)
      setIsRestoring(false)
    }
  }

  // Restore session on initial load
  useEffect(() => {
    let mounted = true

    const initAuth = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession()
        if (session?.user && mounted) {
          const { id, email, user_metadata } = session.user
          const name: string =
            user_metadata?.full_name ?? user_metadata?.name ?? email ?? ''
          await checkBackendUser(id, email ?? '', name)
        } else if (mounted) {
          setIsRestoring(false)
        }
      } catch {
        if (mounted) setIsRestoring(false)
      }
    }

    initAuth()

    const { data: { subscription } } = supabase.auth.onAuthStateChange(
      (event, session) => {
        if (event === 'SIGNED_IN' && session?.user) {
          const { id, email, user_metadata } = session.user
          const name: string =
            user_metadata?.full_name ?? user_metadata?.name ?? email ?? ''
          checkBackendUser(id, email ?? '', name)
        } else if (event === 'SIGNED_OUT' || !session) {
          setUser(null)
          setNeedsOnboarding(false)
          setPendingOAuthUser(null)
          setIsLoading(false)
          setIsRestoring(false)
        }
      }
    )

    return () => {
      mounted = false
      subscription.unsubscribe()
    }
  }, [])

  const loginWithGoogle = async () => {
    setIsLoading(true)
    await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: {
        redirectTo: `${window.location.origin}/login`,
      },
    })
  }

  const login = async (email: string, password: string): Promise<AuthResult> => {
    setIsLoading(true)
    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password,
      })

      if (error) {
        setIsLoading(false)
        return { success: false, user: null, error: error.message }
      }

      if (data.user) {
        const mappedUser: User = {
          id: data.user.id,
          email: data.user.email || '',
          role: (data.user.app_metadata?.role as UserRole) || 'client',
          name: data.user.user_metadata?.name,
          lastname: data.user.user_metadata?.lastname,
          phone: data.user.user_metadata?.phone,
        }
        setUser(mappedUser)
        setIsLoading(false)
        return { success: true, user: mappedUser, error: null }
      }

      setIsLoading(false)
      return { success: false, user: null, error: 'Usuario no encontrado' }
    } catch (err) {
      setIsLoading(false)
      return {
        success: false,
        user: null,
        error: (err as Error).message || 'Error al iniciar sesión',
      }
    }
  }

  const register = async (userData: RegisterFormData): Promise<AuthResult> => {
    setIsLoading(true)
    try {
      // Call backend client registration endpoint if available
      const res = await fetch(`${env.apiUrl}/api/v1/auth/register-client`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: userData.email,
          password: userData.password,
          name: `${userData.name} ${userData.lastname}`.trim(),
          phone: userData.phone,
          vendorUuid: env.storeVendorUuid,
        }),
      })

      if (!res.ok) {
        // Direct Supabase sign up fallback if backend not available
        const { error } = await supabase.auth.signUp({
          email: userData.email,
          password: userData.password,
          options: {
            data: {
              name: userData.name,
              lastname: userData.lastname,
              phone: userData.phone,
            },
          },
        })
        if (error) {
          setIsLoading(false)
          return { success: false, user: null, error: error.message }
        }
      }

      setIsLoading(false)
      return { success: true, user: null, error: null }
    } catch {
      // Supabase fallback
      const { error } = await supabase.auth.signUp({
        email: userData.email,
        password: userData.password,
        options: {
          data: {
            name: userData.name,
            lastname: userData.lastname,
            phone: userData.phone,
          },
        },
      })
      setIsLoading(false)
      if (error) return { success: false, user: null, error: error.message }
      return { success: true, user: null, error: null }
    }
  }

  const completeOnboarding = async (phone: string): Promise<AuthResult> => {
    if (!pendingOAuthUser) {
      return { success: false, user: null, error: 'No hay sesión OAuth pendiente' }
    }

    setIsLoading(true)
    try {
      const res = await fetch(`${env.apiUrl}/api/v1/auth/register-client`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: pendingOAuthUser.email,
          name: pendingOAuthUser.name,
          phone,
          vendorUuid: env.storeVendorUuid,
          externalId: pendingOAuthUser.supabaseUid,
        }),
      })

      if (res.ok) {
        await supabase.auth.refreshSession()
        await checkBackendUser(
          pendingOAuthUser.supabaseUid,
          pendingOAuthUser.email,
          pendingOAuthUser.name
        )
        return { success: true, user: null, error: null }
      }

      // Fallback
      setUser({
        id: pendingOAuthUser.supabaseUid,
        email: pendingOAuthUser.email,
        role: 'client',
        name: pendingOAuthUser.name,
        phone,
      })
      setNeedsOnboarding(false)
      setPendingOAuthUser(null)
      setIsLoading(false)
      return { success: true, user: null, error: null }
    } catch {
      setUser({
        id: pendingOAuthUser.supabaseUid,
        email: pendingOAuthUser.email,
        role: 'client',
        name: pendingOAuthUser.name,
        phone,
      })
      setNeedsOnboarding(false)
      setPendingOAuthUser(null)
      setIsLoading(false)
      return { success: true, user: null, error: null }
    }
  }

  const logout = async () => {
    try {
      await supabase.auth.signOut()
    } finally {
      startTransition(() => {
        setUser(null)
        setNeedsOnboarding(false)
        setPendingOAuthUser(null)
      })
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isRestoring,
        needsOnboarding,
        pendingOAuthUser,
        login,
        loginWithGoogle,
        register,
        completeOnboarding,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

// Domain types — re-exported from shared monorepo package
export type { User, UserRole, AuthResult } from '@neversion/models'

// Store-specific types (UI form state, OAuth onboarding)

export interface RegisterFormData {
  name: string
  lastname: string
  email: string
  password: string
  phone: string
  checkNewsletter?: boolean
  checkCookies?: boolean
}

export interface PendingOAuthUser {
  supabaseUid: string
  email: string
  name: string
}
